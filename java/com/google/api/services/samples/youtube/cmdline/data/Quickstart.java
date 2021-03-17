package com.google.api.services.samples.youtube.cmdline.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;

public class Quickstart {

	/** アプリケーション名. */
	private static final String APPLICATION_NAME = "API Sample";

	/** このアプリケーションのユーザー資格情報を格納するディレクトリ。 */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"), ".credentials/youtube-java-quickstart");

	/** {@linkFileDataStoreFactory}のグローバルインスタンス。 */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** JSONファクトリのグローバルインスタンス。 */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** HTTPトランスポートのグローバルインスタンス。 */
	private static HttpTransport HTTP_TRANSPORT;

	/** このクイックスタートに必要なスコープのグローバルインスタンス。
	 *
	 * これらのスコープを変更する場合は、
	 * 〜/.credentials/drive-java-quickstart で以前に保存した資格情報を削除してください
	 */
	private static final List<String> SCOPES = Arrays.asList(YouTubeScopes.YOUTUBE_READONLY);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * 承認された資格情報オブジェクトを作成します。
	 * @return 承認された資格情報オブジェクト。
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// クライアントシークレットをロードします。
		InputStream in = Quickstart.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// フローを構築し、ユーザー認証リクエストをトリガーします。
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
						.setDataStoreFactory(DATA_STORE_FACTORY)
						.setAccessType("offline")
						.build();
		Credential credential = new AuthorizationCodeInstalledApp(
				flow, new LocalServerReceiver()).authorize("user");
		return credential;
	}

	/**
	 * YouTube Data APIクライアントサービスなど、承認されたAPIクライアントサービスをビルドして返します。
	 * @return 承認されたAPIクライアントサービス
	 * @throws IOException
	 */
	public static YouTube getYouTubeService() throws IOException {
		Credential credential = authorize();
		return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static void main(String[] args) throws IOException {
		YouTube youtube = getYouTubeService();
		try {
			YouTube.Channels.List channelsListByUsernameRequest = youtube.channels()
					.list("snippet,contentDetails,statistics");
			channelsListByUsernameRequest.setForUsername("GoogleDevelopers");

			ChannelListResponse response = channelsListByUsernameRequest.execute();
			Channel channel = response.getItems().get(0);
			System.out.printf(
					"This channel's ID is %s. Its title is '%s', and it has %s views.\n",
					channel.getId(),
					channel.getSnippet().getTitle(),
					channel.getStatistics().getViewCount());
		} catch (GoogleJsonResponseException e) {
			e.printStackTrace();
			System.err.println("There was a service error: " +
					e.getDetails().getCode() + " : " + e.getDetails().getMessage());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
