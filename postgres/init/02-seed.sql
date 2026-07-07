insert into app_roles (id, name, description, created_at, updated_at) values
    (1, 'ADMIN', 'Administrador', now(), now()),
    (2, 'MANAGER', 'Gerente', now(), now()),
    (3, 'SELLER', 'Vendedor', now(), now())
on conflict (id) do nothing;

insert into app_users (id, username, email, password_hash, full_name, active, created_at, updated_at) values
    (1, 'admin', 'admin@erp.local', '3eb3fe66b31e3b4d10fa70b5cad49c7112294af6ae4e476a1c405155d45aa121', 'Administrador do Sistema', true, now(), now()),
    (2, 'manager', 'manager@erp.local', 'b9a9c0814e9d1a10eff04665d00546f83e2514499c7a43b5cc59d663878291f7', 'Gerente Comercial', true, now(), now()),
    (3, 'seller1', 'seller1@erp.local', '6bdf756d33ffcb4d534dd0ec81e28d51aa771093494e16d910fbcc24c2142532', 'Vendedor 1', true, now(), now()),
    (4, 'seller2', 'seller2@erp.local', '6bdf756d33ffcb4d534dd0ec81e28d51aa771093494e16d910fbcc24c2142532', 'Vendedor 2', true, now(), now()),
    (5, 'seller3', 'seller3@erp.local', '6bdf756d33ffcb4d534dd0ec81e28d51aa771093494e16d910fbcc24c2142532', 'Vendedor 3', true, now(), now()),
    (6, 'seller4', 'seller4@erp.local', '6bdf756d33ffcb4d534dd0ec81e28d51aa771093494e16d910fbcc24c2142532', 'Vendedor 4', true, now(), now()),
    (7, 'seller5', 'seller5@erp.local', '6bdf756d33ffcb4d534dd0ec81e28d51aa771093494e16d910fbcc24c2142532', 'Vendedor 5', true, now(), now())
on conflict (id) do nothing;

insert into app_user_roles (user_id, role_id) values
    (1, 1),
    (1, 2),
    (1, 3),
    (2, 2),
    (2, 3),
    (3, 3),
    (4, 3),
    (5, 3),
    (6, 3),
    (7, 3)
on conflict do nothing;

insert into suppliers (id, name, contact_name, email, phone, active, created_at, updated_at) values
    (1, 'Têxtil Alpha', 'Maria Souza', 'contato@textilalpha.local', '21999990001', true, now(), now()),
    (2, 'Moda Beta', 'João Lima', 'contato@modabeta.local', '21999990002', true, now(), now()),
    (3, 'Calçados Delta', 'Carla Nunes', 'contato@calcadosdelta.local', '21999990003', true, now(), now()),
    (4, 'Íntima Gama', 'Paulo Reis', 'contato@intimagama.local', '21999990004', true, now(), now())
on conflict (id) do nothing;

