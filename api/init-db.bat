@echo off
echo Initializing database...
cd /d "d:\project\elderly-care-api"
mysql -u root -pGXJgxj.060811 elderly_care < src\main\resources\data-init.sql
echo Database initialization completed.
pause
