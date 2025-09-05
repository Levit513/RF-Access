@echo off
echo ========================================
echo RF Access - Admin Portal
echo ========================================
echo.

cd /d "C:\Users\LT319\CascadeProjects\Mifare App\rf-access-app\admin-portal"

echo Starting RF Access Admin Portal...
echo.
echo Admin Login: admin / admin123
echo Portal URL: http://localhost:5000
echo.

python app.py

pause
