@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo === YouTube API Java Demo Application ===
echo.

REM Javaのバージョンを確認
echo Javaのバージョンを確認中...
java -version 2>&1
if %errorlevel% neq 0 (
    echo エラー: Javaがインストールされていません
    echo https://www.java.com からJavaをダウンロードしてください
    pause
    exit /b 1
)
echo.

REM Mavenのバージョンを確認
echo Mavenのバージョンを確認中...
mvn -version
if %errorlevel% neq 0 (
    echo エラー: Mavenがインストールされていません
    echo https://maven.apache.org からMavenをダウンロードしてください
    pause
    exit /b 1
)
echo.

REM 依存関係をダウンロード
echo 依存関係をダウンロード中...
mvn clean compile
if %errorlevel% neq 0 (
    echo エラー: コンパイルに失敗しました
    pause
    exit /b 1
)
echo.

REM 実行可能なサンプルを表示
echo 実行可能なサンプル:
echo 1. YouTubeChannelSearcher (APIキー使用) - 推奨
echo.

REM 引数でサンプルを指定
if "%~1"=="" (
    echo YouTubeChannelSearcherを実行します...
    mvn exec:java
) else (
    if "%~1"=="1" (
        echo YouTubeChannelSearcherを実行します...
        mvn exec:java
    ) else if "%~1"=="demo" (
        echo YouTubeChannelSearcherを実行します...
        mvn exec:java
    ) else (
        echo 使用方法: %0 [1^|demo]
        echo 引数なしの場合はYouTubeChannelSearcherを実行します
    )
)

echo.
echo 実行完了
pause
