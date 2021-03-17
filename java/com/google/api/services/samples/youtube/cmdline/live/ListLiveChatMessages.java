/*
 * Copyright (c) 2017 Google Inc.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.LiveChatMessage;
import com.google.api.services.youtube.model.LiveChatMessageAuthorDetails;
import com.google.api.services.youtube.model.LiveChatMessageListResponse;
import com.google.api.services.youtube.model.LiveChatMessageSnippet;
import com.google.api.services.youtube.model.LiveChatSuperChatDetails;
import com.google.common.collect.Lists;

/**
 * ライブブロードキャストからのライブチャットメッセージとSuperChatの詳細を一覧表示します。
 *
 * videoIdは、多くの場合、動画のURLに含まれています。
 * 例：https://www.youtube.com/watch?v=L5Xc93_ZL60
 *                                    ^ videoId
 *
 * ビデオのURLは、ブラウザのアドレスバーに表示されるか、
 * ビデオを右クリックしてコンテキストメニューから[ビデオのURLをコピー]を選択します。
 *
 * @author Jim Rogers
 */
public class ListLiveChatMessages {

	/**
	 * チャットメッセージ用に取得する一般的なフィールド
	 */
	private static final String LIVE_CHAT_FIELDS = "items(authorDetails(channelId,displayName,isChatModerator,isChatOwner,isChatSponsor,"
			+ "profileImageUrl),snippet(displayMessage,superChatDetails,publishedAt)),"
			+ "nextPageToken,pollingIntervalMillis";

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * ライブブロードキャストからのライブチャットメッセージとSuperChatの詳細を一覧表示します。
	 *
	 * @param args videoId（オプション）。
	 * videoIdが指定されている場合、ライブチャットメッセージはこのビデオに関連付けられているチャットから取得されます。
	 * videoIdが指定されていない場合、サインインしたユーザーの現在のライブブロードキャストが代わりに使用されます。
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープでは、認証されたユーザーのアカウントへの読み取り専用アクセスが許可されますが、
		// 他の種類のアカウントアクセスは許可されません。
		List<String> scopes = Lists.newArrayList(YouTubeScopes.YOUTUBE_READONLY);

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "listlivechatmessages");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-listchatmessages-sample").build();

			// liveChatIdを取得します
			String liveChatId = args.length == 1
					? GetLiveChatId.getLiveChatId(youtube, args[0])
					: GetLiveChatId.getLiveChatId(youtube);
			if (liveChatId != null) {
				System.out.println("Live chat id: " + liveChatId);
			} else {
				System.err.println("Unable to find a live chat id");
				System.exit(1);
			}

			// ライブチャットメッセージを取得する
			listChatMessages(liveChatId, null, 0);
		} catch (GoogleJsonResponseException e) {
			System.err
					.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
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

	/**
	 * サーバーが提供する間隔でポーリングして、ライブチャットメッセージを一覧表示します。
	 * ライブチャットの所有者とモデレーターは、より速い速度で投票します。
	 *
	 * @param liveChatId メッセージを一覧表示するライブチャットID。
	 * @param nextPageToken 前のリクエストのページトークン（ある場合）。
	 * @param delayMs リクエストを行う前のミリ秒単位の遅延。
	 */
	private static void listChatMessages(
			final String liveChatId,
			final String nextPageToken,
			long delayMs) {
		System.out.println(
				String.format("Getting chat messages in %1$.3f seconds...", delayMs * 0.001));
		Timer pollTimer = new Timer();
		pollTimer.schedule(
				new TimerTask() {
					@Override
					public void run() {
						try {
							// YouTubeからチャットメッセージを取得する
							LiveChatMessageListResponse response = youtube
									.liveChatMessages()
									.list(liveChatId, "snippet, authorDetails")
									.setPageToken(nextPageToken)
									.setFields(LIVE_CHAT_FIELDS)
									.execute();

							// メッセージとスーパーチャットの詳細を表示する
							List<LiveChatMessage> messages = response.getItems();
							for (int i = 0; i < messages.size(); i++) {
								LiveChatMessage message = messages.get(i);
								LiveChatMessageSnippet snippet = message.getSnippet();
								System.out.println(buildOutput(
										snippet.getDisplayMessage(),
										message.getAuthorDetails(),
										snippet.getSuperChatDetails()));
							}

							// メッセージの次のページをリクエストする
							listChatMessages(
									liveChatId,
									response.getNextPageToken(),
									response.getPollingIntervalMillis());
						} catch (Throwable t) {
							System.err.println("Throwable: " + t.getMessage());
							t.printStackTrace();
						}
					}
				}, delayMs);
	}

	/**
	 * コンソール出力用にチャットメッセージをフォーマットします。
	 *
	 * @param message 出力する表示メッセージ。
	 * @param author メッセージの作成者。
	 * @param superChatDetails メッセージに関連付けられたSuperChatの詳細。
	 * @return コンソール出力用にフォーマットされた文字列。
	 */
	private static String buildOutput(
			String message,
			LiveChatMessageAuthorDetails author,
			LiveChatSuperChatDetails superChatDetails) {
		StringBuilder output = new StringBuilder();
		if (superChatDetails != null) {
			output.append(superChatDetails.getAmountDisplayString());
			output.append("SUPERCHAT RECEIVED FROM ");
		}
		output.append(author.getDisplayName());
		List<String> roles = new ArrayList<String>();
		if (author.getIsChatOwner()) {
			roles.add("OWNER");
		}
		if (author.getIsChatModerator()) {
			roles.add("MODERATOR");
		}
		if (author.getIsChatSponsor()) {
			roles.add("SPONSOR");
		}
		if (roles.size() > 0) {
			output.append(" (");
			String delim = "";
			for (String role : roles) {
				output.append(delim).append(role);
				delim = ", ";
			}
			output.append(")");
		}
		if (message != null && !message.isEmpty()) {
			output.append(": ");
			output.append(message);
		}
		return output.toString();
	}
}
