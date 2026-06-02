@echo off
REM ============================================================
REM Backup diario de la base de datos UD Marketplace (Windows)
REM Uso: backup.bat
REM Task Scheduler: crear tarea diaria a las 2:00 AM
REM ============================================================

REM -- Configuracion ------------------------------------------
set DB_NAME=marketplace
set DB_USER=root
set DB_PASS=root
set DB_HOST=localhost
set DB_PORT=3306
set RETENTION_DAYS=7

REM -- Directorio y timestamp ---------------------------------
set BACKUP_DIR=%~dp0backups
set TIMESTAMP=%date:~6,4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_FILE=%BACKUP_DIR%\%DB_NAME%_%TIMESTAMP%.sql

REM -- Crear directorio si no existe --------------------------
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo [%date% %time%] Iniciando backup de '%DB_NAME%'...

REM -- Ejecutar mysqldump -------------------------------------
mysqldump ^
    --host=%DB_HOST% ^
    --port=%DB_PORT% ^
    --user=%DB_USER% ^
    --password=%DB_PASS% ^
    --single-transaction ^
    --routines ^
    --triggers ^
    --databases %DB_NAME% > "%BACKUP_FILE%"

if %ERRORLEVEL% equ 0 (
    echo [%date% %time%] Backup exitoso: %BACKUP_FILE%
) else (
    echo [%date% %time%] ERROR: Fallo el backup de '%DB_NAME%'
    exit /b 1
)

REM -- Rotacion: eliminar backups mayores a N dias ------------
echo [%date% %time%] Eliminando backups con mas de %RETENTION_DAYS% dias...
forfiles /P "%BACKUP_DIR%" /M *.sql /D -%RETENTION_DAYS% /C "cmd /c del @file" 2>nul

echo [%date% %time%] Backup completado.
