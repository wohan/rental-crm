# SDD: Rental CRM

Версия: 0.1.0. Дата: 2026-06-12.

## 1. Scope

Система автоматизирует учет аренды для владельцев 5-50 объектов в РФ: объекты, арендаторы, договоры, платежи, ремонты, напоминания, аналитика доходности. MVP реализуется как web-приложение; мобильное приложение React Native планируется post-MVP для арендаторов и выездных работ.

## 2. Architecture

Технологии:

- Frontend: TypeScript, React, MobX, Vite.
- Backend: Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Flyway.
- DB: PostgreSQL 16 + PostGIS.
- Infra: Docker Compose.
- Integrations: ЮKassa/СБП, SMS, Email, Telegram, later 1С/ЭДО.

### C4 Context

```text
[Landlord/Manager] -> [Rental CRM Web App]
[Tenant] -> [Tenant Portal/Post-MVP]
[Rental CRM] -> [ЮKassa/СБП]
[Rental CRM] -> [SMS Provider]
[Rental CRM] -> [Email Provider]
[Rental CRM] -> [Telegram Bot API]
[Rental CRM] -> [Fiscalization Provider]
[Rental CRM] -> [PostgreSQL/PostGIS]
```

### C4 Container

```text
Browser
  -> React+MobX SPA
      -> REST API over HTTPS
Spring Boot API
  -> JPA repositories
  -> Payment service
  -> Reminder scheduler
  -> Notification adapters
  -> PostgreSQL/PostGIS
```

### C4 Components

```text
controller/
  DashboardController, PropertyController, TenantController, LeaseController, PaymentController, MaintenanceController
service/
  DashboardService, PaymentService, ReminderDispatcher, ScheduledJobs
repository/
  JPA repositories per aggregate
model/
  RentalProperty, Tenant, Lease, Payment, MaintenanceRequest, Reminder
frontend/
  api/client.ts, stores/RootStore.ts, dashboard components
```

## 3. Data Model / ERD

```text
properties 1--N leases N--1 tenants
leases 1--N payments
properties 1--N maintenance_requests
tenants 0--N maintenance_requests
leases 0--N reminders
tenants 0--N reminders
```

Tables:

- `properties`: id, name, type, address, city, area, rooms, latitude, longitude, cadastral_number, monthly_rent, status, timestamps.
- `tenants`: id, full_name, phone, email, legal_type, inn, passport_encrypted, notes.
- `leases`: id, property_id, tenant_id, start_date, end_date, monthly_rent, deposit_amount, payment_day, status, contract_number, contract_file_url.
- `payments`: id, lease_id, due_date, paid_at, amount, status, method, fiscal_receipt_id, provider_payment_id, comment.
- `maintenance_requests`: id, property_id, tenant_id, title, description, priority, status, cost_estimate, due_date.
- `reminders`: id, lease_id, tenant_id, type, channel, scheduled_at, sent_at, status, payload.

PostGIS usage: store current MVP latitude/longitude for simplicity; post-MVP add `geography(Point,4326)` generated or explicit column for geospatial queries.

## 4. API Specification

Base path: `/api`.

### Dashboard

- `GET /dashboard`
- Response: portfolio totals, revenue, receivable, upcoming payments, expiring leases, maintenance queue.

### Properties

- `GET /properties`
- `GET /properties/{id}`
- `POST /properties`
- `PUT /properties/{id}`

Payload:

```json
{
  "name": "Квартира на Тверской",
  "type": "APARTMENT",
  "address": "Москва, Тверская ул., 10",
  "city": "Москва",
  "area": 54.5,
  "rooms": 2,
  "monthlyRent": 110000,
  "status": "OCCUPIED"
}
```

### Tenants

- `GET /tenants`
- `GET /tenants/{id}`
- `POST /tenants`

### Leases

- `GET /leases`
- `POST /leases`

### Payments

- `GET /payments`
- `PATCH /payments/{id}/paid`

Payload:

```json
{ "method": "SBP", "comment": "Оплачено из кабинета" }
```

### Maintenance

- `GET /maintenance`
- `POST /maintenance`

## 5. UI/UX

