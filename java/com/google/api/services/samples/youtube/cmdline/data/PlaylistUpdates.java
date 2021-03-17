/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.services.samples.youtube.cmdline.data;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.common.collect.Lists;

/**
 * 承認されたユーザーのチャンネルに新しいプライベート再生リストを作成し、その新しい再生リストに動画を追加します。
 *
 * @author Jeremy Walker
 */
public class PlaylistUpdates {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * 新しい再生リストに追加されるビデオを識別するグローバル変数を定義します。
	 */
	private static final String VIDEO_ID = "SZj6rAYkYOg";

	/**
	 * ユーザーを承認し、プレイリストを作成して、プレイリストにアイテムを追加します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "playlistupdates");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-playlistupdates-sample")
					.build();

			// 承認されたユーザーのチャンネルに新しいプライベートプレイリストを作成します。
			String playlistId = insertPlaylist();

			// 有効な再生リストが作成されている場合は、その再生リストに動画を追加します。
			insertPlaylistItem(playlistId, VIDEO_ID);

		} catch (GoogleJsonResponseException e) {
			System.err.println(
					"There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
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
	 * プレイリストを作成し、承認されたアカウントに追加します。
	 */
	private static String insertPlaylist() throws IOException {

		// このコードは、挿入されるプレイリストリソースを構築します。
		// プレイリストのタイトル、説明、プライバシーステータスを定義します。
		PlaylistSnippet playlistSnippet = new PlaylistSnippet();
		playlistSnippet.setTitle("Test Playlist " + Calendar.getInstance().getTime());
		playlistSnippet.setDescription("A private playlist created with the YouTube API v3");
		PlaylistStatus playlistStatus = new PlaylistStatus();
		playlistStatus.setPrivacyStatus("private");

		Playlist youTubePlaylist = new Playlist();
		youTubePlaylist.setSnippet(playlistSnippet);
		youTubePlaylist.setStatus(playlistStatus);

		// APIを呼び出して、新しいプレイリストを挿入します。
		// API呼び出しでは、最初の引数はAPI応答に含める必要のあるリソース部分を識別し、
		// 2番目の引数は挿入されるプレイリストです。
		YouTube.Playlists.Insert playlistInsertCommand = youtube.playlists().insert("snippet,status", youTubePlaylist);
		Playlist playlistInserted = playlistInsertCommand.execute();

		// API応答からデータを出力し、新しいプレイリストの一意のプレイリストIDを返します。
		System.out.println("New Playlist name: " + playlistInserted.getSnippet().getTitle());
		System.out.println(" - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
		System.out.println(" - Description: " + playlistInserted.getSnippet().getDescription());
		System.out.println(" - Posted: " + playlistInserted.getSnippet().getPublishedAt());
		System.out.println(" - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");
		return playlistInserted.getId();

	}

	/**
	 * 指定した動画IDで再生リストアイテムを作成し、指定した再生リストに追加します。
	 *
	 * @param playlistId 新しく作成したプレイリストアイテムに割り当てる
	 * @param videoId    プレイリストアイテムに追加するYouTubeビデオID
	 */
	private static String insertPlaylistItem(String playlistId, String videoId) throws IOException {

		// 再生リストに追加される動画を識別するresourceIdを定義します。
		ResourceId resourceId = new ResourceId();
		resourceId.setKind("youtube#video");
		resourceId.setVideoId(videoId);

		// PlayplayItemリソースの「スニペット」部分に含まれるフィールドを設定します。
		PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
		playlistItemSnippet.setTitle("First video in the test playlist");
		playlistItemSnippet.setPlaylistId(playlistId);
		playlistItemSnippet.setResourceId(resourceId);

		// PlayplayItemリソースを作成し、そのスニペットを上記で作成したオブジェクトに設定します。
		PlaylistItem playlistItem = new PlaylistItem();
		playlistItem.setSnippet(playlistItemSnippet);

		// APIを呼び出して、指定したプレイリストにプレイリストアイテムを追加します。
		// API呼び出しでは、最初の引数はAPI応答に含める必要のあるリソース部分を識別し、
		// 2番目の引数は挿入されるプレイリストアイテムです。
		YouTube.PlaylistItems.Insert playlistItemsInsertCommand = youtube.playlistItems()
				.insert("snippet,contentDetails", playlistItem);
		PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

		// API応答からデータを出力し、新しいプレイリストアイテムの一意のplaylistItemIDを返します。
		System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
		System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
		System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
		System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
		return returnedPlaylistItem.getId();

	}
}
