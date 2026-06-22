@echo off
title NRO SERVER

cd /d "%~dp0"
java -Dfile.encoding=UTF-8 -Xms4G -Xmx6G -Xss512k -XX:+UseZGC -jar "dist\NgocRongOnline.jar"

pause