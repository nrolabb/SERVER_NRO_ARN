@echo off
title NRO SERVER

cd /d "%~dp0"

rem Chạy từ một bản JAR riêng để ant jar có thể build lại dist\NgocRongOnline.jar
rem mà không làm hỏng class loader của server đang hoạt động.
set "RUNTIME_JAR=dist\NgocRongOnline.runtime.%RANDOM%.jar"
copy /Y "dist\NgocRongOnline.jar" "%RUNTIME_JAR%" >nul
if errorlevel 1 (
    echo Khong the tao runtime JAR: %RUNTIME_JAR%
    pause
    exit /b 1
)

java -Dfile.encoding=UTF-8 -Xms4G -Xmx6G -Xss512k -XX:+UseZGC -jar "%RUNTIME_JAR%"
del /Q "%RUNTIME_JAR%" >nul 2>&1

pause
