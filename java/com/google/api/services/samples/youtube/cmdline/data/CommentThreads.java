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
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.CommentThreadSnippet;
import com.google.common.collect.Lists;

/**
 * このサンプルは、次の方法でトップレベルのコメントを作成および管理します。
 *
 * 1. 「commentThreads.insert」メソッドを使用して、動画とチャンネルのトップレベルのコメントを作成します。
 * 2. 「commentThreads.list」メソッドを介して動画とチャンネルのトップレベルのコメントを取得します。
 * 3. 「commentThreads.update」メソッドを介して既存のコメントを更新します。
 *
 * @author Ibrahim Ulukaya
 */
public class CommentThreads {

	/**
	 * YouTubeデータAPIリクエストを行うために使用されるYouTubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * トップレベルのチャンネルと動画のコメントを作成、一覧表示、更新します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープは、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスを可能にし、
		// SSL接続を使用するためのリクエストを必要とします。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "commentthreads");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-commentthreads-sample").build();

			// コメントするチャンネルのIDをユーザーに求めます。 ユーザーがコメントしているチャネルIDを取得します。
			String channelId = getChannelId();
			System.out.println("You chose " + channelId + " to subscribe.");

			// コメントする動画のIDをユーザーに求めます。 ユーザーがコメントしているビデオIDを取得します。
			String videoId = getVideoId();
			System.out.println("You chose " + videoId + " to subscribe.");

			// コメントテキストの入力をユーザーに求めます。 ユーザーがコメントしているテキストを取得します。
			String text = getText();
			System.out.println("You chose " + text + " to subscribe.");

			// videoIdを省略してチャンネルコメントを挿入します。 テキスト付きのコメントスニペットを作成します。
			CommentSnippet commentSnippet = new CommentSnippet();
			commentSnippet.setTextOriginal(text);

			// スニペットを使用してトップレベルのコメントを作成します。
			Comment topLevelComment = new Comment();
			topLevelComment.setSnippet(commentSnippet);

			// channelIdとトップレベルのコメントを使用してコメントスレッドスニペットを作成します。
			CommentThreadSnippet commentThreadSnippet = new CommentThreadSnippet();
			commentThreadSnippet.setChannelId(channelId);
			commentThreadSnippet.setTopLevelComment(topLevelComment);

			// スニペットを使用してコメントスレッドを作成します。
			CommentThread commentThread = new CommentThread();
			commentThread.setSnippet(commentThreadSnippet);

			// YouTube Data APIのcommentThreads.insertメソッドを呼び出して、コメントを作成します。
			CommentThread channelCommentInsertResponse = youtube.commentThreads()
					.insert("snippet", commentThread).execute();
			// API応答から情報を出力します。
			System.out
					.println("\n================== Created Channel Comment ==================\n");
			CommentSnippet snippet = channelCommentInsertResponse.getSnippet().getTopLevelComment()
					.getSnippet();
			System.out.println("  - Author: " + snippet.getAuthorDisplayName());
			System.out.println("  - Comment: " + snippet.getTextDisplay());
			System.out
					.println("\n-------------------------------------------------------------\n");

			// ビデオコメントを挿入
			commentThreadSnippet.setVideoId(videoId);
			// YouTube Data APIのcommentThreads.insertメソッドを呼び出して、コメントを作成します。
			CommentThread videoCommentInsertResponse = youtube.commentThreads()
					.insert("snippet", commentThread).execute();
			// API応答から情報を出力します。
			System.out
					.println("\n================== Created Video Comment ==================\n");
			snippet = videoCommentInsertResponse.getSnippet().getTopLevelComment()
					.getSnippet();
			System.out.println("  - Author: " + snippet.getAuthorDisplayName());
			System.out.println("  - Comment: " + snippet.getTextDisplay());
			System.out
					.println("\n-------------------------------------------------------------\n");

			// YouTube Data APIのcommentThreads.listメソッドを呼び出して、ビデオコメントスレッドを取得します。
			CommentThreadListResponse videoCommentsListResponse = youtube.commentThreads()
					.list("snippet").setVideoId(videoId).setTextFormat("plainText").execute();
			List<CommentThread> videoComments = videoCommentsListResponse.getItems();

			if (videoComments.isEmpty()) {
				System.out.println("Can't get video comments.");
			} else {
				// API応答から情報を出力します。
				System.out
						.println("\n================== Returned Video Comments ==================\n");
				for (CommentThread videoComment : videoComments) {
					snippet = videoComment.getSnippet().getTopLevelComment()
							.getSnippet();
					System.out.println("  - Author: " + snippet.getAuthorDisplayName());
					System.out.println("  - Comment: " + snippet.getTextDisplay());
					System.out
							.println("\n-------------------------------------------------------------\n");
				}
				CommentThread firstComment = videoComments.get(0);
				firstComment.getSnippet().getTopLevelComment().getSnippet()
						.setTextOriginal("updated");
				CommentThread videoCommentUpdateResponse = youtube.commentThreads()
						.update("snippet", firstComment).execute();
				// API応答から情報を出力します。
				System.out
						.println("\n================== Updated Video Comment ==================\n");
				snippet = videoCommentUpdateResponse.getSnippet().getTopLevelComment()
						.getSnippet();
				System.out.println("  - Author: " + snippet.getAuthorDisplayName());
				System.out.println("  - Comment: " + snippet.getTextDisplay());
				System.out
						.println("\n-------------------------------------------------------------\n");

			}

			// YouTube Data APIのcommentThreads.listメソッドを呼び出して、チャンネルのコメントスレッドを取得します。
			CommentThreadListResponse channelCommentsListResponse = youtube.commentThreads()
					.list("snippet").setChannelId(channelId).setTextFormat("plainText").execute();
			List<CommentThread> channelComments = channelCommentsListResponse.getItems();

			if (channelComments.isEmpty()) {
				System.out.println("Can't get channel comments.");
			} else {
				// API応答から情報を出力します。
				System.out
						.println("\n================== Returned Channel Comments ==================\n");
				for (CommentThread channelComment : channelComments) {
					snippet = channelComment.getSnippet().getTopLevelComment()
							.getSnippet();
					System.out.println("  - Author: " + snippet.getAuthorDisplayName());
					System.out.println("  - Comment: " + snippet.getTextDisplay());
					System.out
							.println("\n-------------------------------------------------------------\n");
				}
				CommentThread firstComment = channelComments.get(0);
				firstComment.getSnippet().getTopLevelComment().getSnippet()
						.setTextOriginal("updated");
				CommentThread channelCommentUpdateResponse = youtube.commentThreads()
						.update("snippet", firstComment).execute();
				// API応答から情報を出力します。
				System.out
						.println("\n================== Updated Channel Comment ==================\n");
				snippet = channelCommentUpdateResponse.getSnippet().getTopLevelComment()
						.getSnippet();
				System.out.println("  - Author: " + snippet.getAuthorDisplayName());
				System.out.println("  - Comment: " + snippet.getTextDisplay());
				System.out
						.println("\n-------------------------------------------------------------\n");

			}

		} catch (GoogleJsonResponseException e) {
			System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode()
					+ " : " + e.getDetails().getMessage());
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
	 * ユーザーにチャネルIDの入力を求めます。 次に、IDを返します。
	 */
	private static String getChannelId() throws IOException {

		String channelId = "";

		System.out.print("Please enter a channel id: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		channelId = bReader.readLine();

		return channelId;
	}

	/*
	 * ユーザーにビデオIDの入力を求めます。 次に、IDを返します。
	 */
	private static String getVideoId() throws IOException {

		String videoId = "";

		System.out.print("Please enter a video id: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		videoId = bReader.readLine();

		return videoId;
	}

	/*
	 * コメントのテキストを入力するようにユーザーに促します。 次に、テキストを返します。
	 */
	private static String getText() throws IOException {

		String text = "";

		System.out.print("Please enter a comment text: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		text = bReader.readLine();

		if (text.length() < 1) {
			// 何も入力されていない場合、デフォルトで「YouTubeForDevelopers」になります。
			text = "YouTube For Developers.";
		}
		return text;
	}
}
