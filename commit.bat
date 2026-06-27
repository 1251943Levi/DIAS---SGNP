@echo off
REM ============================================================
REM  Commit rapido do projeto SGNP/DIAS
REM  Uso:  commit.bat "mensagem do commit"
REM  Se nao passares mensagem, usa uma por omissao.
REM ============================================================

cd /d "%~dp0"

set "MSG=%~1"
if "%MSG%"=="" set "MSG=Opcao 2: cargas-template sem portos; copia herda portos da viagem + templates de exemplo"

echo.
echo === Estado atual ===
git status -s

echo.
echo === A adicionar alteracoes ===
git add -A

echo.
echo === A fazer commit ===
git commit -m "%MSG%"

echo.
echo === Concluido. Para enviar para o repositorio remoto: git push ===
pause
