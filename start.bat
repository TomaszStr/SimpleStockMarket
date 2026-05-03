@echo off

REM Check if port parameter is provided
if "%~1"=="" (
    echo Usage: start.bat ^<PORT^>
    echo Example: start.bat 8080
    exit /b 1
)

set APP_PORT=%~1
echo Starting Simple Stock Market on localhost:%APP_PORT%...

REM Build and start in detached mode
docker-compose up -d --build

echo Application is spinning up. Give it a few seconds to initialize the database and replicas.