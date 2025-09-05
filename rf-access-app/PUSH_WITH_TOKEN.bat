@echo off
echo ========================================
echo RF Access - Push with Personal Token
echo ========================================
echo.

cd /d "C:\Users\LT319\CascadeProjects\Mifare App\rf-access-app"

echo Step 1: Add all files
git add .

echo.
echo Step 2: Commit changes
git commit -m "RF Access - Complete system ready for Codespaces build"

echo.
echo Step 3: Push to GitHub
echo When prompted for password, paste your Personal Access Token
echo Username: LT319
echo Password: [PASTE YOUR TOKEN HERE]
echo.

git push -u origin main

echo.
echo ========================================
echo Success! Now try Codespaces at:
echo https://github.com/LT319/RF-Access
echo ========================================

pause
