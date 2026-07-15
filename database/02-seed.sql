-- ShopWise - donnees de demonstration
-- Mot de passe commun des comptes commercants et clients : Shopwise123!

INSERT INTO businesses (name, email, phone, active)
VALUES
    ('Chez Marie', 'contact@chez-marie.local', '0102030405', TRUE),
    ('Studio Zen', 'contact@studio-zen.local', '0198765432', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO merchant_accounts (first_name, last_name, email, password_hash, active, created_at)
VALUES
    ('Marie', 'Dupont', 'owner@shopwise.local', '$2a$10$sqoa7mAB0XNWdAADjct3wuQA606JYSpUvKciSyLBWjvnJuIfP7mb6', TRUE, CURRENT_TIMESTAMP),
    ('Lucas', 'Moreau', 'manager@shopwise.local', '$2a$10$sqoa7mAB0XNWdAADjct3wuQA606JYSpUvKciSyLBWjvnJuIfP7mb6', TRUE, CURRENT_TIMESTAMP),
    ('Emma', 'Petit', 'staff@shopwise.local', '$2a$10$sqoa7mAB0XNWdAADjct3wuQA606JYSpUvKciSyLBWjvnJuIfP7mb6', TRUE, CURRENT_TIMESTAMP),
    ('Hugo', 'Leroy', 'owner.zen@shopwise.local', '$2a$10$sqoa7mAB0XNWdAADjct3wuQA606JYSpUvKciSyLBWjvnJuIfP7mb6', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO business_memberships (merchant_account_id, business_id, role, active)
SELECT ma.id, b.id, memberships.role, TRUE
FROM (VALUES
    ('owner@shopwise.local', 'contact@chez-marie.local', 'OWNER'),
    ('manager@shopwise.local', 'contact@chez-marie.local', 'MANAGER'),
    ('staff@shopwise.local', 'contact@chez-marie.local', 'STAFF'),
    ('owner.zen@shopwise.local', 'contact@studio-zen.local', 'OWNER')
) AS memberships(account_email, business_email, role)
JOIN merchant_accounts ma ON ma.email = memberships.account_email
JOIN businesses b ON b.email = memberships.business_email
ON CONFLICT (merchant_account_id, business_id) DO NOTHING;

INSERT INTO merchant_invitations (
    business_id, email, role, token, expires_at, status, created_by_id, created_at
)
SELECT b.id, 'invite@shopwise.local', 'STAFF', 'demo-invitation-token',
       CURRENT_TIMESTAMP + INTERVAL '48 hours', 'PENDING', ma.id, CURRENT_TIMESTAMP
FROM businesses b
JOIN merchant_accounts ma ON ma.email = 'owner@shopwise.local'
WHERE b.email = 'contact@chez-marie.local'
ON CONFLICT (token) DO NOTHING;

INSERT INTO clients (
    business_id, first_name, last_name, email, phone, active, created_at, updated_at
)
SELECT b.id, clients.first_name, clients.last_name, clients.email, clients.phone, TRUE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('contact@chez-marie.local', 'Alice', 'Martin', 'alice.martin@example.com', '0601010101'),
    ('contact@chez-marie.local', 'Thomas', 'Bernard', 'thomas.bernard@example.com', '0602020202'),
    ('contact@chez-marie.local', 'Chloe', 'Durand', 'chloe.durand@example.com', '0603030303'),
    ('contact@studio-zen.local', 'Nina', 'Roux', 'nina.roux@example.com', '0604040404')
) AS clients(business_email, first_name, last_name, email, phone)
JOIN businesses b ON b.email = clients.business_email
ON CONFLICT (email) DO NOTHING;

INSERT INTO customer_accounts (client_id, password_hash, active)
SELECT c.id, '$2a$10$sqoa7mAB0XNWdAADjct3wuQA606JYSpUvKciSyLBWjvnJuIfP7mb6', TRUE
FROM clients c
WHERE c.email IN ('alice.martin@example.com', 'nina.roux@example.com')
ON CONFLICT (client_id) DO NOTHING;

INSERT INTO services (business_id, name, description, duration_minutes, loyalty_points, active)
SELECT b.id, service_data.name, service_data.description,
       service_data.duration_minutes, service_data.loyalty_points, TRUE
FROM businesses b
CROSS JOIN (VALUES
    ('Conseil personnalisé', 'Conseil adapté aux besoins du client', 45, 25),
    ('Retrait de commande', 'Retrait et verification de la commande', 30, 10),
    ('Atelier découverte', 'Atelier collectif de découverte', 60, 40)
) AS service_data(name, description, duration_minutes, loyalty_points)
WHERE NOT EXISTS (
    SELECT 1 FROM services s WHERE s.business_id = b.id AND LOWER(s.name) = LOWER(service_data.name)
);

INSERT INTO appointments (
    business_id, client_id, service_id, created_by_account_id,
    starts_at, ends_at, status, comment, created_at, updated_at
)
SELECT b.id, c.id, s.id, ma.id,
       appointment_data.starts_at,
       appointment_data.starts_at + make_interval(mins => s.duration_minutes),
       appointment_data.status, appointment_data.comment, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('contact@chez-marie.local', 'alice.martin@example.com', 'Conseil personnalisé', 'owner@shopwise.local', CURRENT_TIMESTAMP - INTERVAL '7 days', 'HONORED', 'SEED-RDV-ALICE-HONORE'),
    ('contact@chez-marie.local', 'thomas.bernard@example.com', 'Retrait de commande', 'manager@shopwise.local', CURRENT_TIMESTAMP + INTERVAL '1 day', 'SCHEDULED', 'SEED-RDV-THOMAS-PLANIFIE'),
    ('contact@chez-marie.local', 'chloe.durand@example.com', 'Atelier découverte', 'staff@shopwise.local', CURRENT_TIMESTAMP - INTERVAL '2 days', 'CANCELED', 'SEED-RDV-CHLOE-ANNULE'),
    ('contact@studio-zen.local', 'nina.roux@example.com', 'Atelier découverte', 'owner.zen@shopwise.local', CURRENT_TIMESTAMP - INTERVAL '3 days', 'HONORED', 'SEED-RDV-NINA-HONORE')
) AS appointment_data(business_email, client_email, service_name, creator_email, starts_at, status, comment)
JOIN businesses b ON b.email = appointment_data.business_email
JOIN clients c ON c.email = appointment_data.client_email AND c.business_id = b.id
JOIN services s ON LOWER(s.name) = LOWER(appointment_data.service_name) AND s.business_id = b.id
JOIN merchant_accounts ma ON ma.email = appointment_data.creator_email
WHERE NOT EXISTS (
    SELECT 1 FROM appointments a WHERE a.comment = appointment_data.comment
);

