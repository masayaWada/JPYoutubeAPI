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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;

/**
 * 認証されたユーザーのチャンネルに動画をアップロードします。OAuth2.0を使用してリクエストを承認します。
 * このアプリケーションでアップロードするには、ビデオファイルをプロジェクトフォルダに追加する必要があることに注意してください。
 *
 * @author Jeremy Walker
 */
public class UploadVideo {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * アップロードされるビデオのMIMEタイプを指定するグローバル変数を定義します。
	 */
	private static final String VIDEO_FILE_FORMAT = "video/*";

	private static final String SAMPLE_VIDEO_FILENAME = "sample-video.mp4";

	/**
	 * ユーザーが選択した動画をユーザーのYouTubeチャンネルにアップロードします。
	 * コードは、アプリケーションのプロジェクトフォルダーでビデオを検索し、OAuth2.0を使用してAPIリクエストを承認します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープを使用すると、
		// アプリケーションは認証されたユーザーのYouTubeチャンネルにファイルをアップロードできますが、
		// 他の種類のアクセスは許可されません。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "uploadvideo");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
					"youtube-cmdline-uploadvideo-sample").build();

			System.out.println("Uploading: " + SAMPLE_VIDEO_FILENAME);

			// アップロードする前に、ビデオに追加情報を追加してください。
			Video videoObjectDefiningMetadata = new Video();

			// 動画を一般公開するように設定します。 これがデフォルト設定です。 その他のサポート設定は、「非公開」と「非公開」です。
			// ステータス:非公開-unlisted、限定公開-private、公開-public
			VideoStatus status = new VideoStatus();
			status.setPrivacyStatus("public");
			videoObjectDefiningMetadata.setStatus(status);

			// ビデオのメタデータのほとんどは、VideoSnippetオブジェクトに設定されています。
			VideoSnippet snippet = new VideoSnippet();

			// このコードは、Calendarインスタンスを使用して、テスト目的で一意の名前と説明を作成し、
			// 複数のファイルを簡単にアップロードできるようにします。
			// このコードをプロジェクトから削除し、代わりに独自の標準名を使用する必要があります。
			Calendar cal = Calendar.getInstance();
			snippet.setTitle("Test Upload via Java on " + cal.getTime());
			snippet.setDescription(
					"Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

			// 動画に関連付けるキーワードタグを設定します。
			List<String> tags = new ArrayList<String>();
			tags.add("test");
			tags.add("example");
			tags.add("java");
			tags.add("YouTube Data API V3");
			tags.add("erase me");
			snippet.setTags(tags);

			// 完成したスニペットオブジェクトをビデオリソースに追加します。
			videoObjectDefiningMetadata.setSnippet(snippet);

			InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,
					UploadVideo.class.getResourceAsStream("/sample-video.mp4"));

			// ビデオを挿入します。 このコマンドは3つの引数を送信します。
			// 1つ目は、APIリクエストが設定している情報と、APIレスポンスが返す情報を指定します。
			// 2番目の引数は、新しいビデオに関するメタデータを含むビデオリソースです。
			// 3番目の引数は実際のビデオコンテンツです。
			YouTube.Videos.Insert videoInsert = youtube.videos()
					.insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

			// アップロードタイプを設定し、イベントリスナーを追加します。
			MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

			// メディアの直接アップロードが有効になっているかどうかを示します。
			// 「True」の値は、メディアの直接アップロードが有効になっており、
			// メディアコンテンツ全体が1回のリクエストでアップロードされることを示します。
			// デフォルトの「False」の値は、要求が再開可能なメディアアップロードプロトコルを使用することを示します。
			// これは、ネットワークの中断またはその他の送信障害の後にアップロード操作を再開する機能をサポートし、
			// 次の場合に時間と帯域幅を節約します。 ネットワーク障害。
			uploader.setDirectUploadEnabled(false);

			MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
				public void progressChanged(MediaHttpUploader uploader) throws IOException {
					switch (uploader.getUploadState()) {
					case INITIATION_STARTED:
						System.out.println("Initiation Started");
						break;
					case INITIATION_COMPLETE:
						System.out.println("Initiation Completed");
						break;
					case MEDIA_IN_PROGRESS:
						System.out.println("Upload in progress");
						System.out.println("Upload percentage: " + uploader.getProgress());
						break;
					case MEDIA_COMPLETE:
						System.out.println("Upload Completed!");
						break;
					case NOT_STARTED:
						System.out.println("Upload Not Started!");
						break;
					}
				}
			};
			uploader.setProgressListener(progressListener);

			// APIを呼び出して、ビデオをアップロードします。
			Video returnedVideo = videoInsert.execute();

			// API応答から新しく挿入されたビデオに関するデータを出力します。
			System.out.println("\n================== Returned Video ==================\n");
			System.out.println("  - Id: " + returnedVideo.getId());
			System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
			System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
			System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
			System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());

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
}
