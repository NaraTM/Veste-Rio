create table if not exists app_roles (
    id bigserial primary key,
    name varchar(30) not null unique,
    description varchar(80) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists app_users (
    id bigserial primary key,
    username varchar(60) not null unique,
    email varchar(120) not null unique,
    password_hash varchar(64) not null,
    full_name varchar(120) not null,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists app_user_roles (
    user_id bigint not null references app_users(id) on delete cascade,
    role_id bigint not null references app_roles(id) on delete cascade,
    primary key (user_id, role_id)
);

create table if not exists suppliers (
    id bigserial primary key,
    name varchar(120) not null,
    contact_name varchar(120),
    email varchar(120),
    phone varchar(30),
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists products (
    id bigserial primary key,
    sku varchar(40) not null unique,
    name varchar(120) not null,
    description varchar(600),
    supplier_id bigint references suppliers(id),
    unit_price numeric(15,2) not null,
    stock_quantity numeric(15,0) not null,
    min_stock numeric(15,0) not null,
    category varchar(60),
    product_type varchar(60),
    target_audience varchar(20),
    color_name varchar(30),
    size_label varchar(20),
    icon_name varchar(20),
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists purchases (
    id bigserial primary key,
    supplier_id bigint not null references suppliers(id),
    purchase_date date not null,
    status varchar(30) not null,
    total_value numeric(15,2) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists purchase_items (
    id bigserial primary key,
    purchase_id bigint not null references purchases(id) on delete cascade,
    product_id bigint not null references products(id),
    quantity numeric(15,0) not null,
    unit_cost numeric(15,2) not null,
    subtotal numeric(15,2) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists sales (
    id bigserial primary key,
    seller_id bigint not null references app_users(id),
    sale_date date not null,
    status varchar(30) not null,
    discount_percent numeric(5,2) not null default 0,
    discount_value numeric(15,2) not null default 0,
    coupon_code varchar(40),
    total_value numeric(15,2) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists sale_items (
    id bigserial primary key,
    sale_id bigint not null references sales(id) on delete cascade,
    product_id bigint not null references products(id),
    quantity numeric(15,0) not null,
    unit_price numeric(15,2) not null,
    subtotal numeric(15,2) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists payments (
    id bigserial primary key,
    sale_id bigint not null references sales(id) on delete cascade,
    method varchar(30) not null,
    status varchar(30) not null,
    amount numeric(15,2) not null,
    payment_date date,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists discount_coupons (
    id bigserial primary key,
    code varchar(40) not null unique,
    description varchar(160),
    discount_percent numeric(5,2) not null,
    active boolean not null default true,
    valid_until date,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists stock_movements (
    id bigserial primary key,
    product_id bigint not null references products(id),
    type varchar(30) not null,
    quantity numeric(15,0) not null,
    reference_type varchar(40),
    note varchar(120),
    moved_at timestamp not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
