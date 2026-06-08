-- Ejecutar completo en el SQL Editor de Supabase.

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.users (id, name, is_admin)
  values (
    new.id,
    coalesce(
      nullif(trim(new.raw_user_meta_data ->> 'name'), ''),
      split_part(coalesce(new.email, 'Cliente LPZ'), '@', 1)
    ),
    false
  )
  on conflict (id) do nothing;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

alter table public.products
  add column if not exists unit_cost numeric;

update public.products
set unit_cost = price
where unit_cost is null;

alter table public.products
  alter column unit_cost set not null;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'products_unit_cost_nonnegative'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_unit_cost_nonnegative check (unit_cost >= 0);
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

  for item_record in
    select
      p.id,
      p.price,
      p.unit_cost,
      p.stock,
      p.active,
      requested.quantity
    from public.products p
    join (
      select
        (value ->> 'product_id')::uuid as product_id,
        sum((value ->> 'quantity')::integer) as quantity
      from jsonb_array_elements(items)
      group by (value ->> 'product_id')::uuid
    ) requested on requested.product_id = p.id
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

    if item_record.stock < item_record.quantity then
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
      requested.quantity
    from public.products p
    join (
      select
        (value ->> 'product_id')::uuid as product_id,
        sum((value ->> 'quantity')::integer) as quantity
      from jsonb_array_elements(items)
      group by (value ->> 'product_id')::uuid
    ) requested on requested.product_id = p.id
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
    set stock = stock - item_record.quantity
    where id = item_record.id;
  end loop;

  return sale_id;
end;
$$;

create or replace function public.get_admin_total_profit()
returns numeric
language plpgsql
security definer
set search_path = public
as $$
declare
  current_user_id uuid := auth.uid();
  total_profit numeric;
begin
  if current_user_id is null or not exists (
    select 1
    from public.users u
    where u.id = current_user_id
      and u.is_admin = true
  ) then
    raise exception 'ADMIN_REQUIRED';
  end if;

  select coalesce(sum((sd.unit_price - sd.unit_cost) * sd.quantity), 0)
  into total_profit
  from public.sales_details sd
  join public.sales s on s.id_sale = sd.id_sale
  where upper(trim(s.state)) not in ('CANCELADO', 'CANCELADA', 'CANCELLED');

  return total_profit;
end;
$$;

revoke execute on function public.confirm_sale_with_stock(uuid, uuid, numeric, text, jsonb) from public, anon;
revoke execute on function public.get_admin_total_profit() from public, anon;
grant execute on function public.confirm_sale_with_stock(uuid, uuid, numeric, text, jsonb) to authenticated;
grant execute on function public.get_admin_total_profit() to authenticated;
