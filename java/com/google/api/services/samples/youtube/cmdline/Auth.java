package com.google.api.services.samples.youtube.cmdline;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

/**
 * すべてのサンプルで使用される共有クラス。ユーザーを承認し、資格情報をキャッシュするためのメソッドが含まれています。
 */
public class Auth {

	/**
	 * HTTPトランスポートのグローバルインスタンスを定義します。
	 */
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * JSONファクトリのグローバルインスタンスを定義します。
	 */
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * これは、OAuthトークンが保存されるユーザーのホームディレクトリの下で使用されるディレクトリです。
	 */
	private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";

	/**
	 * インストールされたアプリケーションがユーザーの保護されたデータにアクセスすることを許可します。
	 *
	 * @param scopes              YouTubeアップロードを実行するために必要なスコープのリスト。
	 * @param credentialDatastore OAuthトークンをキャッシュするための資格情報データストアの名前
	 */
	public static Credential authorize(List<String> scopes, String credentialDatastore) throws IOException {

		// クライアントシークレットをロードします。
		Reader clientSecretReader = new InputStreamReader(Auth.class.getResourceAsStream("/client_secrets.json"));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

		// デフォルトが置き換えられたことを確認します（デフォルト=「ここにXを入力してください」）。
		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println(
					"Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential "
							+ "into src/main/resources/client_secrets.json");
			System.exit(1);
		}

		// これにより、〜/ .oauth-credentials / $ {credentialDatastore}に認証情報データストアが作成されます
		FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(
				new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
		DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setCredentialDataStore(datastore)
						.build();

		// ローカルサーバーを構築し、ポート8080にバインドします
		LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

		// 承認します。
		return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
	}
}
