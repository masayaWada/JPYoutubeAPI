package com.google.api.services.samples.youtube.cmdline.analytics;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtubeAnalytics.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.model.ResultTable;
import com.google.api.services.youtubeAnalytics.model.ResultTable.ColumnHeaders;
import com.google.common.collect.Lists;

/**
 * この例では、YouTubeデータとYouTubeアナリティクスAPIを使用してYouTubeアナリティクスデータを取得します。
 * また、認証にOAuth2.0を使用します。
 */
public class YouTubeAnalyticsReports {

	/**
	 * HTTPトランスポートのグローバルインスタンスを定義します。
	 */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * JSONファクトリのグローバルインスタンスを定義します。
	 */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * YouTube AnalyticsAPIリクエストを行うために使用される
	 * YoutubeAnalyticsオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTubeAnalytics analytics;

	/**
	 * このコードはユーザーを承認し、YouTube Data APIを使用してユーザーのYouTubeチャンネルに関する情報を取得し、
	 * YouTube AnalyticsAPIを使用してユーザーのチャンネルの統計を取得して出力します。
	 *
	 * @param args コマンドライン引数（使用されません）。
	 */
	public static void main(String[] args) {

		// これらのスコープは、認証されたユーザーのYouTubeチャンネルに関する情報と、
		// そのチャンネルのアナリティクスデータにアクセスするために必要です。
		List<String> scopes = Lists.newArrayList(
				"https://www.googleapis.com/auth/yt-analytics.readonly",
				"https://www.googleapis.com/auth/youtube.readonly");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "analyticsreports");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
					.setApplicationName("youtube-analytics-api-report-example")
					.build();

			// このオブジェクトは、YouTube AnalyticsAPIリクエストを行うために使用されます。
			analytics = new YouTubeAnalytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
					.setApplicationName("youtube-analytics-api-report-example")
					.build();

			// 現在のユーザーのチャネルIDを取得するリクエストを作成します。
			YouTube.Channels.List channelRequest = youtube.channels().list("id,snippet");
			channelRequest.setMine(true);
			channelRequest.setFields("items(id,snippet/title)");
			ChannelListResponse channels = channelRequest.execute();

			// ユーザーに関連付けられているチャネルを一覧表示します。
			List<Channel> listOfChannels = channels.getItems();

			// ユーザーのデフォルトチャネルは、リストの最初の項目です。
			Channel defaultChannel = listOfChannels.get(0);
			String channelId = defaultChannel.getId();

			PrintStream writer = System.out;
			if (channelId == null) {
				writer.println("チャネルが見つかりません。");
			} else {
				writer.println("デフォルトチャネル: " + defaultChannel.getSnippet().getTitle() +
						" ( " + channelId + " )\n");

				printData(writer, "Views Over Time.", executeViewsOverTimeQuery(analytics, channelId));
				printData(writer, "Top Videos", executeTopVideosQuery(analytics, channelId));
				printData(writer, "Demographics", executeDemographicsQuery(analytics, channelId));
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Throwable t) {
			System.err.println("Throwable: " + t.getMessage());
			t.printStackTrace();
		}
	}

	/**
	 * チャンネルの1日あたりの視聴回数とユニーク視聴者数を取得します。
	 *
	 * @param analytics APIへのアクセスに使用される分析サービスオブジェクト。
	 * @param id        データを取得するチャネルID。
	 * @return APIからの応答。
	 * @throws IOException APIエラーが発生した場合。
	 */
	private static ResultTable executeViewsOverTimeQuery(YouTubeAnalytics analytics,
			String id) throws IOException {

		return analytics.reports()
				.query("channel==" + id, // channel id
						"2012-01-01", // Start date.
						"2012-01-14", // End date.
						"views,uniques") // Metrics.
				.setDimensions("day")
				.setSort("day")
				.execute();
	}

	/**
	 * チャンネルで最も視聴された10本の動画を降順で取得します。
	 *
	 * @param analytics APIへのアクセスに使用される分析サービスオブジェクト。
	 * @param id        データを取得するチャネルID。
	 * @return APIからの応答。
	 * @throws IOException APIエラーが発生した場合。
	 */
	private static ResultTable executeTopVideosQuery(YouTubeAnalytics analytics,
			String id) throws IOException {

		return analytics.reports()
				.query("channel==" + id, // channel id
						"2012-01-01", // Start date.
						"2012-08-14", // End date.
						"views,subscribersGained,subscribersLost") // Metrics.
				.setDimensions("video")
				.setSort("-views")
				.setMaxResults(10)
				.execute();
	}

	/**
	 * チャネルの人口統計レポートを取得します。
	 *
	 * @param analytics APIへのアクセスに使用される分析サービスオブジェクト。
	 * @param id        データを取得するチャネルID。
	 * @return APIからの応答。
	 * @throws IOException APIエラーが発生した場合。
	 */
	private static ResultTable executeDemographicsQuery(YouTubeAnalytics analytics,
			String id) throws IOException {
		return analytics.reports()
				.query("channel==" + id, // channel id
						"2007-01-01", // Start date.
						"2012-08-14", // End date.
						"viewerPercentage") // Metrics.
				.setDimensions("ageGroup,gender")
				.setSort("-viewerPercentage")
				.execute();
	}

	/**
	 * API応答を出力します。
	 * チャネル名は、各列名および行のすべてのデータとともに印刷されます。
	 *
	 * @param writer  出力するストリーム
	 * @param title   レポートのタイトル
	 * @param results APIから返されたデータ。
	 */
	private static void printData(PrintStream writer, String title, ResultTable results) {
		writer.println("Report: " + title);
		if (results.getRows() == null || results.getRows().isEmpty()) {
			writer.println("結果が見つかりません。");
		} else {

			// 列ヘッダーを印刷します。
			for (ColumnHeaders header : results.getColumnHeaders()) {
				writer.printf("%30s", header.getName());
			}
			writer.println();

			// 実際のデータを印刷します。
			for (List<Object> row : results.getRows()) {
				for (int colNum = 0; colNum < results.getColumnHeaders().size(); colNum++) {
					ColumnHeaders header = results.getColumnHeaders().get(colNum);
					Object column = row.get(colNum);
					if ("INTEGER".equals(header.getUnknownKeys().get("dataType"))) {
						long l = ((BigDecimal) column).longValue();
						writer.printf("%30d", l);
					} else if ("FLOAT".equals(header.getUnknownKeys().get("dataType"))) {
						writer.printf("%30f", column);
					} else if ("STRING".equals(header.getUnknownKeys().get("dataType"))) {
						writer.printf("%30s", column);
					} else {
						// デフォルトの出力。
						writer.printf("%30s", column);
					}
				}
				writer.println();
			}
			writer.println();
		}
	}

}
