-- RPC nueva para evitar conflictos con la firma anterior de get_admin_recent_orders.

alter table public.sales
  add column if not exists cancellation_reason text;

create or replace function public.get_admin_recent_orders_v2(limit_count integer default 100)
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
    coalesce(u.name, 'Cliente')::text as customer_name,
    coalesce(s.total, 0)::double precision as total,
    coalesce(s.state, 'SIN ESTADO')::text as state,
    s.cancellation_reason::text as cancellation_reason,
    coalesce(s.created_at::text, '') as created_at,
    coalesce(sum(sd.quantity), 0)::bigint as item_count
  from public.sales s
  left join public.users u on u.id = s.id_user
  left join public.sales_details sd on sd.id_sale = s.id_sale
  group by s.id_sale, s.id_user, u.name, s.total, s.state, s.cancellation_reason, s.created_at
  order by s.created_at desc
  limit greatest(coalesce(limit_count, 100), 1);
end;
$$;

revoke execute on function public.get_admin_recent_orders_v2(integer) from public, anon;
grant execute on function public.get_admin_recent_orders_v2(integer) to authenticated;

notify pgrst, 'reload schema';
