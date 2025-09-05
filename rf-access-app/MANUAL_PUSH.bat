@echo off
echo ========================================
echo RF Access - Manual GitHub Push
echo ========================================
echo.

cd /d "C:\Users\LT319\CascadeProjects\Mifare App\rf-access-app"

echo Configuring Git...
git config --global user.name "LT319"
git config --global user.email "lt319@example.com"

echo.
echo Adding files...
git add .

echo.
echo Committing...
git commit -m "RF Access - Complete system ready for auto-build"

echo.
echo Adding remote...
git remote add origin https://github.com/LT319/RF-Access.git

echo.
echo Setting main branch...
git branch -M main

echo.
echo Pushing to GitHub...
git push -u origin main

echo.
echo ========================================
echo Push complete! Check GitHub for build status:
echo https://github.com/LT319/RF-Access/actions
echo ========================================
pause
