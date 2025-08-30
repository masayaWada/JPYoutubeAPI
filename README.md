# YouTube API Demo

YouTube Data API v3を使用したシンプルなJavaデモアプリケーションです。

## 必要な環境

### 共通要件
- Java 11以上
- Maven 3.6以上
- YouTube Data API v3のAPIキー

### Windows環境
- Windows 10/11
- PowerShell または コマンドプロンプト
- UTF-8対応のターミナル（推奨）

### macOS/Linux環境
- macOS 10.15以上 または Linux
- Bash シェル

## セットアップ

1. このリポジトリをクローンまたはダウンロードします
2. YouTube Data API v3のAPIキーを取得します：
   - [Google Cloud Console](https://console.developers.google.com/project/_/apiui/credential)にアクセス
   - プロジェクトを作成または選択
   - YouTube Data API v3を有効化
   - APIキーを作成
3. 設定ファイルを作成します：
   ```bash
   # APIキー用の設定ファイルを作成
   cp src/main/resources/youtube.properties.example src/main/resources/youtube.properties
   # エディタでyoutube.propertiesを開き、YOUR_API_KEY_HEREを実際のAPIキーに置き換え
   ```
4. 依存関係をダウンロードします：
   ```bash
   mvn clean compile
   ```

## 実行方法

### Windows環境

#### 簡単な実行（推奨）
```cmd
run.bat
```

または

```cmd
run.bat demo
```

#### PowerShellでの実行
```powershell
.\run.bat
```

### macOS/Linux環境

#### 簡単な実行（推奨）
```bash
./run.sh
```

または

```bash
./run.sh demo
```

### その他の実行方法

#### Windows
```cmd
# デモアプリケーションを実行
run.bat demo
```

#### macOS/Linux
```bash
# デモアプリケーションを実行
./run.sh demo
```

### Mavenコマンドで直接実行

#### Windows
```cmd
# YouTubeChannelSearcher（APIキー使用）
mvn exec:java -Dexec.mainClass="YouTubeChannelSearcher"
```

#### macOS/Linux
```bash
# YouTubeChannelSearcher（APIキー使用）
mvn exec:java -Dexec.mainClass="YouTubeChannelSearcher"
```

## 利用可能な機能

- **YouTubeChannelSearcher**: APIキーを使用したシンプルなチャンネル検索機能
  - YouTubeチャンネルの検索
  - チャンネル情報の表示
  - 日本語対応

## 設定ファイル

- `src/main/java/YouTubeChannelSearcher.java`: メインアプリケーション
- `src/main/resources/youtube.properties`: APIキーの設定
- `pom.xml`: Maven依存関係の設定
- `.gitignore`: Git除外設定
- `run.sh`: macOS/Linux用実行スクリプト
- `run.bat`: Windows用実行スクリプト

## セキュリティについて

⚠️ **重要**: このリポジトリには機密情報は含まれていませんが、以下の点にご注意ください：

- APIキーやクライアントシークレットは絶対にコミットしないでください
- `.gitignore`ファイルで機密ファイルが除外されるよう設定されています
- 実際のAPIキーは環境変数またはローカル設定ファイルで管理してください

## トラブルシューティング

### 共通の問題

1. **APIキーエラー**: `src/main/resources/youtube.properties`でAPIキーが正しく設定されているか確認してください
2. **依存関係エラー**: `mvn clean compile`を実行して依存関係を再ダウンロードしてください
3. **Javaバージョンエラー**: Java 11以上がインストールされているか確認してください
4. **設定ファイルが見つからない**: `youtube.properties.example`をコピーして`youtube.properties`を作成してください

### Windows固有の問題

1. **文字化け**: コマンドプロンプトで文字化けが発生する場合は、PowerShellを使用してください
2. **実行権限エラー**: `run.bat`が実行できない場合は、ファイルを右クリックして「管理者として実行」を試してください
3. **パスエラー**: 長いパス名が原因でエラーが発生する場合は、プロジェクトを短いパスに移動してください

### macOS/Linux固有の問題

1. **実行権限エラー**: `chmod +x run.sh`で実行権限を付与してください
2. **Bashエラー**: `/bin/bash`が存在しない場合は、`sh run.sh`を試してください

## 使用ライブラリ

### 最新バージョン（2024年12月時点）
- **Google API Client Library**: 2.2.0
- **Google HTTP Client**: 1.45.0  
- **Google OAuth2 Client**: 1.39.0
- **Jackson Core**: 2.20.0
- **Jackson Databind**: 2.20.0
- **Jackson Annotations**: 2.18.2
- **Apache Commons IO**: 2.20.0
- **JUnit**: 4.13.2

## 参考リンク

- [YouTube Data API v3 Documentation](https://developers.google.com/youtube/v3)
- [Google API Client Library for Java](https://github.com/googleapis/google-api-java-client)
- [Jackson JSON Processing](https://github.com/FasterXML/jackson)
