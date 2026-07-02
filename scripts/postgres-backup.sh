#!/bin/sh
set -eu

log() {
  printf '%s %s
' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')" "$*"
}

require_number() {
  value="$1"
  name="$2"
  case "$value" in
    ''|*[!0-9]*)
      log "ERROR: $name must be a positive integer, got '$value'"
      exit 1
      ;;
  esac
  if [ "$value" -lt 1 ]; then
    log "ERROR: $name must be greater than zero"
    exit 1
  fi
}

backup_once() {
  db="${POSTGRES_DB:-rentcrm}"
  host="${POSTGRES_HOST:-postgres}"
  port="${POSTGRES_PORT:-5432}"
  user="${POSTGRES_USER:-rentcrm}"
  backup_dir="${BACKUP_DIR:-/backups}"
  keep_last="${BACKUP_KEEP_LAST:-2}"
  require_number "$keep_last" "BACKUP_KEEP_LAST"

  mkdir -p "$backup_dir"
  timestamp="$(date -u '+%Y%m%dT%H%M%SZ')"
  target="$backup_dir/${db}_${timestamp}.dump"
  tmp="$target.tmp"

  log "Starting Postgres backup db=$db host=$host port=$port target=$target"
  PGPASSWORD="${POSTGRES_PASSWORD:-}" pg_dump     --host="$host"     --port="$port"     --username="$user"     --dbname="$db"     --format=custom     --no-owner     --no-acl     --file="$tmp"
  mv "$tmp" "$target"
  gzip -f "$target"
  log "Postgres backup finished file=$target.gz"

  index=0
  for file in $(ls -1t "$backup_dir"/${db}_*.dump.gz 2>/dev/null || true); do
    index=$((index + 1))
    if [ "$index" -gt "$keep_last" ]; then
      rm -f "$file"
      log "Deleted old Postgres backup file=$file"
    fi
  done
}

interval="${BACKUP_INTERVAL_SECONDS:-86400}"
require_number "$interval" "BACKUP_INTERVAL_SECONDS"

if [ "${BACKUP_RUN_ON_START:-true}" = "true" ]; then
  backup_once
else
  log "Initial backup skipped because BACKUP_RUN_ON_START=false"
fi

while true; do
  log "Next Postgres backup in ${interval}s"
  sleep "$interval"
  backup_once
done
