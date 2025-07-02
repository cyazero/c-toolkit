@echo off
REM 默认启动GUI界面
if "%1"=="" (
    java -jar %~dp0/c-toolkit-1.0-SNAPSHOT.jar
    exit /b
)

REM 启动CLI界面
if /i "%1"=="--cli" (
    java -jar %~dp0/c-toolkit-1.0-SNAPSHOT.jar --cli
    exit /b
)

REM 直接执行命令
java -jar %~dp0/c-toolkit-1.0-SNAPSHOT.jar --cli %*