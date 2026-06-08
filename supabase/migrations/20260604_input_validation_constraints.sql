-- Ejecutar completo despues de 20260604_auth_sales_profit.sql.

update public.users
set phone = nullif(regexp_replace(coalesce(phone, ''), '\D', '', 'g'), '')
where phone is not null;

update public.users
set phone = null
where phone is not null
  and phone !~ '^[0-9]{10}$';

update public.addresses
set postal_code = nullif(regexp_replace(coalesce(postal_code, ''), '\D', '', 'g'), '')
where postal_code is not null;

update public.addresses
set postal_code = null
where postal_code is not null
  and postal_code !~ '^[0-9]{5}$';

do $$
begin
  if exists (
    select 1
    from public.products
    where price <= 0
       or unit_cost <= 0
       or price <> round(price, 2)
       or unit_cost <> round(unit_cost, 2)
       or stock < 0
       or stock > 99999
       or char_length(trim(title)) not between 1 and 150
       or char_length(coalesce(description, '')) > 1000
       or char_length(coalesce(img_url, '')) > 500
       or (
         nullif(trim(img_url), '') is not null
         and trim(img_url) !~* '^https?://'
       )
  ) then
    raise exception 'Hay productos con precio, costo, stock o textos invalidos. Corrigelos antes de aplicar las restricciones.';
  end if;

  if exists (
    select 1
    from public.users
    where char_length(trim(name)) not between 2 and 80
  ) then
    raise exception 'Hay usuarios con nombres fuera del rango de 2 a 80 caracteres. Corrigelos antes de aplicar las restricciones.';
  end if;

  if exists (
    select 1
    from public.addresses
    where char_length(trim(street)) not between 1 and 150
       or char_length(trim(city)) not between 1 and 80
       or (
         state_addres is not null
         and char_length(trim(state_addres)) not between 1 and 80
       )
  ) then
    raise exception 'Hay direcciones con textos fuera de los limites permitidos. Corrigelas antes de aplicar las restricciones.';
  end if;
end;
$$;

do $$
begin
  if not exists (
    select 1 from pg_constraint
    where conname = 'users_name_length'
      and conrelid = 'public.users'::regclass
  ) then
    alter table public.users
      add constraint users_name_length
      check (char_length(trim(name)) between 2 and 80);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'users_phone_format'
      and conrelid = 'public.users'::regclass
  ) then
    alter table public.users
      add constraint users_phone_format
      check (phone is null or phone ~ '^[0-9]{10}$');
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'addresses_street_length'
      and conrelid = 'public.addresses'::regclass
  ) then
    alter table public.addresses
      add constraint addresses_street_length
      check (char_length(trim(street)) between 1 and 150);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'addresses_city_length'
      and conrelid = 'public.addresses'::regclass
  ) then
    alter table public.addresses
      add constraint addresses_city_length
      check (char_length(trim(city)) between 1 and 80);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'addresses_state_length'
      and conrelid = 'public.addresses'::regclass
  ) then
    alter table public.addresses
      add constraint addresses_state_length
      check (
        state_addres is null
        or char_length(trim(state_addres)) between 1 and 80
      );
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'addresses_postal_code_format'
      and conrelid = 'public.addresses'::regclass
  ) then
    alter table public.addresses
      add constraint addresses_postal_code_format
      check (postal_code is null or postal_code ~ '^[0-9]{5}$');
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_price_positive'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_price_positive check (price > 0);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_price_two_decimals'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_price_two_decimals check (price = round(price, 2));
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_unit_cost_positive'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_unit_cost_positive check (unit_cost > 0);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_unit_cost_two_decimals'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_unit_cost_two_decimals check (unit_cost = round(unit_cost, 2));
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_stock_range'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_stock_range check (stock between 0 and 99999);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_title_length'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_title_length
      check (char_length(trim(title)) between 1 and 150);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_description_length'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_description_length
      check (char_length(coalesce(description, '')) <= 1000);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_img_url_length'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_img_url_length
      check (char_length(coalesce(img_url, '')) <= 500);
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'products_img_url_format'
      and conrelid = 'public.products'::regclass
  ) then
    alter table public.products
      add constraint products_img_url_format
      check (
        nullif(trim(img_url), '') is null
        or trim(img_url) ~* '^https?://'
      );
  end if;

  if not exists (
    select 1 from pg_constraint
    where conname = 'payment_methods_last4_format'
      and conrelid = 'public.payment_methods'::regclass
  ) then
    alter table public.payment_methods
      add constraint payment_methods_last4_format
      check (last4 is null or trim(last4) ~ '^[0-9]{4}$');
  end if;
end;
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  requested_name text;
begin
  requested_name := trim(coalesce(new.raw_user_meta_data ->> 'name', ''));

  if char_length(requested_name) not between 2 and 80 then
    requested_name := 'Cliente LPZ';
  end if;

  insert into public.users (id, name, is_admin)
  values (new.id, requested_name, false)
  on conflict (id) do nothing;

  return new;
end;
$$;
