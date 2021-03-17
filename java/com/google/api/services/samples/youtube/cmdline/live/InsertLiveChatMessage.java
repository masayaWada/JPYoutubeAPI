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
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.LiveChatMessage;
import com.google.api.services.youtube.model.LiveChatMessageSnippet;
import com.google.api.services.youtube.model.LiveChatTextMessageDetails;
import com.google.common.collect.Lists;

/**
 * 現在のユーザーのライブブロードキャストまたはidで指定されたビデオにメッセージを挿入します。
 *
 * videoIdは、多くの場合、動画のURLに含まれています。
 * 例：https://www.youtube.com/watch?v=L5Xc93_ZL60
 *                                    ^ videoId
 * ビデオのURLは、ブラウザのアドレスバーに表示されるか、
 * ビデオを右クリックしてコンテキストメニューから[ビデオのURLをコピー]を選択します。
 *
 * @author Jim Rogers
 */
public class InsertLiveChatMessage {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * ライブブロードキャストにメッセージを挿入します。
	 *
	 * @param args 挿入するメッセージ（必須）の後にvideoId（オプション）が続きます。
	 * videoIdが指定されている場合、ライブチャットメッセージはこのビデオに関連付けられているチャットから取得されます。
	 * videoIdが指定されていない場合、サインインしたユーザーの現在のライブブロードキャストが代わりに使用されます。
	 */
	public static void main(String[] args) {
		// 挿入するチャットメッセージを取得する
		if (args.length == 0) {
			System.err.println("No message specified");
			System.exit(1);
		}
		String message = args[0];

		// このOAuth2.0アクセススコープにより、認証されたユーザーのアカウントへの書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList(YouTubeScopes.YOUTUBE_FORCE_SSL);

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "insertlivechatmessage");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-insertchatmessage-sample").build();

			// liveChatIdを取得します
			String liveChatId = args.length == 2
					? GetLiveChatId.getLiveChatId(youtube, args[1])
					: GetLiveChatId.getLiveChatId(youtube);
			if (liveChatId != null) {
				System.out.println("Live chat id: " + liveChatId);
			} else {
				System.err.println("Unable to find a live chat id");
				System.exit(1);
			}

			// メッセージをライブチャットに挿入します
			LiveChatMessage liveChatMessage = new LiveChatMessage();
			LiveChatMessageSnippet snippet = new LiveChatMessageSnippet();
			snippet.setType("textMessageEvent");
			snippet.setLiveChatId(liveChatId);
			LiveChatTextMessageDetails details = new LiveChatTextMessageDetails();
			details.setMessageText(message);
			snippet.setTextMessageDetails(details);
			liveChatMessage.setSnippet(snippet);
			YouTube.LiveChatMessages.Insert liveChatInsert = youtube.liveChatMessages().insert("snippet",
					liveChatMessage);
			LiveChatMessage response = liveChatInsert.execute();
			System.out.println("Inserted message id " + response.getId());
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
}
