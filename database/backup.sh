#!/bin/bash
# ============================================================
# Backup diario de la base de datos UD Marketplace
# Uso: bash backup.sh
# Cron (diario a las 2:00 AM):
#   0 2 * * * /ruta/al/proyecto/database/backup.sh >> /var/log/ud_marketplace_backup.log 2>&1
# ============================================================

# ── Configuración ──────────────────────────────────────
DB_NAME="marketplace"
DB_USER="root"
DB_PASS="root"
DB_HOST="localhost"
DB_PORT="3306"

BACKUP_DIR="$(dirname "$0")/backups"
RETENTION_DAYS=7
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql.gz"

# ── Crear directorio si no existe ──────────────────────
mkdir -p "$BACKUP_DIR"

# ── Ejecutar mysqldump con compresión gzip ─────────────
echo "[$(date)] Iniciando backup de '${DB_NAME}'..."

mysqldump \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USER" \
    --password="$DB_PASS" \
    --single-transaction \
    --routines \
    --triggers \
    --databases "$DB_NAME" \
    | gzip > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "[$(date)] Backup exitoso: $BACKUP_FILE ($SIZE)"
else
    echo "[$(date)] ERROR: Falló el backup de '${DB_NAME}'"
    exit 1
fi

# ── Rotación: eliminar backups mayores a N días ────────
echo "[$(date)] Eliminando backups con más de ${RETENTION_DAYS} días..."
find "$BACKUP_DIR" -name "*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete

echo "[$(date)] Backup completado."
