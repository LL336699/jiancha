@echo off
chcp 936 >nul
title JIANCHA - 部署新后端并重启
net session >nul 2>&1
if errorlevel 1 goto :no_admin

echo ============================================================
echo   纪检监察助手 · 部署 P4 后端并重启服务
echo ============================================================
echo.

set "SRC=D:\CodeWork\纪检监察\jiancha\RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar"
set "DST=C:\Users\pc\jcmysql\backend-svc\ruoyi-admin.jar"
set "SVC=C:\Users\pc\jcmysql\backend-svc\jiancha-backend.exe"

if not exist "%SRC%" goto :no_jar

echo [1/4] 停止后端服务...
"%SVC%" stop
ping -n 7 127.0.0.1 >nul

echo [2/4] 复制新 jar...
copy /Y "%SRC%" "%DST%" >nul
if errorlevel 1 goto :copy_fail
echo        完成: %DST%

echo [3/4] 启动后端服务...
"%SVC%" start
echo [4/4] 等待服务就绪...
ping -n 25 127.0.0.1 >nul

echo.
echo ============================================================
echo   完成。后端日志: C:\Users\pc\jcmysql\backend.log
echo   探活: http://127.0.0.1:8080/captchaImage
echo ============================================================
echo.
pause
exit /b 0

:no_admin
echo [错误] 本脚本需要管理员权限。
echo        请右键此文件，选择"以管理员身份运行"。
echo.
pause
exit /b 1

:no_jar
echo [错误] 未找到打包好的 jar：
echo        %SRC%
echo        请先执行: mvn -pl ruoyi-admin -am package -DskipTests
echo.
pause
exit /b 1

:copy_fail
echo [错误] 复制 jar 失败，请确认后端服务已完全停止。
echo.
pause
exit /b 1
