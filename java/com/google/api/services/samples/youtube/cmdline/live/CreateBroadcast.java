/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.services.samples.youtube.cmdline.live;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamSnippet;
import com.google.common.collect.Lists;

/**
 * Use the YouTube Live Streaming API to insert a broadcast and a stream
 * and then bind them together. Use OAuth 2.0 to authorize the API requests.
 *
 * @author Ibrahim Ulukaya
 */
public class CreateBroadcast {

	/**
	 * Define a global instance of a Youtube object, which will be used
	 * to make YouTube Data API requests.
	 */
	private static YouTube youtube;

	/**
	 * Create and insert a liveBroadcast resource.
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "createbroadcast");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-createbroadcast-sample").build();

			// 放送のタイトルを入力するようにユーザーに促します。
			String title = getBroadcastTitle();
			System.out.println("You chose " + title + " for broadcast title.");

			// ブロードキャストのタイトルとスケジュールされた開始時刻と終了時刻を含むスニペットを作成します。
			// 現在、それらの時間はハードコーディングされています。
			LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
			broadcastSnippet.setTitle(title);
			broadcastSnippet.setScheduledStartTime(new DateTime("2024-01-30T00:00:00.000Z"));
			broadcastSnippet.setScheduledEndTime(new DateTime("2024-01-31T00:00:00.000Z"));

			// ブロードキャストのプライバシーステータスを「プライベート」に設定します。
			// 参照：https：//developers.google.com/youtube/v3/live/docs/liveBroadcasts#status.privacyStatus
			LiveBroadcastStatus status = new LiveBroadcastStatus();
			status.setPrivacyStatus("private");

			LiveBroadcast broadcast = new LiveBroadcast();
			broadcast.setKind("youtube#liveBroadcast");
			broadcast.setSnippet(broadcastSnippet);
			broadcast.setStatus(status);

			// ブロードキャストを挿入するためのAPIリクエストを作成して実行します。
			YouTube.LiveBroadcasts.Insert liveBroadcastInsert = youtube.liveBroadcasts().insert("snippet,status",
					broadcast);
			LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();

			// API応答から情報を出力します。
			System.out.println("\n================== Returned Broadcast ==================\n");
			System.out.println("  - Id: " + returnedBroadcast.getId());
			System.out.println("  - Title: " + returnedBroadcast.getSnippet().getTitle());
			System.out.println("  - Description: " + returnedBroadcast.getSnippet().getDescription());
			System.out.println("  - Published At: " + returnedBroadcast.getSnippet().getPublishedAt());
			System.out.println(
					"  - Scheduled Start Time: " + returnedBroadcast.getSnippet().getScheduledStartTime());
			System.out.println(
					"  - Scheduled End Time: " + returnedBroadcast.getSnippet().getScheduledEndTime());

			// ビデオストリームのタイトルを入力するようにユーザーに促します。
			title = getStreamTitle();
			System.out.println("You chose " + title + " for stream title.");

			// ビデオストリームのタイトルを使用してスニペットを作成します。
			LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
			streamSnippet.setTitle(title);

			// ビデオストリームのコンテンツ配信ネットワーク設定を定義します。
			// 設定は、ストリームの形式と取り込みタイプを指定します。
			// 参照：https：//developers.google.com/youtube/v3/live/docs/liveStreams#cdn
			CdnSettings cdnSettings = new CdnSettings();
			cdnSettings.setFormat("1080p");
			cdnSettings.setIngestionType("rtmp");

			LiveStream stream = new LiveStream();
			stream.setKind("youtube#liveStream");
			stream.setSnippet(streamSnippet);
			stream.setCdn(cdnSettings);

			// APIリクエストを作成して実行し、ストリームを挿入します。
			YouTube.LiveStreams.Insert liveStreamInsert = youtube.liveStreams().insert("snippet,cdn", stream);
			LiveStream returnedStream = liveStreamInsert.execute();

			// API応答から情報を出力します。
			System.out.println("\n================== Returned Stream ==================\n");
			System.out.println("  - Id: " + returnedStream.getId());
			System.out.println("  - Title: " + returnedStream.getSnippet().getTitle());
			System.out.println("  - Description: " + returnedStream.getSnippet().getDescription());
			System.out.println("  - Published At: " + returnedStream.getSnippet().getPublishedAt());

			// 新しいブロードキャストとストリームをバインドするリクエストを作成して実行します。
			YouTube.LiveBroadcasts.Bind liveBroadcastBind = youtube.liveBroadcasts().bind(returnedBroadcast.getId(),
					"id,contentDetails");
			liveBroadcastBind.setStreamId(returnedStream.getId());
			returnedBroadcast = liveBroadcastBind.execute();

			// API応答から情報を出力します。
			System.out.println("\n================== Returned Bound Broadcast ==================\n");
			System.out.println("  - Broadcast Id: " + returnedBroadcast.getId());
			System.out.println(
					"  - Bound Stream Id: " + returnedBroadcast.getContentDetails().getBoundStreamId());

		} catch (GoogleJsonResponseException e) {
			System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());
			e.printStackTrace();

		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Throwable t) {
			System.err.println("Throwable: " + t.getMessage());
			t.printStackTrace();
		}
	}

	/*
	 * 放送のタイトルを入力するようにユーザーに促します。
	 */
	private static String getBroadcastTitle() throws IOException {

		String title = "";

		System.out.print("Please enter a broadcast title: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		title = bReader.readLine();

		if (title.length() < 1) {
			// デフォルトのタイトルとして「NewBroadcast」を使用します。
			title = "New Broadcast";
		}
		return title;
	}

	/*
	 * ストリームのタイトルを入力するようにユーザーに促します。
	 */
	private static String getStreamTitle() throws IOException {

		String title = "";

		System.out.print("Please enter a stream title: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		title = bReader.readLine();

		if (title.length() < 1) {
			// デフォルトのタイトルとして「New Stream」を使用します。
			title = "New Stream";
		}
		return title;
	}

}
