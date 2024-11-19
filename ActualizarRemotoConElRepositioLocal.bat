@echo off
REM Mostrar el estado actual
git status

REM Forzar el push al repositorio remoto
git push origin HEAD --force

echo El registro de commits ha sido actualizado forzosamente en el repositorio remoto.
pause
