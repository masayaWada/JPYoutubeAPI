# JPYoutubeAPI

YouTube Data API v3を使用したJavaサンプルプロジェクトです。

## 必要な環境

- Java 11以上
- Maven 3.6以上
- YouTube Data API v3のAPIキー

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

### 簡単な実行（推奨）

```bash
./run.sh
```

または

```bash
./run.sh simple
```

### その他のサンプル実行

```bash
# OAuth2認証を使用したサンプル
./run.sh oauth

# 動画検索サンプル
./run.sh search

# アップロード動画一覧サンプル
./run.sh uploads
```

### Mavenコマンドで直接実行

```bash
# SimpleQuickstart（APIキー使用）
mvn exec:java -Dexec.mainClass="com.google.api.services.samples.youtube.cmdline.data.SimpleQuickstart"

# Quickstart（OAuth2認証）
mvn exec:java -Dexec.mainClass="com.google.api.services.samples.youtube.cmdline.data.Quickstart"
```

## 利用可能なサンプル

- **SimpleQuickstart**: APIキーを使用したシンプルなチャンネル情報取得
- **Quickstart**: OAuth2認証を使用したチャンネル情報取得
- **Search**: 動画検索機能
- **MyUploads**: アップロードした動画の一覧表示
- **UploadVideo**: 動画アップロード機能
- **UpdateVideo**: 動画情報の更新
- **PlaylistUpdates**: プレイリストの操作
- **CommentHandling**: コメントの操作
- **Live**: ライブ配信関連の機能
- **Analytics**: YouTube Analytics APIの使用

## 設定ファイル

- `resources/youtube.properties`: APIキーの設定
- `resources/client_secrets.json`: OAuth2認証用のクライアントシークレット
- `pom.xml`: Maven依存関係の設定
- `.vscode/settings.json`: VSCode用のJava設定

## セキュリティについて

⚠️ **重要**: このリポジトリには機密情報は含まれていませんが、以下の点にご注意ください：

- APIキーやクライアントシークレットは絶対にコミットしないでください
- `.gitignore`ファイルで機密ファイルが除外されるよう設定されています
- 実際のAPIキーは環境変数またはローカル設定ファイルで管理してください

## トラブルシューティング

1. **APIキーエラー**: `src/main/resources/youtube.properties`でAPIキーが正しく設定されているか確認してください
2. **依存関係エラー**: `mvn clean compile`を実行して依存関係を再ダウンロードしてください
3. **Javaバージョンエラー**: Java 11以上がインストールされているか確認してください
4. **設定ファイルが見つからない**: `youtube.properties.example`をコピーして`youtube.properties`を作成してください

## 参考リンク

- [YouTube Data API v3 Documentation](https://developers.google.com/youtube/v3)
- [Google API Client Library for Java](https://github.com/googleapis/google-api-java-client)