insert into products (id, sku, name, description, supplier_id, unit_price, stock_quantity, min_stock, category, product_type, target_audience, color_name, size_label, icon_name, active, created_at, updated_at) values
    (1, 'SUP-CAM-001', 'Camiseta Básica', 'Malha de algodão para uso diário', 1, 89.62, 24, 6, 'SUPERIOR', 'CAMISETA', 'MASCULINO', 'AZUL', 'M', '👕', true, now(), now()),
    (2, 'SUP-CAM-002', 'Camiseta Slim', 'Modelagem ajustada para uso casual', 1, 96.48, 18, 5, 'SUPERIOR', 'CAMISETA', 'FEMININO', 'ROSA', 'P', '👕', true, now(), now()),
    (3, 'SUP-CMS-001', 'Camisa Social', 'Camisa de tecido leve para trabalho', 1, 156.96, 14, 4, 'SUPERIOR', 'CAMISA', 'MASCULINO', 'BRANCO', 'G', '👔', true, now(), now()),
    (4, 'SUP-POL-001', 'Polo Confort', 'Polo em malha piquet', 1, 132.74, 16, 4, 'SUPERIOR', 'POLO', 'UNISSEX', 'VERDE', 'M', '👕', true, now(), now()),
    (5, 'SUP-REG-001', 'Regata Fitness', 'Regata leve para treino', 1, 74.35, 20, 5, 'SUPERIOR', 'REGATA', 'FEMININO', 'VERMELHO', 'P', '🎽', true, now(), now()),
    (6, 'SUP-MOL-001', 'Moletom Soft', 'Moletom com toque macio', 1, 189.62, 12, 3, 'SUPERIOR', 'MOLETOM', 'UNISSEX', 'CINZA', 'G', '🧥', true, now(), now()),
    (7, 'SUP-JAQ-001', 'Jaqueta Corta Vento', 'Jaqueta leve para meia estação', 1, 219.48, 9, 3, 'SUPERIOR', 'JAQUETA', 'UNISSEX', 'PRETO', 'GG', '🧥', true, now(), now()),
    (8, 'SUP-VES-001', 'Vestido Casual', 'Vestido midi de tecido plano', 1, 178.25, 11, 3, 'SUPERIOR', 'VESTIDO', 'FEMININO', 'AZUL', 'M', '👗', true, now(), now()),
    (9, 'INF-CJE-001', 'Calça Jeans Reta', 'Jeans tradicional para uso diário', 2, 169.84, 15, 4, 'INFERIOR', 'CALCA_JEANS', 'MASCULINO', 'AZUL', '42', '👖', true, now(), now()),
    (10, 'INF-CJE-002', 'Calça Jeans Skinny', 'Jeans ajustado com elastano', 2, 176.43, 13, 4, 'INFERIOR', 'CALCA_JEANS', 'FEMININO', 'PRETO', '38', '👖', true, now(), now()),
    (11, 'INF-CSO-001', 'Calça Social', 'Calça para ambiente corporativo', 2, 184.27, 10, 3, 'INFERIOR', 'CALCA_SOCIAL', 'FEMININO', 'PRETO', '40', '👖', true, now(), now()),
    (12, 'INF-BER-001', 'Bermuda Sarja', 'Bermuda casual de sarja', 2, 128.90, 17, 5, 'INFERIOR', 'BERMUDA', 'UNISSEX', 'BEGE', '40', '🩳', true, now(), now()),
    (13, 'INF-SHO-001', 'Short Praia', 'Short leve para lazer', 2, 98.57, 19, 5, 'INFERIOR', 'SHORT', 'MASCULINO', 'AZUL', '42', '🩳', true, now(), now()),
    (14, 'INF-SAI-001', 'Saia Midi', 'Saia de comprimento midi', 2, 142.31, 12, 3, 'INFERIOR', 'SAIA', 'FEMININO', 'VERMELHO', '38', '👗', true, now(), now()),
    (15, 'INT-CUE-001', 'Cueca Boxer', 'Cueca em algodão com elastano', 4, 39.96, 30, 8, 'INTIMO', 'CUECA', 'MASCULINO', 'PRETO', 'G', '🩲', true, now(), now()),
    (16, 'INT-CAL-001', 'Calcinha Conforto', 'Calcinha básica de microfibra', 4, 34.82, 28, 8, 'INTIMO', 'CALCINHA', 'FEMININO', 'BEGE', 'M', '🩲', true, now(), now()),
    (17, 'INT-SUT-001', 'Sutiã Básico', 'Sutiã com bojo leve', 4, 79.43, 16, 4, 'INTIMO', 'SUTIA', 'FEMININO', 'BRANCO', '44', '👙', true, now(), now()),
    (18, 'INT-MEI-001', 'Meia Cano Médio', 'Par de meias para uso diário', 4, 24.78, 36, 10, 'INTIMO', 'MEIA', 'UNISSEX', 'BRANCO', '39-43', '🧦', true, now(), now()),
    (19, 'INT-MEI-002', 'Meia Esportiva', 'Par de meias para treino', 4, 27.64, 34, 10, 'INTIMO', 'MEIA', 'UNISSEX', 'PRETO', '34-38', '🧦', true, now(), now()),
    (20, 'CAL-TEN-001', 'Tênis Casual', 'Tênis urbano para dia a dia', 3, 289.62, 14, 4, 'CALCADO', 'TENIS', 'UNISSEX', 'BRANCO', '40', '👟', true, now(), now()),
    (21, 'CAL-TEN-002', 'Tênis Running', 'Tênis leve para caminhada', 3, 318.44, 10, 3, 'CALCADO', 'TENIS', 'UNISSEX', 'AZUL', '39', '👟', true, now(), now()),
    (22, 'CAL-SAP-001', 'Sapato Social', 'Sapato social de acabamento fosco', 3, 326.91, 8, 2, 'CALCADO', 'SAPATO', 'MASCULINO', 'PRETO', '41', '👞', true, now(), now()),
    (23, 'CAL-CHI-001', 'Chinelo Conforto', 'Chinelo com palmilha macia', 3, 62.58, 22, 6, 'CALCADO', 'CHINELO', 'UNISSEX', 'VERDE', '39', '🩴', true, now(), now()),
    (24, 'CAL-SAN-001', 'Sandália Urbana', 'Sandália casual para uso diário', 3, 148.73, 11, 3, 'CALCADO', 'SANDALIA', 'FEMININO', 'ROSA', '37', '👡', true, now(), now())
