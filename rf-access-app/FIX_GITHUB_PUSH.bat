@echo off
echo ========================================
echo RF Access - Fix GitHub Push Issue
echo ========================================
echo.

cd /d "C:\Users\LT319\CascadeProjects\Mifare App\rf-access-app"

echo Step 1: Check Git status
git status

echo.
echo Step 2: Re-initialize if needed
git init

echo.
echo Step 3: Configure Git user
git config --global user.name "LT319"
git config --global user.email "lt319@example.com"

echo.
echo Step 4: Add all files
git add .

echo.
echo Step 5: Commit changes
git commit -m "RF Access - Complete system with Android app and admin portal"

echo.
echo Step 6: Add remote (may already exist)
git remote remove origin 2>nul
git remote add origin https://github.com/LT319/RF-Access.git

echo.
echo Step 7: Push to GitHub
echo You may need to authenticate with GitHub...
git push -u origin main --force

echo.
echo ========================================
echo If authentication fails, you need to:
echo 1. Generate a Personal Access Token at:
echo    https://github.com/settings/tokens
echo 2. Use token as password when prompted
echo ========================================

pause
