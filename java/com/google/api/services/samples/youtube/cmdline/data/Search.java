/*
 * Copyright (c) 2012 Google Inc.
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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

/**
 * 検索語に一致する動画のリストを印刷します。
 *
 * @author Jeremy Walker
 */
public class Search {

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
			InputStream in = Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
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
				public void initialize(HttpRequest request) throws IOException {
				}
			}).setApplicationName("youtube-cmdline-search-sample").build();

			// ユーザーにクエリ用語の入力を求めます。
			String queryTerm = getInputQuery();

			// 検索結果を取得するためのAPIリクエストを定義します。
			YouTube.Search.List search = youtube.search().list("id,snippet");

			// 認証されていないリクエストには、{{Google CloudConsole}}から開発者キーを設定します。
			// 参照：{{https://cloud.google.com/console}}
			String apiKey = properties.getProperty("youtube.apikey");
			search.setKey(apiKey);
			search.setQ(queryTerm);

			// 検索結果を制限して、動画のみを含めます
			// 参照：https://developers.google.com/youtube/v3/docs/search/list#type
			search.setType("video");

			// 効率を上げるには、アプリケーションが使用するフィールドのみを取得します。
			search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
			search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

			// APIを呼び出し、結果を出力します。
			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			if (searchResultList != null) {
				prettyPrint(searchResultList.iterator(), queryTerm);
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
	 * イテレータですべての結果を出力します。 結果ごとに、タイトル、ビデオID、およびサムネイルを印刷します。
	 *
	 * @param iteratorSearchResults 印刷するSearchResultsのイテレータ
	 *
	 * @param query 検索クエリ（文字列）
	 */
	private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

		System.out.println("\n=============================================================");
		System.out.println(
				"   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
		System.out.println("=============================================================\n");

		if (!iteratorSearchResults.hasNext()) {
			System.out.println(" There aren't any results for your query.");
		}

		while (iteratorSearchResults.hasNext()) {

			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();

			// 結果がビデオを表していることを確認します。それ以外の場合、アイテムにはビデオIDが含まれません。
			if (rId.getKind().equals("youtube#video")) {
				Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

				System.out.println(" Video Id" + rId.getVideoId());
				System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
				System.out.println(" Thumbnail: " + thumbnail.getUrl());
				System.out.println("\n-------------------------------------------------------------\n");
			}
		}
	}
}
