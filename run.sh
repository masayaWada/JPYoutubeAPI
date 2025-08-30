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
echo "1. YouTubeChannelSearcher (APIキー使用) - 推奨"
echo

# 引数でサンプルを指定
if [ $# -eq 0 ]; then
    echo "YouTubeChannelSearcherを実行します..."
    mvn exec:java
else
    case $1 in
        "1"|"demo")
            echo "YouTubeChannelSearcherを実行します..."
            mvn exec:java
            ;;
        *)
            echo "使用方法: $0 [1|demo]"
            echo "引数なしの場合はYouTubeChannelSearcherを実行します"
            ;;
    esac
fi
