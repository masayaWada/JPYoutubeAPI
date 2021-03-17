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
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.common.collect.Lists;

/**
 * メタデータにキーワードタグを追加して、動画を更新します。
 * デモでは、認証にYouTube Data API（v3）とOAuth2.0を使用します。
 *
 * @author Ibrahim Ulukaya
 */
public class UpdateVideo {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * ユーザーが指定した動画にキーワードタグを追加します。 OAuth2.0を使用してAPIリクエストを承認します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "updatevideo");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-updatevideo-sample").build();

			// 更新するビデオのビデオIDを入力するようにユーザーに促します。
			String videoId = getVideoIdFromUser();
			System.out.println("You chose " + videoId + " to update.");

			// 動画に追加するキーワードタグを入力するようにユーザーに促します。
			String tag = getTagFromUser();
			System.out.println("You chose " + tag + " as a tag.");

			// YouTube Data APIのyoutube.videos.listメソッドを呼び出して、指定された動画を表すリソースを取得します。
			YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet").setId(videoId);
			VideoListResponse listResponse = listVideosRequest.execute();

			// APIリクエストは一意の動画IDを指定しているため、APIレスポンスは正確に1つの動画を返す必要があります。
			// 応答にビデオが含まれていない場合、指定されたビデオIDが見つかりませんでした。
			List<Video> videoList = listResponse.getItems();
			if (videoList.isEmpty()) {
				System.out.println("Can't find a video with ID: " + videoId);
				return;
			}

			// ビデオリソースからスニペットを抽出します。
			Video video = videoList.get(0);
			VideoSnippet snippet = video.getSnippet();

			// ビデオにすでに関連付けられているタグを保持します。 ビデオにタグがない場合は、新しい配列を作成します。
			// 提供されたタグを、ビデオに関連付けられているタグのリストに追加します。
			List<String> tags = snippet.getTags();
			if (tags == null) {
				tags = new ArrayList<String>(1);
				snippet.setTags(tags);
			}
			tags.add(tag);

			// videos.update（）メソッドを呼び出して、ビデオリソースを更新します。
			YouTube.Videos.Update updateVideosRequest = youtube.videos().update("snippet", video);
			Video videoResponse = updateVideosRequest.execute();

			// 更新されたリソースから情報を印刷します。
			System.out.println("\n================== Returned Video ==================\n");
			System.out.println("  - Title: " + videoResponse.getSnippet().getTitle());
			System.out.println("  - Tags: " + videoResponse.getSnippet().getTags());

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
	 * ユーザーにキーワードタグの入力を求めます。
	 */
	private static String getTagFromUser() throws IOException {

		String keyword = "";

		System.out.print("Please enter a tag for your video: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		keyword = bReader.readLine();

		if (keyword.length() < 1) {
			// ユーザーがタグを入力しない場合は、デフォルト値の「新しいタグ」を使用します。
			keyword = "New Tag";
		}
		return keyword;
	}

	/*
	 * ユーザーにビデオIDの入力を求めます。
	 */
	private static String getVideoIdFromUser() throws IOException {

		String videoId = "";

		System.out.print("Please enter a video Id to update: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		videoId = bReader.readLine();

		if (videoId.length() < 1) {
			// ユーザーが値を指定しない場合は終了します。
			System.out.print("Video Id can't be empty!");
			System.exit(1);
		}

		return videoId;
	}

}
