/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.youtube.cmdline.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.Joiner;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.GeoPoint;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

/**
 * このサンプルは、特定のキーワードに関連付けられ、特定の地理座標の半径内にあるビデオを次のように一覧表示します。
 *
 * 1. 「youtube.search.list」メソッドを使用して動画を検索し、「type」、「q」、「location」、「locationRadius」パラメータを設定します。
 * 2. 「youtube.videos.list」メソッドを使用して各動画の場所の詳細を取得し、「id」パラメータを検索結果の動画IDのカンマ区切りリストに設定します。
 *
 * @author Ibrahim Ulukaya
 */
public class GeolocationSearch {

	/**
	 * 開発者のAPIキーを含むファイルの名前を識別するグローバル変数を定義します。
	 */
	private static final String PROPERTIES_FILENAME = "youtube.properties";

	private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * YouTubeオブジェクトを初期化して、YouTubeで動画を検索します。
	 * 次に、結果セット内の各ビデオの名前とサムネイル画像を表示します。
	 *
	 * @param args command line args.
	 */
	public static void main(String[] args) {
		// プロパティファイルから開発者キーを読み取ります。
		Properties properties = new Properties();
		try {
			InputStream in = GeolocationSearch.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
			properties.load(in);

		} catch (IOException e) {
			System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
					+ " : " + e.getMessage());
			System.exit(1);
		}

		try {
			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			// 最後の引数は必須ですが、HttpRequestの初期化時に何も初期化する必要がないため、
			// インターフェイスをオーバーライドしてno-op関数を提供します。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) throws IOException {
				}
			}).setApplicationName("youtube-cmdline-geolocationsearch-sample").build();

			// ユーザーにクエリ用語の入力を求めます。
			String queryTerm = getInputQuery();

			// ユーザーに位置座標の入力を求めます。
			String location = getInputLocation();

			// ユーザーに場所の半径を入力するように求めます。
			String locationRadius = getInputLocationRadius();

			// 検索結果を取得するためのAPIリクエストを定義します。
			YouTube.Search.List search = youtube.search().list("id,snippet");

			// 認証されていないリクエストには、
			// {{Google CloudConsole}}から開発者キーを設定します。
			// 参照：{{https://cloud.google.com/console}}
			String apiKey = properties.getProperty("youtube.apikey");
			search.setKey(apiKey);
			search.setQ(queryTerm);
			search.setLocation(location);
			search.setLocationRadius(locationRadius);

			// 検索結果を制限して、動画のみを含めます。
			// 参照：https：//developers.google.com/youtube/v3/docs/search/list#type
			search.setType("video");

			// ベストプラクティスとして、アプリケーションが使用するフィールドのみを取得します。
			search.setFields("items(id/videoId)");
			search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

			// APIを呼び出し、結果を出力します。
			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			List<String> videoIds = new ArrayList<String>();

			if (searchResultList != null) {

				// ビデオIDをマージする
				for (SearchResult searchResult : searchResultList) {
					videoIds.add(searchResult.getId().getVideoId());
				}
				Joiner stringJoiner = Joiner.on(',');
				String videoId = stringJoiner.join(videoIds);

				// YouTube Data API youtube.videos.listメソッドを呼び出して、
				// 指定された動画を表すリソースを取得します。
				YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, recordingDetails")
						.setId(videoId);
				VideoListResponse listResponse = listVideosRequest.execute();

				List<Video> videoList = listResponse.getItems();

				if (videoList != null) {
					prettyPrint(videoList.iterator(), queryTerm);
				}
			}
		} catch (GoogleJsonResponseException e) {
			System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());
		} catch (IOException e) {
			System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/*
	 * クエリ用語を入力し、ユーザー指定の用語を返すようにユーザーに促します。
	 */
	private static String getInputQuery() throws IOException {

		String inputQuery = "";

		System.out.print("Please enter a search term: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		inputQuery = bReader.readLine();

		if (inputQuery.length() < 1) {
			// デフォルトとして「YouTubeDevelopersLive」という文字列を使用します。
			inputQuery = "YouTube Developers Live";
		}
		return inputQuery;
	}

	/*
	 * ユーザーに位置座標の入力を求め、ユーザー指定の座標を返します。
	 */
	private static String getInputLocation() throws IOException {

		String inputQuery = "";

		System.out.print("Please enter location coordinates (example: 37.42307,-122.08427): ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		inputQuery = bReader.readLine();

		if (inputQuery.length() < 1) {
			// デフォルトとして文字列「37.42307、-122.08427」を使用します。
			inputQuery = "37.42307,-122.08427";
		}
		return inputQuery;
	}

	/*
	 * ユーザーに場所の半径を入力して、ユーザーが指定した半径を返すように求めます。
	 */
	private static String getInputLocationRadius() throws IOException {

		String inputQuery = "";

		System.out.print("Please enter a location radius (examples: 5km, 8mi):");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		inputQuery = bReader.readLine();

		if (inputQuery.length() < 1) {
			// デフォルトとして文字列「5km」を使用します。
			inputQuery = "5km";
		}
		return inputQuery;
	}

	/*
	 * イテレータですべての結果を出力します。
	 * 結果ごとに、タイトル、ビデオID、場所、サムネイルを印刷します。
	 *
	 * @param iteratorVideoResults 印刷するビデオのイテレータ
	 *
	 * @param query 検索クエリ（文字列）
	 */
	private static void prettyPrint(Iterator<Video> iteratorVideoResults, String query) {

		System.out.println("\n=============================================================");
		System.out.println(
				"   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
		System.out.println("=============================================================\n");

		if (!iteratorVideoResults.hasNext()) {
			System.out.println(" There aren't any results for your query.");
		}

		while (iteratorVideoResults.hasNext()) {

			Video singleVideo = iteratorVideoResults.next();

			Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
			GeoPoint location = singleVideo.getRecordingDetails().getLocation();

			System.out.println(" Video Id" + singleVideo.getId());
			System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
			System.out.println(" Location: " + location.getLatitude() + ", " + location.getLongitude());
			System.out.println(" Thumbnail: " + thumbnail.getUrl());
			System.out.println("\n-------------------------------------------------------------\n");
		}
	}
}
