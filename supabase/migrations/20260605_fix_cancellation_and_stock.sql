-- Ejecutar despues de 20260604_input_validation_constraints.sql.

update public.products
set stock = 0
where stock < 0;

alter table public.sales
  add column if not exists cancellation_reason text;

update public.sales
set cancellation_reason = 'Cancelado por administración.'
where upper(trim(state)) in ('CANCELADO', 'CANCELADA', 'CANCELLED')
  and nullif(trim(coalesce(cancellation_reason, '')), '') is null;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'sales_cancellation_reason_required'
      and conrelid = 'public.sales'::regclass
  ) then
    alter table public.sales
      add constraint sales_cancellation_reason_required
      check (
        upper(trim(state)) not in ('CANCELADO', 'CANCELADA', 'CANCELLED')
        or nullif(trim(coalesce(cancellation_reason, '')), '') is not null
      );
  end if;

  if not exists (
    select 1
    from pg_constraint
    where conname = 'products_stock_range'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_stock_range check (stock between 0 and 99999);
  end if;
end;
$$;

drop function if exists public.confirm_sale_with_stock(uuid, uuid, numeric, text, jsonb);

create or replace function public.confirm_sale_with_stock(
  sale_id uuid,
  address_id uuid,
  shipping_cost numeric,
  payment_method_value text,
  items jsonb
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
#variable_conflict use_variable
declare
  current_user_id uuid := auth.uid();
  calculated_total numeric := greatest(coalesce(shipping_cost, 0), 0);
  requested_product_count integer;
  validated_product_count integer := 0;
  item_record record;
begin
  if current_user_id is null then
    raise exception 'AUTH_REQUIRED';
  end if;

  if sale_id is null or address_id is null then
    raise exception 'INVALID_SALE_DATA';
  end if;

  if not exists (
    select 1
    from public.addresses a
    where a.id_address = address_id
      and a.id_user = current_user_id
  ) then
    raise exception 'INVALID_ADDRESS';
  end if;

  if items is null or jsonb_typeof(items) <> 'array' or jsonb_array_length(items) = 0 then
    raise exception 'EMPTY_CART';
  end if;

  select count(*)
  into requested_product_count
  from (
    select (value ->> 'product_id')::uuid as product_id
    from jsonb_array_elements(items)
    group by (value ->> 'product_id')::uuid
  ) requested;

  drop table if exists tmp_confirm_sale_items;

  create temporary table tmp_confirm_sale_items on commit drop as
  select
    (value ->> 'product_id')::uuid as product_id,
    sum((value ->> 'quantity')::integer) as quantity
  from jsonb_array_elements(items)
  group by (value ->> 'product_id')::uuid;

  for item_record in
    select
      p.id,
      p.price,
      p.unit_cost,
      p.stock as locked_stock,
      p.active,
      requested.quantity
    from public.products p
    join tmp_confirm_sale_items requested on requested.product_id = p.id
    order by p.id
    for update of p
  loop
    validated_product_count := validated_product_count + 1;

    if item_record.quantity <= 0 then
      raise exception 'INVALID_QUANTITY';
    end if;

    if not item_record.active then
      raise exception 'INACTIVE_PRODUCT';
    end if;

    if item_record.locked_stock < item_record.quantity then
      raise exception 'INSUFFICIENT_STOCK';
    end if;

    calculated_total := calculated_total + (item_record.price * item_record.quantity);
  end loop;

  if validated_product_count <> requested_product_count then
    raise exception 'PRODUCT_NOT_FOUND';
  end if;

  insert into public.sales (
    id_sale,
    id_user,
    id_address,
    total,
    state,
    payment_method
  )
  values (
    sale_id,
    current_user_id,
    address_id,
    calculated_total,
    'PENDIENTE',
    nullif(trim(payment_method_value), '')
  );

  for item_record in
    select
      p.id,
      p.price,
      p.unit_cost,
      p.stock as locked_stock,
      requested.quantity
    from public.products p
    join tmp_confirm_sale_items requested on requested.product_id = p.id
    order by p.id
  loop
    insert into public.sales_details (
      id_sale,
      id_product,
      quantity,
      unit_price,
      unit_cost
    )
    values (
      sale_id,
      item_record.id,
      item_record.quantity,
      item_record.price,
      item_record.unit_cost
    );

    update public.products
    set stock = greatest(item_record.locked_stock - item_record.quantity, 0)
    where id = item_record.id;
  end loop;

  return sale_id;
end;
$$;

drop function if exists public.update_admin_order_status(uuid, text);
drop function if exists public.update_admin_order_status(uuid, text, text);

create or replace function public.update_admin_order_status(
  order_id uuid,
  new_status text,
  cancellation_reason_value text default null
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  current_user_id uuid := auth.uid();
  clean_status text := upper(trim(coalesce(new_status, '')));
  clean_reason text := nullif(trim(coalesce(cancellation_reason_value, '')), '');
begin
  if current_user_id is null or not exists (
    select 1
    from public.users u
    where u.id = current_user_id
      and u.is_admin = true
  ) then
    raise exception 'ADMIN_REQUIRED';
  end if;

  if order_id is null or clean_status = '' then
    raise exception 'INVALID_ORDER_STATUS';
  end if;

  if clean_status in ('CANCELADO', 'CANCELADA', 'CANCELLED') and clean_reason is null then
    raise exception 'CANCELLATION_REASON_REQUIRED';
  end if;

  update public.sales
  set
    state = clean_status,
    cancellation_reason = case
      when clean_status in ('CANCELADO', 'CANCELADA', 'CANCELLED') then clean_reason
      else null
    end
  where id_sale = order_id;

  if not found then
    raise exception 'ORDER_NOT_FOUND';
  end if;
end;
$$;

drop function if exists public.get_admin_recent_orders(integer) cascade;

create or replace function public.get_admin_recent_orders(limit_count integer default 5)
returns table (
  id_sale uuid,
  id_user uuid,
  customer_name text,
  total double precision,
  state text,
  cancellation_reason text,
  created_at text,
  item_count bigint
)
language plpgsql
security definer
set search_path = public
as $$
declare
  current_user_id uuid := auth.uid();
begin
  if current_user_id is null or not exists (
    select 1
    from public.users u
    where u.id = current_user_id
      and u.is_admin = true
  ) then
    raise exception 'ADMIN_REQUIRED';
  end if;

  return query
  select
    s.id_sale,
    s.id_user,
    coalesce(u.name, 'Cliente') as customer_name,
    s.total::double precision as total,
    s.state,
    s.cancellation_reason,
    s.created_at::text as created_at,
    coalesce(sum(sd.quantity), 0)::bigint as item_count
  from public.sales s
  left join public.users u on u.id = s.id_user
  left join public.sales_details sd on sd.id_sale = s.id_sale
  group by s.id_sale, s.id_user, u.name, s.total, s.state, s.cancellation_reason, s.created_at
  order by s.created_at desc
  limit greatest(coalesce(limit_count, 5), 1);
end;
$$;

revoke execute on function public.confirm_sale_with_stock(uuid, uuid, numeric, text, jsonb) from public, anon;
revoke execute on function public.update_admin_order_status(uuid, text, text) from public, anon;
revoke execute on function public.get_admin_recent_orders(integer) from public, anon;
grant execute on function public.confirm_sale_with_stock(uuid, uuid, numeric, text, jsonb) to authenticated;
grant execute on function public.update_admin_order_status(uuid, text, text) to authenticated;
grant execute on function public.get_admin_recent_orders(integer) to authenticated;
