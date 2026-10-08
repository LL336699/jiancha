@echo off
chcp 936 >nul
title JIANCHA - 启动服务

rem ============================================================
rem  纪检监察助手 - 一键启动全部服务
rem  服务: jiancha-mysql / Redis / jiancha-backend / jiancha-frontend
rem  需要管理员权限（右键 -> 以管理员身份运行）
rem ============================================================

net session >nul 2>&1
if errorlevel 1 goto :no_admin

echo.
echo   ================================================
echo     纪检监察助手  --  启动全部服务
echo   ================================================
echo.

call :start_one "jiancha-mysql"    "数据库 MySQL 3306"
call :start_one "Redis"            "缓存 Redis 6379"

echo.
echo   [..] 等待数据库与缓存就绪，约 6 秒
ping -n 7 127.0.0.1 >nul
echo.

call :start_one "jiancha-backend"  "后端 Spring Boot 8080"
call :start_one "jiancha-frontend" "前端 Vite 88"

echo.
echo   ================================================
echo     完成
echo     访问地址  http://localhost:88
echo     初始账号  admin / admin123
echo   ================================================
echo.
pause
exit /b 0

:no_admin
echo.
echo   [!] 需要管理员权限
echo       请右键本文件，选择 以管理员身份运行
echo.
pause
exit /b 1

:start_one
sc query "%~1" >nul 2>&1
if errorlevel 1 goto :not_installed
net start "%~1" >nul 2>&1
if errorlevel 2 goto :already_running
if errorlevel 1 goto :start_failed
echo   [成功]   %~1   -- %~2
exit /b 0

:not_installed
echo   [跳过]   %~1   -- 服务未安装   -- %~2
exit /b 0

:already_running
echo   [已运行] %~1   -- %~2
exit /b 0

:start_failed
echo   [失败]   %~1 启动失败，请查看服务日志   -- %~2
exit /b 0
