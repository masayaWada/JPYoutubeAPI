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

import java.util.Calendar;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.ActivityContentDetails;
import com.google.api.services.youtube.model.ActivityContentDetailsBulletin;
import com.google.api.services.youtube.model.ActivitySnippet;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.common.collect.Lists;

/**
 * ユーザーのチャンネルフィードに投稿される動画速報を作成します。
 *
 * @author Jeremy Walker
 */
public class ChannelBulletin {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/*
	 * ユーザーのチャンネルフィードに掲示板として投稿される動画IDのグローバルインスタンスを定義します。
	 * 実際には、おそらく検索またはアプリからこの値を取得します。
	 */
	private static String VIDEO_ID = "L-oNKK1CrnU";

	/**
	 * ユーザーを承認し、
	 * youtube.channels.listメソッドを呼び出してユーザーのYouTubeチャンネルに関する情報を取得し、
	 * 動画IDを含む掲示板をそのチャンネルに投稿します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "channelbulletin");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
					"youtube-cmdline-channelbulletin-sample").build();

			// 現在のユーザーのチャネルIDを取得するリクエストを作成します。
			// https://developers.google.com/youtube/v3/docs/channels/list を参照してください
			YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
			channelRequest.setMine(true);

			// API応答には、このユースケースに必要なチャネル情報のみを含めます。
			channelRequest.setFields("items/contentDetails");
			ChannelListResponse channelResult = channelRequest.execute();

			List<Channel> channelsList = channelResult.getItems();

			if (channelsList != null) {
				// ユーザーのデフォルトチャネルは、リストの最初の項目です。
				String channelId = channelsList.get(0).getId();

				// チャネル速報を表すアクティビティリソースのスニペットを作成します。
				// チャネルIDと説明を設定します。
				ActivitySnippet snippet = new ActivitySnippet();
				snippet.setChannelId(channelId);
				Calendar cal = Calendar.getInstance();
				snippet.setDescription("Bulletin test video via YouTube API on " + cal.getTime());

				// ビデオIFを識別するリソースを作成します。
				// 種類を「youtube＃playlist」に設定し、動画IDの代わりに再生リストIDを使用できます。
				ResourceId resource = new ResourceId();
				resource.setKind("youtube#video");
				resource.setVideoId(VIDEO_ID);

				ActivityContentDetailsBulletin bulletin = new ActivityContentDetailsBulletin();
				bulletin.setResourceId(resource);

				// リクエストのActivityContentDetailsオブジェクトを作成します。
				ActivityContentDetails contentDetails = new ActivityContentDetails();
				contentDetails.setBulletin(bulletin);

				// スニペットとコンテンツの詳細を含むリソースを作成して、アクティビティを送信します
				Activity activity = new Activity();
				activity.setSnippet(snippet);
				activity.setContentDetails(contentDetails);

				// APIリクエストは、書き込まれているリソースパーツ（contentDetailsとスニペット）を識別します。
				// API応答には、これらの部分も含まれます。
				YouTube.Activities.Insert insertActivities = youtube.activities().insert("contentDetails,snippet",
						activity);
				// 新しく作成されたアクティビティリソースを返します。
				Activity newActivityInserted = insertActivities.execute();

				if (newActivityInserted != null) {
					System.out.println(
							"New Activity inserted of type " + newActivityInserted.getSnippet().getType());
					System.out.println(" - Video id "
							+ newActivityInserted.getContentDetails().getBulletin().getResourceId().getVideoId());
					System.out.println(
							" - Description: " + newActivityInserted.getSnippet().getDescription());
					System.out.println(" - Posted on " + newActivityInserted.getSnippet().getPublishedAt());
				} else {
					System.out.println("Activity failed.");
				}

			} else {
				System.out.println("No channels are assigned to this user.");
			}
		} catch (GoogleJsonResponseException e) {
			e.printStackTrace();
			System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
