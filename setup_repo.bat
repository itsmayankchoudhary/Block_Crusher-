@echo off
echo Block Crusher - Git Repository Setup
echo =====================================
echo.
echo This script will initialize a local Git repository and guide you through
echo connecting it to a remote GitHub repository.
echo.
echo Prerequisites:
echo   1. Git installed and available in PATH.
echo   2. A GitHub account with a new empty repository.
echo.
echo If you haven't created a GitHub repository yet, please do so now.
echo Visit: https://github.com/new
echo.
set /p proceed="Have you installed Git and created an empty GitHub repo? (y/n): "
if /i "%proceed%" neq "y" (
    echo Please complete the prerequisites and run this script again.
    pause
    exit /b 1
)

echo.
echo Step 1: Initializing local Git repository...
git init
if %errorlevel% neq 0 (
    echo Failed to initialize Git repository. Ensure Git is installed.
    pause
    exit /b 1
)

echo Step 2: Adding remote origin...
set /p remote_url="Enter your GitHub repository URL (HTTPS or SSH): "
git remote add origin "%remote_url%"
if %errorlevel% neq 0 (
    echo Failed to add remote origin. Check the URL.
    pause
    exit /b 1
)

echo Step 3: Staging files...
git add .
if %errorlevel% neq 0 (
    echo Failed to stage files.
    pause
    exit /b 1
)

echo Step 4: Committing initial files...
git commit -m "Initial commit: project structure and basic docs"
if %errorlevel% neq 0 (
    echo Failed to commit.
    pause
    exit /b 1
)

echo Step 5: Renaming default branch to 'main'...
git branch -M main
if %errorlevel% neq 0 (
    echo Failed to rename branch.
    pause
    exit /b 1
)

echo Step 6: Pushing to GitHub...
git push -u origin main
if %errorlevel% neq 0 (
    echo Push failed. Check your authentication (PAT or SSH).
    echo Follow the instructions in GIT_SETUP.md.
    pause
    exit /b 1
)

echo.
echo Success! Your repository is now set up and pushed to GitHub.
echo You can now proceed with game development.
pause