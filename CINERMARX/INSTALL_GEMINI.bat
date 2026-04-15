@echo off
echo Installing Gemini CLI, thanks for your patience.
echo.

:: --- Download and install Chocolatey ---
echo Installing Chocolatey...
powershell -NoProfile -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
if %errorlevel% neq 0 (
    echo Error installing Chocolatey.
    pause
    exit /b 1
)
echo.

:: ---a Install Node.js ---
echo Installing Node.js version 24.11.0...
choco install nodejs --version=24.11.0 -y
if %errorlevel% neq 0 (
    echo Error installing Node.js.
    pause
    exit /b 1
)
echo.

:: --- Verify Node.js and npm ---
echo Verifying Node.js and npm installation...
node -v
npm -v
echo.

:: --- Install Gemini CLI globally ---
echo Installing Gemini CLI...
npm install -g @google/gemini-cli
if %errorlevel% equ 0 (
    echo Installation completed successfully!
) else (
    echo There was an error during installation.
)
echo.
pause
