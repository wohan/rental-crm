# Error tracking and observability

Дата: 2026-06-15

## Что реализовано в приложении

1. В backend добавлена таблица `error_events`.
2. Неожиданные runtime-исключения перехватываются глобальным обработчиком.
3. Для каждой ошибки сохраняются:
   - `errorId`;
   - HTTP method/path;
   - email пользователя, если он авторизован;
   - `accountId`, если пользователь найден;
   - класс исключения;
   - сообщение;
   - полный stack trace;
   - timestamp.
4. Пользователю возвращается безопасный ответ с `errorId`, без stack trace.
5. Если задан `APP_ERROR_ALERT_EMAIL`, backend отправляет email-уведомление о runtime-ошибке через SMTP.
6. Для SaaS-админа добавлен endpoint `GET /api/admin/errors`.

## Логирование сервисов

1. CRUD-операции сущностей логируются на уровне `DEBUG`.
2. Критичные операции биллинга логируются на уровне `INFO`:
   - создание checkout;
   - применение оплаченного счета;
   - применение webhook платежного провайдера.
3. Критичные операции уведомлений логируются на уровне `INFO`:
   - ручной запуск отправки;
   - итоговое количество delivery-записей;
   - сбой отправки канала.
4. Детали отправки уведомлений по каналам логируются на уровне `DEBUG`.

## Внешний error tracking

Для промышленной эксплуатации рекомендуется подключить Sentry-compatible collector.

### GlitchTip

Сайт: https://glitchtip.com/

Документация установки: https://glitchtip.com/documentation/install

Почему подходит:

1. Open source.
2. Sentry-compatible SDK/API.
3. Устанавливается отдельным Docker Compose/подом.
4. Поддерживает email-уведомления и проекты.
5. Существенно легче для небольшого SaaS, чем полный Sentry self-hosted.

### Sentry

Self-hosted: https://develop.sentry.dev/self-hosted/

Spring Boot SDK: https://docs.sentry.io/platforms/java/guides/spring-boot/

Когда выбирать:

1. Нужны зрелые dashboards, issue grouping, releases, performance traces.
2. Есть ресурсы на сопровождение более тяжелой инфраструктуры.
3. Команда готова администрировать отдельный observability stack.

## Рекомендованная схема для RentCRM

1. Оставить встроенную таблицу `error_events` как локальный fallback и support-инструмент.
2. Поднять GlitchTip отдельным compose-профилем или на отдельном сервере.
3. Подключить Sentry-compatible Java SDK через DSN на следующем этапе.
4. Настроить email-алерты:
   - backend runtime error через `APP_ERROR_ALERT_EMAIL`;
   - grouping/alerts в GlitchTip/Sentry.
5. Не отправлять в error tracking пароли, токены, паспортные данные и секреты провайдеров.
