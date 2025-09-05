@echo off
echo ========================================
echo RF Access - GitHub Auto-Build Setup
echo ========================================
echo.

cd /d "C:\Users\LT319\CascadeProjects\Mifare App\rf-access-app"

echo Step 1: Initialize Git Repository
git init
git add .
git commit -m "Initial RF Access project - ready for auto-build"

echo.
echo Step 2: GitHub Repository Setup
echo ========================================
echo NEXT STEPS (Manual):
echo.
echo 1. Go to: https://github.com/new
echo 2. Repository name: rf-access
echo 3. Description: RF Access - Digital Access Cards
echo 4. Set to Public (for free GitHub Actions)
echo 5. Click "Create repository"
echo.
echo 6. Copy the repository URL (e.g., https://github.com/yourusername/rf-access.git)
echo 7. Run these commands:
echo.
echo    git remote add origin [YOUR_REPO_URL]
echo    git branch -M main
echo    git push -u origin main
echo.
echo ========================================
echo AUTO-BUILD PROCESS:
echo ========================================
echo.
echo ✅ Once pushed to GitHub:
echo   - GitHub Actions will automatically build APK
echo   - APK will be available in Actions tab
echo   - Releases will be created automatically
echo   - Download APK from Releases section
echo.
echo ✅ Your APK will be ready in ~5 minutes!
echo.
pause

echo.
echo Opening GitHub in browser...
start https://github.com/new

echo.
echo Repository is ready for GitHub!
echo Push to GitHub and your APK will build automatically.
pause
