@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=up"

set "COMPOSE_CMD=docker compose"
docker compose version >nul 2>&1
if errorlevel 1 (
  where docker-compose >nul 2>&1
  if errorlevel 1 (
    echo ERRO: docker compose ou docker-compose nao encontrado no PATH.
    exit /b 1
  )
  set "COMPOSE_CMD=docker-compose"
)

set "ROOT=%~dp0"
set "COMPOSE_DIR="
set "URL=http://localhost:8080/erp-web-war/login.xhtml"

for /r "%ROOT%" %%F in (docker-compose.yml) do (
  set "COMPOSE_DIR=%%~dpF"
  goto :FOUND
)

:FOUND
if "%COMPOSE_DIR%"=="" (
  echo ERRO: docker-compose.yml nao encontrado.
  exit /b 1
)

pushd "%COMPOSE_DIR%" >nul

if /i "%ACTION%"=="up" (
  call :RUN_UP
  goto :END
)

if /i "%ACTION%"=="ps" (
  %COMPOSE_CMD% ps
  goto :END
)

if /i "%ACTION%"=="logs" (
  %COMPOSE_CMD% logs -f
  goto :END
)

if /i "%ACTION%"=="down" (
  %COMPOSE_CMD% down -v --remove-orphans
  goto :END
)

if /i "%ACTION%"=="restart" (
  %COMPOSE_CMD% down -v --remove-orphans
  call :RUN_UP
  goto :END
)

if /i "%ACTION%"=="reset" (
  %COMPOSE_CMD% down -v --remove-orphans
  call :RUN_UP
  goto :END
)

if /i "%ACTION%"=="open" (
  start "" "%URL%"
  goto :END
)

exit /b 2

:RUN_UP
call :COMPOSE_UP
if errorlevel 1 (
  call :FIX_DOCKER_DNS
  call :RESTART_DOCKER
  call :COMPOSE_UP
  if errorlevel 1 (
    call :FIX_DOCKER_DNS
    call :RESTART_DOCKER
    call :COMPOSE_UP
    if errorlevel 1 exit /b 1
  )
)
for /l %%I in (1,1,60) do (
  powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri '%URL%' -Method Head -UseBasicParsing -TimeoutSec 2; exit 0 } catch { exit 1 }" >nul 2>&1 && goto :OPEN_URL
  timeout /t 1 /nobreak >nul
)
:OPEN_URL
start "" "%URL%"
echo %URL%
%COMPOSE_CMD% ps
exit /b 0

:COMPOSE_UP
%COMPOSE_CMD% up --build -d
exit /b %errorlevel%

:FIX_DOCKER_DNS
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$paths = @(); if ($env:USERPROFILE) { $paths += (Join-Path $env:USERPROFILE '.docker\daemon.json') }; if ($env:ProgramData) { $paths += (Join-Path $env:ProgramData 'docker\config\daemon.json') }; $paths = $paths | Select-Object -Unique; foreach ($path in $paths) { $dir = Split-Path $path -Parent; if (-not (Test-Path $dir)) { try { New-Item -ItemType Directory -Path $dir -Force | Out-Null } catch {} }; $json = @{}; if (Test-Path $path) { try { $raw = Get-Content -Raw -Path $path; if ($raw.Trim()) { $json = $raw | ConvertFrom-Json -AsHashtable } } catch { $json = @{} } }; if ($null -eq $json -or $json.GetType().Name -ne 'Hashtable') { $json = @{} }; $json['dns'] = @('1.1.1.1','8.8.8.8'); try { $json | ConvertTo-Json -Depth 100 | Set-Content -Path $path -Encoding UTF8 } catch {} }"
exit /b 0

:RESTART_DOCKER
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$svc = Get-Service -Name 'com.docker.service' -ErrorAction SilentlyContinue; if ($svc) { try { Restart-Service -Name 'com.docker.service' -Force -ErrorAction Stop } catch {} }; $desktop = Get-Process -Name 'Docker Desktop' -ErrorAction SilentlyContinue; if ($desktop) { try { $desktop | Stop-Process -Force -ErrorAction Stop } catch {} }; $candidates = @((Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'), (Join-Path ${env:ProgramFiles(x86)} 'Docker\Docker\Docker Desktop.exe')); foreach ($candidate in $candidates) { if ($candidate -and (Test-Path $candidate)) { Start-Process -FilePath $candidate; break } }; Start-Sleep -Seconds 8"
exit /b 0

:END
popd >nul
endlocal
