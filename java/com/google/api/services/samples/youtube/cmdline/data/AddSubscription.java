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

package com.google.api.services.samples.youtube.cmdline.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.common.collect.Lists;

/**
 * YouTube Data API（v3）を使用して、ユーザーをチャンネルに登録します。
 * 認証にはOAuth2.0を使用します。
 */
public class AddSubscription {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * ユーザーのYouTubeアカウントをユーザーが選択したチャンネルに登録します。
	 *
	 * @param args コマンドライン引数（使用されません）。
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "addsubscription");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
					"youtube-cmdline-addsubscription-sample").build();

			// ユーザーが選択したチャンネルをサブスクライブします。
			// ユーザーがサブスクライブしているチャネルIDを取得します。
			String channelId = getChannelId();
			System.out.println("サブスクライブする " + channelId + " を選択しました。");

			// チャネルIDを識別するresourceIdを作成します。
			ResourceId resourceId = new ResourceId();
			resourceId.setChannelId(channelId);
			resourceId.setKind("youtube#channel");

			// resourceIdを含むスニペットを作成します。
			SubscriptionSnippet snippet = new SubscriptionSnippet();
			snippet.setResourceId(resourceId);

			// サブスクリプションを追加するリクエストを作成し、リクエストを送信します。
			// リクエストは、挿入するサブスクリプションメタデータと、APIサーバーがレスポンスで返す必要のある情報を識別します。
			Subscription subscription = new Subscription();
			subscription.setSnippet(snippet);
			YouTube.Subscriptions.Insert subscriptionInsert = youtube.subscriptions().insert("snippet,contentDetails",
					subscription);
			Subscription returnedSubscription = subscriptionInsert.execute();

			// API応答から情報を出力します。
			System.out.println("\n================== Returned Subscription ==================\n");
			System.out.println("  - Id: " + returnedSubscription.getId());
			System.out.println("  - Title: " + returnedSubscription.getSnippet().getTitle());

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
	 * ユーザーにチャネルIDを入力して返すように求めます。
	 */
	private static String getChannelId() throws IOException {

		String channelId = "";

		System.out.print("チャンネルIDを入力してください: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		channelId = bReader.readLine();

		if (channelId.length() < 1) {
			// 何も入力されていない場合、デフォルトで「YouTubeForDevelopers」になります。
			channelId = "UCtVd0c0tGXuTSbU5d8cSBUg";
		}
		return channelId;
	}
}