MVP screens:

- Dashboard: KPI cards, overdue payments, upcoming payments, expiring leases, repair queue.
- Objects: table/cards, filters by status/type/city, create/edit modal.
- Tenants: contact list, legal type, related leases/payments.
- Leases: timeline, contract metadata, renewal alert, file links.
- Payments: calendar/list, status, payment method, "mark paid", provider payment id.
- Maintenance: kanban/list with status, priority, due date, cost.
- Settings: organization, users/roles, notification templates, payment provider settings.

Design principles: dense operational UI, no marketing hero; 8px cards, predictable tables, icon buttons for actions, no nested cards.

## 6. Security Design

MVP code ships without full authentication to keep local compose runnable; production hardening requires:

- Spring Security with JWT session tokens and refresh tokens.
- RBAC: owner, manager, accountant, technician, tenant.
- Password hashing with Argon2/bcrypt.
- HTTPS termination at reverse proxy.
- Encryption at rest for `passport_encrypted`, object storage documents, backups.
- Audit log for PII reads/writes and payment changes.
- Rate limiting on auth, payment webhook, public tenant links.
- Secret management through environment variables, not repository files.
- 152-ФЗ: РФ data residency, consent tracking, privacy policy, data retention, subject request workflow.
- 54-ФЗ: fiscal receipt integration for ИП/ЮЛ payments where required.

## 7. Integration Design

### Payments: ЮKassa/СБП

Flow:

1. Create payment link for planned payment.
2. Store `provider_payment_id`, status `PENDING`.
3. Receive webhook, validate signature/idempotency key.
4. Mark payment `PAID`, store `paid_at`.
5. If fiscalization enabled, send receipt payload and store `fiscal_receipt_id`.

### SMS

Adapter interface: `sendSms(phone, template, variables)`. Providers: SMS.ru, МТС Exolve, Voximplant. Store delivery id and status post-MVP.

### Email

SMTP or transactional provider. Templates for payment due, overdue, lease renewal, maintenance update.

### Telegram

Bot sends notifications to landlord/managers; tenant Telegram linking through one-time token.

## 8. Async Jobs

- Daily 06:10: mark planned payments with past due date as overdue.
- Every 5 minutes: dispatch due reminders.
- Monthly: generate next month payment schedule.
- Nightly: backup verification and stale webhook reconciliation.
- Post-MVP: document generation queue, import jobs, bank statement matching.

## 9. CI/CD

Pipeline:

1. Checkout.
2. Backend: `mvn test`, `mvn package`.
3. Frontend: `npm ci`, `npm run build`.
4. Build Docker images.
5. Run compose smoke test with healthcheck.
6. Push tagged images.
7. Deploy to VPS/Kubernetes with DB migrations through app startup or controlled migration job.

Branch policy: PR review, required tests, migration review for destructive DB changes.

## 10. Monitoring

- Spring Actuator: `/actuator/health`, `/actuator/metrics`.
- Logs: structured JSON in prod, request id, user id, organization id.
- Metrics: API latency, error rate, DB pool, payment webhook failures, notification failures, overdue job updates.
- Alerts: API down, DB down, payment webhook error spike, reminder queue lag.
- Backups: daily logical dump, PITR where available, restore drill monthly.

## 11. Deployment

Local:

```bash
docker compose up
```

Services:

- Frontend: http://localhost:15173
- Backend API: http://localhost:18080/api
- Actuator health: http://localhost:18080/actuator/health
- PostgreSQL/PostGIS: internal Compose service `db:5432` (not exposed on host by default)

Production additions:

- reverse proxy with TLS;
- managed object storage for documents;
- separate prod DB credentials;
- backup volume or managed PostgreSQL;
- auth enabled;
- provider credentials for payments/notifications/fiscalization.

## 12. Acceptance Criteria

- `docker compose up` starts DB, backend, frontend.
- Flyway creates schema and demo data.
- Dashboard loads metrics and lists.
- Payment can be marked as paid from UI/API.
- Backend unit tests cover payment transitions and dashboard aggregation.
- Docs define MVP, post-MVP, legal/security, integrations, CI/CD, monitoring.
