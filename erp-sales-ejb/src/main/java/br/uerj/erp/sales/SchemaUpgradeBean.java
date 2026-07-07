package br.uerj.erp.sales;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Singleton
@Startup
public class SchemaUpgradeBean {

    @PersistenceContext(unitName = "erpPU")
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        List<String> statements = List.of(
                "alter table if exists sales add column if not exists discount_percent numeric(5,2) not null default 0",
                "alter table if exists sales add column if not exists discount_value numeric(15,2) not null default 0",
                "alter table if exists sales add column if not exists coupon_code varchar(40)",
                "update sales set discount_percent = 0 where discount_percent is null",
                "update sales set discount_value = 0 where discount_value is null",
                "alter table if exists products add column if not exists category varchar(60)",
                "alter table if exists products add column if not exists product_type varchar(60)",
                "alter table if exists products add column if not exists target_audience varchar(20)",
                "alter table if exists products add column if not exists color_name varchar(30)",
                "alter table if exists products add column if not exists size_label varchar(20)",
                "alter table if exists products add column if not exists icon_name varchar(20)",
                "alter table if exists products add column if not exists supplier_id bigint",
                "create table if not exists discount_coupons (id bigserial primary key, code varchar(40) not null unique, description varchar(160), discount_percent numeric(5,2) not null, active boolean not null default true, valid_until date, created_at timestamptz not null default now(), updated_at timestamptz not null default now())",
                "insert into discount_coupons (code, description, discount_percent, active, valid_until, created_at, updated_at) values ('BEMVINDO10', 'Cupom de boas-vindas', 10.00, true, current_date + 60, now(), now()) on conflict (code) do nothing",
                "insert into discount_coupons (code, description, discount_percent, active, valid_until, created_at, updated_at) values ('OUTONO15', 'Campanha sazonal', 15.00, true, current_date + 45, now(), now()) on conflict (code) do nothing",
                "insert into discount_coupons (code, description, discount_percent, active, valid_until, created_at, updated_at) values ('VIP20', 'Ação promocional premium', 20.00, true, current_date + 30, now(), now()) on conflict (code) do nothing",
                "update products set supplier_id = 1 where supplier_id is null and category = 'SUPERIOR'",
                "update products set supplier_id = 2 where supplier_id is null and category = 'INFERIOR'",
                "update products set supplier_id = 4 where supplier_id is null and category = 'INTIMO'",
                "update products set supplier_id = 3 where supplier_id is null and category = 'CALCADO'",
                "update payments set method = 'PIX' where method = 'CASH'"
        );

        for (String statement : statements) {
            entityManager.createNativeQuery(statement).executeUpdate();
        }
    }
}
