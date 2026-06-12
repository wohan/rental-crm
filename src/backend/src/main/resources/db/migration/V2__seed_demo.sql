INSERT INTO properties (name, type, address, city, area, rooms, latitude, longitude, monthly_rent, status)
VALUES
('Квартира на Тверской', 'APARTMENT', 'Москва, Тверская ул., 10', 'Москва', 54.5, 2, 55.7601, 37.6187, 110000, 'OCCUPIED'),
('Склад Юг', 'WAREHOUSE', 'Москва, Варшавское ш., 125', 'Москва', 180.0, NULL, 55.6154, 37.6067, 220000, 'OCCUPIED'),
('Студия у метро', 'APARTMENT', 'Санкт-Петербург, Лиговский пр., 80', 'Санкт-Петербург', 28.0, 1, 59.9207, 30.3552, 52000, 'VACANT');

INSERT INTO tenants (full_name, phone, email, legal_type, inn, notes)
VALUES
('Иван Петров', '+79990001122', 'ivan.petrov@example.ru', 'INDIVIDUAL', NULL, 'Платит через СБП'),
('ООО Ромашка', '+74951234567', 'finance@romashka.example', 'LEGAL_ENTITY', '7701234567', 'Нужны акты ежемесячно');

INSERT INTO leases (property_id, tenant_id, start_date, end_date, monthly_rent, deposit_amount, payment_day, status, contract_number)
VALUES
(1, 1, CURRENT_DATE - INTERVAL '8 months', CURRENT_DATE + INTERVAL '4 months', 110000, 110000, 5, 'ACTIVE', 'Ж-001/2026'),
(2, 2, CURRENT_DATE - INTERVAL '3 months', CURRENT_DATE + INTERVAL '9 months', 220000, 220000, 10, 'ACTIVE', 'К-014/2026');

INSERT INTO payments (lease_id, due_date, paid_at, amount, status, method, comment)
VALUES
(1, date_trunc('month', CURRENT_DATE)::date + 4, now() - INTERVAL '3 days', 110000, 'PAID', 'SBP', 'Июнь оплачен'),
(1, date_trunc('month', CURRENT_DATE + INTERVAL '1 month')::date + 4, NULL, 110000, 'PLANNED', 'MANUAL', 'Следующий платеж'),
(2, date_trunc('month', CURRENT_DATE)::date + 9, NULL, 220000, 'OVERDUE', 'BANK_TRANSFER', 'Ожидаем оплату');

INSERT INTO maintenance_requests (property_id, tenant_id, title, description, priority, status, cost_estimate, due_date)
VALUES
(1, 1, 'Проверить протечку под раковиной', 'Арендатор прислал фото, нужна диагностика сантехника.', 'HIGH', 'IN_PROGRESS', 6000, CURRENT_DATE + 2),
(2, 2, 'Замена ламп на погрузочной зоне', 'Согласовать время с кладовщиком.', 'MEDIUM', 'OPEN', 12000, CURRENT_DATE + 5);