on conflict (id) do nothing;

insert into discount_coupons (id, code, description, discount_percent, active, valid_until, created_at, updated_at) values
    (1, 'BEMVINDO10', 'Cupom de boas-vindas', 10.00, true, current_date + 60, now(), now()),
    (2, 'OUTONO15', 'Campanha sazonal', 15.00, true, current_date + 45, now(), now()),
    (3, 'VIP20', 'Ação promocional premium', 20.00, true, current_date + 30, now(), now())
on conflict (id) do nothing;

insert into stock_movements (product_id, type, quantity, reference_type, note, moved_at, created_at, updated_at) values
    (1, 'INBOUND', 24, 'SEED', 'Carga inicial', now(), now(), now()),
    (2, 'INBOUND', 18, 'SEED', 'Carga inicial', now(), now(), now()),
    (3, 'INBOUND', 14, 'SEED', 'Carga inicial', now(), now(), now()),
    (4, 'INBOUND', 16, 'SEED', 'Carga inicial', now(), now(), now()),
    (5, 'INBOUND', 20, 'SEED', 'Carga inicial', now(), now(), now()),
    (6, 'INBOUND', 12, 'SEED', 'Carga inicial', now(), now(), now()),
    (7, 'INBOUND', 9, 'SEED', 'Carga inicial', now(), now(), now()),
    (8, 'INBOUND', 11, 'SEED', 'Carga inicial', now(), now(), now()),
    (9, 'INBOUND', 15, 'SEED', 'Carga inicial', now(), now(), now()),
    (10, 'INBOUND', 13, 'SEED', 'Carga inicial', now(), now(), now()),
    (11, 'INBOUND', 10, 'SEED', 'Carga inicial', now(), now(), now()),
    (12, 'INBOUND', 17, 'SEED', 'Carga inicial', now(), now(), now()),
    (13, 'INBOUND', 19, 'SEED', 'Carga inicial', now(), now(), now()),
    (14, 'INBOUND', 12, 'SEED', 'Carga inicial', now(), now(), now()),
    (15, 'INBOUND', 30, 'SEED', 'Carga inicial', now(), now(), now()),
    (16, 'INBOUND', 28, 'SEED', 'Carga inicial', now(), now(), now()),
    (17, 'INBOUND', 16, 'SEED', 'Carga inicial', now(), now(), now()),
    (18, 'INBOUND', 36, 'SEED', 'Carga inicial', now(), now(), now()),
    (19, 'INBOUND', 34, 'SEED', 'Carga inicial', now(), now(), now()),
    (20, 'INBOUND', 14, 'SEED', 'Carga inicial', now(), now(), now()),
    (21, 'INBOUND', 10, 'SEED', 'Carga inicial', now(), now(), now()),
    (22, 'INBOUND', 8, 'SEED', 'Carga inicial', now(), now(), now()),
    (23, 'INBOUND', 22, 'SEED', 'Carga inicial', now(), now(), now()),
    (24, 'INBOUND', 11, 'SEED', 'Carga inicial', now(), now(), now())
on conflict do nothing;
