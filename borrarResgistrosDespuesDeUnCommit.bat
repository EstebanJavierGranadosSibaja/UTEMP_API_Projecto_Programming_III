@echo off
REM Mostrar el estado actual
git status

REM Hacer un hard reset al commit especificado
git reset --soft 96621c62aadc81438247b4c2c40f06eca6e5ad16

echo Los registros despues del commit han sido borrados, pero los cambios estan en tu directorio de trabajo.
pause
