@echo off
chcp 936 >nul
title JIANCHA - 停止服务

rem ============================================================
rem  纪检监察助手 - 一键停止全部服务
rem  停止顺序: 前端 -> 后端 -> 缓存 -> 数据库
rem  需要管理员权限（右键 -> 以管理员身份运行）
rem ============================================================

net session >nul 2>&1
if errorlevel 1 goto :no_admin

echo.
echo   ================================================
echo     纪检监察助手  --  停止全部服务
echo   ================================================
echo.

call :stop_one "jiancha-frontend" "前端 Vite 88"
call :stop_one "jiancha-backend"  "后端 Spring Boot 8080"
call :stop_one "Redis"            "缓存 Redis 6379"
call :stop_one "jiancha-mysql"    "数据库 MySQL 3306"

echo.
echo   ================================================
echo     已全部停止
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

:stop_one
sc query "%~1" >nul 2>&1
if errorlevel 1 goto :not_installed
net stop "%~1" >nul 2>&1
if errorlevel 2 goto :already_stopped
if errorlevel 1 goto :stop_failed
echo   [成功]   %~1   -- %~2
exit /b 0

:not_installed
echo   [跳过]   %~1   -- 服务未安装   -- %~2
exit /b 0

:already_stopped
echo   [已停止] %~1   -- %~2
exit /b 0

:stop_failed
echo   [失败]   %~1 停止失败   -- %~2
exit /b 0
