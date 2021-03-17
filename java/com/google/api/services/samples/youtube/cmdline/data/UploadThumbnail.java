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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Thumbnails.Set;
import com.google.api.services.youtube.model.ThumbnailSetResponse;
import com.google.common.collect.Lists;

/**
 * このサンプルでは、MediaHttpUploaderを使用して画像をアップロードし、
 * APIのyoutube.thumbnails.setメソッドを呼び出して、画像を動画のカスタムサムネイルとして設定します。
 *
 * @author Ibrahim Ulukaya
 */
public class UploadThumbnail {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * アップロードされる画像のMIMEタイプを指定するグローバル変数を定義します。
	 */
	private static final String IMAGE_FILE_FORMAT = "image/png";

	/**
	 * サムネイル画像のビデオIDとパスを指定するようにユーザーに促します。
	 * 次に、APIを呼び出して、画像を動画のサムネイルとして設定します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "uploadthumbnail");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
					"youtube-cmdline-uploadthumbnail-sample").build();

			// 更新するビデオのビデオIDを入力するようにユーザーに促します。
			String videoId = getVideoIdFromUser();
			System.out.println("You chose " + videoId + " to upload a thumbnail.");

			// サムネイル画像の場所を指定するようにユーザーに促します。
			File imageFile = getImageFromUser();
			System.out.println("You chose " + imageFile + " to upload.");

			// サムネイル画像ファイルの内容を含むオブジェクトを作成します。
			InputStreamContent mediaContent = new InputStreamContent(
					IMAGE_FILE_FORMAT, new BufferedInputStream(new FileInputStream(imageFile)));
			mediaContent.setLength(imageFile.length());

			// mediaContentオブジェクトが指定されたビデオのサムネイルであることを指定するAPIリクエストを作成します。
			Set thumbnailSet = youtube.thumbnails().set(videoId, mediaContent);

			// アップロードタイプを設定し、イベントリスナーを追加します。
			MediaHttpUploader uploader = thumbnailSet.getMediaHttpUploader();

			// メディアの直接アップロードが有効になっているかどうかを示します。
			// 「True」の値は、メディアの直接アップロードが有効になっており、
			// メディアコンテンツ全体が1回のリクエストでアップロードされることを示します。
			// デフォルトの「False」の値は、要求が再開可能なメディアアップロードプロトコルを使用することを示します。
			// これは、ネットワークの中断またはその他の送信障害の後にアップロード操作を再開する機能をサポートし、
			// 次の場合に時間と帯域幅を節約します。 ネットワーク障害。
			uploader.setDirectUploadEnabled(false);

			// サムネイル画像のアップロード状態を設定します。
			MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
				@Override
				public void progressChanged(MediaHttpUploader uploader) throws IOException {
					switch (uploader.getUploadState()) {
					// この値は、開始要求が送信される前に設定されます。
					case INITIATION_STARTED:
						System.out.println("Initiation Started");
						break;
					// この値は、開始要求が完了した後に設定されます。
					case INITIATION_COMPLETE:
						System.out.println("Initiation Completed");
						break;
					// この値は、メディアファイルチャンクがアップロードされた後に設定されます。
					case MEDIA_IN_PROGRESS:
						System.out.println("Upload in progress");
						System.out.println("Upload percentage: " + uploader.getProgress());
						break;
					// この値は、メディアファイル全体が正常にアップロードされた後に設定されます。
					case MEDIA_COMPLETE:
						System.out.println("Upload Completed!");
						break;
					// この値は、アップロードプロセスがまだ開始されていないことを示します。
					case NOT_STARTED:
						System.out.println("Upload Not Started!");
						break;
					}
				}
			};
			uploader.setProgressListener(progressListener);

			// 画像をアップロードし、指定した動画のサムネイルとして設定します。
			ThumbnailSetResponse setResponse = thumbnailSet.execute();

			// 更新されたビデオのサムネイル画像のURLを印刷します。
			System.out.println("\n================== Uploaded Thumbnail ==================\n");
			System.out.println("  - Url: " + setResponse.getItems().get(0).getDefault().getUrl());

		} catch (GoogleJsonResponseException e) {
			System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());
			e.printStackTrace();

		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 * YouTubeビデオIDを入力し、ユーザー入力を返すようにユーザーに促します。
	 */
	private static String getVideoIdFromUser() throws IOException {

		String inputVideoId = "";

		System.out.print("Please enter a video Id to update: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		inputVideoId = bReader.readLine();

		if (inputVideoId.length() < 1) {
			// ユーザーがビデオIDを指定しない場合は終了します。
			System.out.print("Video Id can't be empty!");
			System.exit(1);
		}

		return inputVideoId;
	}

	/*
	 * アップロードするサムネイル画像のパスを入力するようにユーザーに促します。
	 */
	private static File getImageFromUser() throws IOException {

		String path = "";

		System.out.print("Please enter the path of the image file to upload: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		path = bReader.readLine();

		if (path.length() < 1) {
			// ユーザーが画像ファイルへのパスを指定しない場合は終了します。
			System.out.print("Path can not be empty!");
			System.exit(1);
		}

		return new File(path);
	}
}