INSERT INTO loyalty_accounts (client_id, points_balance, updated_at)
SELECT c.id, balances.points_balance, CURRENT_TIMESTAMP
FROM (VALUES
    ('alice.martin@example.com', 35),
    ('thomas.bernard@example.com', 15),
    ('nina.roux@example.com', 40)
) AS balances(client_email, points_balance)
JOIN clients c ON c.email = balances.client_email
ON CONFLICT (client_id) DO NOTHING;

INSERT INTO loyalty_transactions (
    loyalty_account_id, appointment_id, type, points_delta, reason, transaction_date
)
SELECT la.id, a.id, 'EARNED', s.loyalty_points, 'Appointment honored', a.ends_at
FROM appointments a
JOIN services s ON s.id = a.service_id
JOIN loyalty_accounts la ON la.client_id = a.client_id
WHERE a.status = 'HONORED'
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO loyalty_transactions (
    loyalty_account_id, appointment_id, type, points_delta, reason, transaction_date
)
SELECT la.id, NULL, adjustments.type, adjustments.points_delta,
       adjustments.reason, CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM (VALUES
    ('alice.martin@example.com', 'ADJUSTMENT', 10, 'SEED-CREDIT-BIENVENUE'),
    ('thomas.bernard@example.com', 'ADJUSTMENT', 15, 'SEED-CREDIT-BIENVENUE')
) AS adjustments(client_email, type, points_delta, reason)
JOIN clients c ON c.email = adjustments.client_email
JOIN loyalty_accounts la ON la.client_id = c.id
WHERE NOT EXISTS (
    SELECT 1
    FROM loyalty_transactions lt
    WHERE lt.loyalty_account_id = la.id AND lt.reason = adjustments.reason
);
