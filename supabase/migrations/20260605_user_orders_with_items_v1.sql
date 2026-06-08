-- Devuelve las compras del usuario autenticado con sus productos.
-- Ejecutar despues de 20260605_fix_cancellation_and_stock.sql.

alter table public.sales
  add column if not exists cancellation_reason text;

create or replace function public.get_user_orders_with_items_v1()
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  current_user_id uuid := auth.uid();
begin
  if current_user_id is null then
    raise exception 'AUTH_REQUIRED';
  end if;

  return coalesce(
    (
      select jsonb_agg(order_payload order by order_created_at desc)
      from (
        select
          s.created_at as order_created_at,
          jsonb_build_object(
            'id_sale', s.id_sale::text,
            'id_user', s.id_user::text,
            'id_address', s.id_address::text,
            'total', coalesce(s.total, 0)::double precision,
            'state', coalesce(s.state, 'PENDIENTE'),
            'payment_method', s.payment_method,
            'cancellation_reason', s.cancellation_reason,
            'created_at', coalesce(s.created_at::text, ''),
            'items', coalesce(
              (
                select jsonb_agg(
                  jsonb_build_object(
                    'productId', sd.id_product::text,
                    'productTitle', coalesce(p.title, 'Producto'),
                    'artistName', coalesce(a.name, 'Artista desconocido'),
                    'quantity', coalesce(sd.quantity, 0),
                    'selectedFormat', coalesce(c.name, 'Formato fisico'),
                    'unitPrice', coalesce(sd.unit_price, 0)::double precision,
                    'imageUrl', p.img_url
                  )
                  order by p.title, sd.id_product
                )
                from public.sales_details sd
                left join public.products p on p.id = sd.id_product
                left join public.artists a on a.id = p.fk_id_artist
                left join public.categories c on c.id = p.fk_id_category
                where sd.id_sale = s.id_sale
              ),
              '[]'::jsonb
            )
          ) as order_payload
        from public.sales s
        where s.id_user = current_user_id
      ) payloads
    ),
    '[]'::jsonb
  );
end;
$$;

revoke execute on function public.get_user_orders_with_items_v1() from public, anon;
grant execute on function public.get_user_orders_with_items_v1() to authenticated;

notify pgrst, 'reload schema';
