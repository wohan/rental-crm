# RentCRM

Вертикальная CRM для малых арендодателей 5-50 объектов: объекты, арендаторы, договоры, платежи, заявки, документы и сводная аналитика.

## Стек

- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway.
- DB: PostgreSQL 16.
- Frontend: React, TypeScript, MobX, Vite, Nginx.
- Deployment: Docker Compose.

## Быстрый запуск

```bash
cp .env.example .env
docker compose up -d --build
```

После запуска приложение доступно на `http://localhost` или на порту из `HTTP_PORT`.

## Production notes

1. Заменить `POSTGRES_PASSWORD` и `APP_JWT_SECRET`.
2. Поставить HTTPS перед frontend/nginx или на внешнем reverse proxy.
3. Настроить регулярный backup volume `postgres_data`.
4. Указать корректный `APP_CORS_ORIGINS`.
5. Разместить политику обработки персональных данных, оферту и пользовательское соглашение.

## Документация

Подробная аналитика и SDD: [docs/SDD.md](docs/SDD.md).
