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
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.collect.Lists;

/**
 * ビデオIDまたは現在サインインしているユーザーからライブチャットIDを取得します。
 *
 * videoIdは、多くの場合、動画のURLに含まれています。
 * 例：https://www.youtube.com/watch?v=L5Xc93_ZL60
 *                                    ^ videoId
 * ビデオのURLは、ブラウザのアドレスバーに表示されるか、ビデオを右クリックして選択します。
 * コンテキストメニューからビデオのURLをコピーします。
 *
 * @author Jim Rogers
 */
public class GetLiveChatId {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * ライブブロードキャストからライブチャットメッセージとSuperChatの詳細をポーリングします。
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
			Credential credential = Auth.authorize(scopes, "getlivechatid");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-getlivechatid-sample").build();

			// liveChatIdを取得します
			String liveChatId = args.length == 1
					? getLiveChatId(youtube, args[0])
					: getLiveChatId(youtube);
			if (liveChatId != null) {
				System.out.println("Live chat id: " + liveChatId);
			} else {
				System.err.println("Unable to find a live chat id");
				System.exit(1);
			}
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
	 * 認証されたユーザーのライブブロードキャストからliveChatIdを取得します。
	 *
	 * @param youtube このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
	 * @return liveChatId、または見つからない場合はnull。
	 */
	static String getLiveChatId(YouTube youtube) throws IOException {
		// ユーザーのliveChatIdにサインインします
		YouTube.LiveBroadcasts.List broadcastList = youtube
				.liveBroadcasts()
				.list("snippet")
				.setFields("items/snippet/liveChatId")
				.setBroadcastType("all")
				.setBroadcastStatus("active");
		LiveBroadcastListResponse broadcastListResponse = broadcastList.execute();
		for (LiveBroadcast b : broadcastListResponse.getItems()) {
			String liveChatId = b.getSnippet().getLiveChatId();
			if (liveChatId != null && !liveChatId.isEmpty()) {
				return liveChatId;
			}
		}

		return null;
	}

	/**
	 * videoIdに関連付けられたブロードキャストからliveChatIdを取得します。
	 *
	 * @param youtube このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
	 * @param videoId ライブブロードキャストに関連付けられたvideoId。
	 * @return liveChatId、または見つからない場合はnull。
	 */
	static String getLiveChatId(YouTube youtube, String videoId) throws IOException {
		// ビデオからliveChatIdを取得します
		YouTube.Videos.List videoList = youtube.videos()
				.list("liveStreamingDetails")
				.setFields("items/liveStreamingDetails/activeLiveChatId")
				.setId(videoId);
		VideoListResponse response = videoList.execute();
		for (Video v : response.getItems()) {
			String liveChatId = v.getLiveStreamingDetails().getActiveLiveChatId();
			if (liveChatId != null && !liveChatId.isEmpty()) {
				return liveChatId;
			}
		}

		return null;
	}
}
