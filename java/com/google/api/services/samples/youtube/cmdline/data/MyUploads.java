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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.common.collect.Lists;

/**
 * 認証されたユーザーのYouTubeチャンネルにアップロードされたビデオのリストを印刷します。
 *
 * @author Jeremy Walker
 */
public class MyUploads {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * ユーザーを承認し、youtube.channels.listメソッドを呼び出して、
	 * ユーザーのチャンネルにアップロードされた動画のリストの再生リストIDを取得してから、
	 * youtube.playlistItems.listメソッドを呼び出してその再生リスト内の動画のリストを取得します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープでは、
		// 認証されたユーザーのアカウントへの読み取り専用アクセスが許可されますが、他の種類のアカウントアクセスは許可されません。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.readonly");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "myuploads");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
					"youtube-cmdline-myuploads-sample").build();

			// APIのchannels.listメソッドを呼び出して、認証されたユーザーのチャネルを表すリソースを取得します。
			// API応答には、このユースケースに必要なチャネル情報のみを含めます。
			// チャンネルのcontentDetails部分には、チャンネルにアップロードされた動画を含むリストのIDを含む、
			// チャンネルに関連する再生リストIDが含まれています。
			YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
			channelRequest.setMine(true);
			channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
			ChannelListResponse channelResult = channelRequest.execute();

			List<Channel> channelsList = channelResult.getItems();

			if (channelsList != null) {
				// ユーザーのデフォルトチャネルは、リストの最初の項目です。
				// API応答からチャンネルの動画の再生リストIDを抽出します。
				String uploadPlaylistId = channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();

				// アップロードされたビデオのリストにアイテムを保存するリストを定義します。
				List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

				// チャンネルにアップロードされた動画の再生リストを取得します。
				YouTube.PlaylistItems.List playlistItemRequest = youtube.playlistItems()
						.list("id,contentDetails,snippet");
				playlistItemRequest.setPlaylistId(uploadPlaylistId);

				// このアプリケーションで使用されるデータのみを取得することで、アプリケーションをより効率的にします。
				// 参考:https://developers.google.com/youtube/v3/getting-started#partial
				playlistItemRequest.setFields(
						"items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

				String nextToken = "";

				// APIを1回以上呼び出して、リスト内のすべてのアイテムを取得します。
				// API応答がnextPageTokenを返す限り、取得するアイテムはまだまだあります。
				do {
					playlistItemRequest.setPageToken(nextToken);
					PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

					playlistItemList.addAll(playlistItemResult.getItems());

					nextToken = playlistItemResult.getNextPageToken();
				} while (nextToken != null);

				// 結果に関する情報を出力します。
				prettyPrint(playlistItemList.size(), playlistItemList.iterator());
			}

		} catch (GoogleJsonResponseException e) {
			e.printStackTrace();
			System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/*
	 * プレイリスト内のすべてのアイテムに関する情報を印刷します。
	 *
	 * @param size リストのサイズ
	 *
	 * @param playlistEntries アップロードされたプレイリストからのプレイリストアイテムのイテレータ
	 */
	private static void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
		System.out.println("=============================================================");
		System.out.println("\t\tTotal Videos Uploaded: " + size);
		System.out.println("=============================================================\n");

		while (playlistEntries.hasNext()) {
			PlaylistItem playlistItem = playlistEntries.next();
			System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
			System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
			System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
			System.out.println("\n-------------------------------------------------------------\n");
		}
	}
}
