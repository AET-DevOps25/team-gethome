@echo off
REM Setup environment variables for GetHome services

echo Setting up environment variables for GetHome services...

REM Create .env file from example if it doesn't exist
if not exist .env (
    echo Creating .env file from env.example...
    copy env.example .env
    echo ✅ .env file created successfully!
    echo ⚠️  Please review and update the .env file with your actual credentials
) else (
    echo ✅ .env file already exists
)

REM Load environment variables for current session
if exist .env (
    echo Loading environment variables...
    for /f "tokens=1,2 delims==" %%a in (.env) do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
    echo ✅ Environment variables loaded successfully!
) else (
    echo ❌ .env file not found. Please create it first.
    exit /b 1
)

echo.
echo 🎉 Environment setup complete!
echo You can now run the services with:
echo   gradlew.bat bootRun (for each service)
echo.
echo Remember to:
echo   1. Never commit the .env file to version control
echo   2. Update the .env file with your actual credentials
echo   3. Use different credentials for different environments

pause 