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

import java.io.IOException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.common.collect.Lists;

/**
 * OAuth 2.0を使用してAPIリクエストを承認し、チャンネルのブロードキャストのリストを取得します。
 *
 * @author Ibrahim Ulukaya
 */
public class ListBroadcasts {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * ユーザーのチャンネルのブロードキャストを一覧表示します。
	 *
	 * @param args コマンドライン引数（使用されません）。
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープでは、認証されたユーザーのアカウントへの読み取り専用アクセスが許可されますが、
		// 他の種類のアカウントアクセスは許可されません。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.readonly");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "listbroadcasts");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-listbroadcasts-sample").build();

			// ブロードキャストを一覧表示するリクエストを作成します。
			YouTube.LiveBroadcasts.List liveBroadcastRequest = youtube.liveBroadcasts().list("id,snippet");

			// API応答は、タイプまたはステータスに基づいてブロードキャストをフィルタリングしてはならないことを示します。
			liveBroadcastRequest.setBroadcastType("all").setBroadcastStatus("all");

			// APIリクエストを実行し、ブロードキャストのリストを返します。
			LiveBroadcastListResponse returnedListResponse = liveBroadcastRequest.execute();
			List<LiveBroadcast> returnedList = returnedListResponse.getItems();

			// API応答から情報を出力します。
			System.out.println("\n================== Returned Broadcasts ==================\n");
			for (LiveBroadcast broadcast : returnedList) {
				System.out.println("  - Id: " + broadcast.getId());
				System.out.println("  - Title: " + broadcast.getSnippet().getTitle());
				System.out.println("  - Description: " + broadcast.getSnippet().getDescription());
				System.out.println("  - Published At: " + broadcast.getSnippet().getPublishedAt());
				System.out.println(
						"  - Scheduled Start Time: " + broadcast.getSnippet().getScheduledStartTime());
				System.out.println(
						"  - Scheduled End Time: " + broadcast.getSnippet().getScheduledEndTime());
				System.out.println("\n-------------------------------------------------------------\n");
			}

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
}
