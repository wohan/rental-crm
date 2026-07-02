# Postgres backup and restore

Дата: 2026-06-28

## Что реализовано

В проект добавлен отдельный Docker Compose сервис `postgres-backup`.

Он:

1. Ждет, пока `postgres` станет healthy.
2. Выполняет `pg_dump` в custom format (`.dump`).
3. Сжимает дамп в `.dump.gz`.
4. Складывает файлы в Docker volume `postgres_backups`.
5. Хранит только последние `BACKUP_KEEP_LAST` дампа, по умолчанию 2.
6. Более старые дампы автоматически удаляет.

Это логический backup Postgres. Он подходит для SaaS MVP/раннего production, потому что прост в эксплуатации и восстановлении.

## Почему pg_dump

Общепринятые механизмы для Postgres:

- `pg_dump` / `pg_restore` — логические дампы. Хорошо подходят для небольших и средних баз, просты для Docker Compose.
- filesystem snapshot / volume snapshot — быстрые снимки диска, требуют аккуратной настройки и инфраструктуры.
- WAL archiving + base backup — полноценный PITR, позволяет восстановиться на момент времени, но сложнее в сопровождении.
- managed backup у облачного Postgres — лучший вариант для зрелого production, если база вынесена в managed DB.

Для текущего проекта выбран `pg_dump` в отдельном контейнере: он дешевый, понятный, переносимый и не требует отдельной инфраструктуры.

## Настройки

В `.env` можно задать:

```env
BACKUP_INTERVAL_SECONDS=86400
BACKUP_KEEP_LAST=2
BACKUP_RUN_ON_START=true
```

Значения:

- `BACKUP_INTERVAL_SECONDS=86400` — бэкап раз в сутки.
- `BACKUP_KEEP_LAST=2` — хранить только 2 последних бэкапа.
- `BACKUP_RUN_ON_START=true` — делать первый бэкап сразу после старта контейнера.

Если нужно делать backup не сразу при старте, а только после первого интервала:

```env
BACKUP_RUN_ON_START=false
```

## Где лежат бэкапы

В Docker volume:

```bash
docker volume ls | grep postgres_backups
```

Посмотреть файлы:

```bash
docker compose exec postgres-backup ls -lah /backups
```

Формат имени:

```text
rentcrm_20260628T120000Z.dump.gz
```

## Ручной запуск backup

Можно выполнить одноразовый backup той же логикой:

```bash
docker compose exec postgres-backup sh -c 'BACKUP_RUN_ON_START=true BACKUP_INTERVAL_SECONDS=999999999 /usr/local/bin/postgres-backup.sh'
```

Но эта команда уйдет в долгий sleep после первого дампа. Для ручного production-запуска проще временно перезапустить сервис:

```bash
docker compose restart postgres-backup
```

При `BACKUP_RUN_ON_START=true` он сразу создаст новый backup и применит ротацию.

## Восстановление

Восстановление лучше делать на остановленном backend, чтобы приложение не писало в БД во время restore.

1. Остановить backend:

```bash
docker compose stop backend
```

2. Посмотреть список backup-файлов:

```bash
docker compose exec postgres-backup ls -lah /backups
```

3. Восстановить выбранный файл. Пример:

```bash
docker compose exec -T postgres-backup sh -c '
  gzip -dc /backups/rentcrm_YYYYMMDDTHHMMSSZ.dump.gz |
  PGPASSWORD="$POSTGRES_PASSWORD" pg_restore     --host=postgres     --port=5432     --username="$POSTGRES_USER"     --dbname="$POSTGRES_DB"     --clean     --if-exists     --no-owner     --no-acl
'
```

4. Запустить backend:

```bash
docker compose up -d backend
```

5. Проверить health:

```bash
curl -fsS http://127.0.0.1:8080/actuator/health
```

## Проверка, что backup реально работает

После деплоя нужно проверить:

```bash
docker compose ps postgres-backup
docker compose logs --tail=80 postgres-backup
docker compose exec postgres-backup ls -lah /backups
```

Ожидаемый результат:

- сервис `postgres-backup` запущен;
- в логах есть `Postgres backup finished`;
- в `/backups` есть `.dump.gz` файл;
- файлов не больше `BACKUP_KEEP_LAST`.

## Ограничения

- Хранится только база Postgres. Файлы документов лежат в `document_storage`, их нужно бэкапить отдельно, если требуется полный disaster recovery.
- `pg_dump` не дает point-in-time recovery. Для зрелого production позже стоит добавить WAL archiving или вынести БД в managed Postgres с автоматическими backup.
- При `BACKUP_RUN_ON_START=true` частые перезапуски backup-сервиса будут создавать новые дампы и ротировать старые.

## Рекомендация на следующий этап

Для полноценной эксплуатации нужно добавить backup файлов документов:

- отдельный архив `document_storage`;
- ротация по тем же правилам;
- проверка восстановления файла и БД вместе.
