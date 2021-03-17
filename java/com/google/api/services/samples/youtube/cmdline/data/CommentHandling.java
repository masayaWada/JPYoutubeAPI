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
import com.google.api.services.youtube.model.CommentListResponse;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.common.collect.Lists;

/**
 * このサンプルは、次の方法でコメントを作成および管理します。
 *
 * 1.「commentThreads.list」メソッドを介してビデオのトップレベルのコメントを取得します。
 * 2.「comments.insert」メソッドを介してコメントスレッドに返信します。
 * 3.「comments.list」メソッドを介してコメント応答を取得します。
 * 4.「comments.update」メソッドを使用して既存のコメントを更新します。
 * 5.「comments.setModerationStatus」メソッドを使用して既存のコメントのモデレートステータスを設定します。
 * 6.「comments.markAsSpam」メソッドを使用してコメントをスパムとしてマークします。
 * 7.「comments.delete」メソッドを使用して既存のコメントを削除します。
 */
public class CommentHandling {

	/**
	 * YouTubeデータAPIリクエストを行うために使用されるYouTubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * リスト、コメントスレッドへの返信。 返信の一覧表示、更新、モデレート、マーク付け、削除。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープは、への完全な読み取り/書き込みアクセスを可能にします。
		// 認証されたユーザーのアカウントであり、SSL接続を使用するための要求が必要です。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "commentthreads");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-commentthreads-sample").build();

			// コメントする動画のIDをユーザーに求めます。
			// ユーザーがコメントしているビデオIDを取得します。
			String videoId = getVideoId();
			System.out.println("You chose " + videoId + " to subscribe.");

			// コメントテキストの入力をユーザーに求めます。
			// ユーザーがコメントしているテキストを取得します。
			String text = getText();
			System.out.println("You chose " + text + " to subscribe.");

			// 例として、使用可能なすべてのメソッドを順番に使用します。

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
					CommentSnippet snippet = videoComment.getSnippet().getTopLevelComment()
							.getSnippet();
					System.out.println("  - Author: " + snippet.getAuthorDisplayName());
					System.out.println("  - Comment: " + snippet.getTextDisplay());
					System.out
							.println("\n-------------------------------------------------------------\n");
				}
				CommentThread firstComment = videoComments.get(0);

				// このスレッドを新しい返信の親として使用します。
				String parentId = firstComment.getId();

				// テキスト付きのコメントスニペットを作成します。
				CommentSnippet commentSnippet = new CommentSnippet();
				commentSnippet.setTextOriginal(text);
				commentSnippet.setParentId(parentId);

				// スニペットを使用してコメントを作成します。
				Comment comment = new Comment();
				comment.setSnippet(commentSnippet);

				// YouTube Data APIのcomments.insertメソッドを呼び出して、コメントに返信します。
				// (新しいトップレベルのコメントを作成する場合は、
				//  代わりにcommentThreads.insertメソッドを使用する必要があります。)
				Comment commentInsertResponse = youtube.comments().insert("snippet", comment)
						.execute();

				// API応答から情報を出力します。
				System.out
						.println("\n================== Created Comment Reply ==================\n");
				CommentSnippet snippet = commentInsertResponse.getSnippet();
				System.out.println("  - Author: " + snippet.getAuthorDisplayName());
				System.out.println("  - Comment: " + snippet.getTextDisplay());
				System.out
						.println("\n-------------------------------------------------------------\n");

				// YouTube Data APIのcomments.listメソッドを呼び出して、既存のコメントの返信を取得します。
				CommentListResponse commentsListResponse = youtube.comments().list("snippet")
						.setParentId(parentId).setTextFormat("plainText").execute();
				List<Comment> comments = commentsListResponse.getItems();

				if (comments.isEmpty()) {
					System.out.println("Can't get comment replies.");
				} else {
					// API応答から情報を出力します。
					System.out
							.println("\n================== Returned Comment Replies ==================\n");
					for (Comment commentReply : comments) {
						snippet = commentReply.getSnippet();
						System.out.println("  - Author: " + snippet.getAuthorDisplayName());
						System.out.println("  - Comment: " + snippet.getTextDisplay());
						System.out
								.println("\n-------------------------------------------------------------\n");
					}
					Comment firstCommentReply = comments.get(0);
					firstCommentReply.getSnippet().setTextOriginal("updated");
					Comment commentUpdateResponse = youtube.comments()
							.update("snippet", firstCommentReply).execute();
					// API応答から情報を出力します。
					System.out
							.println("\n================== Updated Video Comment ==================\n");
					snippet = commentUpdateResponse.getSnippet();
					System.out.println("  - Author: " + snippet.getAuthorDisplayName());
					System.out.println("  - Comment: " + snippet.getTextDisplay());
					System.out
							.println("\n-------------------------------------------------------------\n");

					// YouTube Data APIのcomments.setModerationStatusメソッドを呼び出して、
					// 既存のコメントのモデレートステータスを設定します。
					youtube.comments().setModerationStatus(firstCommentReply.getId(), "published");
					System.out.println("  -  Changed comment status to published: "
							+ firstCommentReply.getId());

					// YouTube Data APIのcomments.markAsSpamメソッドを呼び出して、既存のコメントをスパムとしてマークします。
					youtube.comments().markAsSpam(firstCommentReply.getId());
					System.out.println("  -  Marked comment as spam: " + firstCommentReply.getId());

					// YouTube Data APIのcomments.deleteメソッドを呼び出して、既存のコメントを削除します。
					youtube.comments().delete(firstCommentReply.getId());
					System.out
							.println("  -  Deleted comment as spam: " + firstCommentReply.getId());
				}
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
