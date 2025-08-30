#!/bin/bash

# YouTube API Java サンプル実行スクリプト

echo "=== YouTube API Java サンプル実行環境 ==="
echo

# Javaのバージョンを確認
echo "Javaのバージョンを確認中..."
java -version
echo

# Mavenのバージョンを確認
echo "Mavenのバージョンを確認中..."
mvn -version
echo

# 依存関係をダウンロード
echo "依存関係をダウンロード中..."
mvn clean compile
echo

# 実行可能なサンプルを表示
echo "実行可能なサンプル:"
echo "1. SimpleQuickstart (APIキー使用) - 推奨"
echo "2. Quickstart (OAuth2認証)"
echo "3. Search (動画検索)"
echo "4. MyUploads (アップロード動画一覧)"
echo

# 引数でサンプルを指定
if [ $# -eq 0 ]; then
    echo "SimpleQuickstartを実行します..."
    mvn exec:java
else
    case $1 in
        "1"|"simple")
            echo "SimpleQuickstartを実行します..."
            mvn exec:java
            ;;
        "2"|"oauth")
            echo "Quickstart (OAuth2)を実行します..."
            mvn exec:java -Dexec.mainClass="com.google.api.services.samples.youtube.cmdline.data.Quickstart"
            ;;
        "3"|"search")
            echo "Searchサンプルを実行します..."
            mvn exec:java -Dexec.mainClass="com.google.api.services.samples.youtube.cmdline.data.Search"
            ;;
        "4"|"uploads")
            echo "MyUploadsサンプルを実行します..."
            mvn exec:java -Dexec.mainClass="com.google.api.services.samples.youtube.cmdline.data.MyUploads"
            ;;
        *)
            echo "使用方法: $0 [1|simple|2|oauth|3|search|4|uploads]"
            echo "引数なしの場合はSimpleQuickstartを実行します"
            ;;
    esac
fi
