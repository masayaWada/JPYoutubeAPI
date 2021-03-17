/*
 * Copyright (c) 2015 Google Inc.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Captions.Download;
import com.google.api.services.youtube.YouTube.Captions.Insert;
import com.google.api.services.youtube.YouTube.Captions.Update;
import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionListResponse;
import com.google.api.services.youtube.model.CaptionSnippet;
import com.google.common.collect.Lists;

/**
 * このサンプルは、次の方法で字幕トラックを作成および管理します。
 *
 * 1. 「captions.insert」メソッドを介してビデオのキャプショントラックをアップロードします。
 * 2. 「captions.list」メソッドを介してビデオのキャプショントラックを取得します。
 * 3. 「captions.update」メソッドを使用して既存のキャプショントラックを更新します。
 * 4. 「captions.download」メソッドを使用してキャプショントラックをダウンロードします。
 * 5. 「captions.delete」メソッドを使用して既存のキャプショントラックを削除します。
 */
public class Captions {

	/**
	 * YouTubeデータAPIリクエストを行うために使用されるYouTubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * アップロードされるキャプションのMIMEタイプを指定するグローバル変数を定義します。
	 */
	private static final String CAPTION_FILE_FORMAT = "*/*";

	/**
	 * キャプションのダウンロード形式を指定するグローバル変数を定義します。
	 */
	private static final String SRT = "srt";

	/**
	 * キャプショントラックをアップロード、一覧表示、更新、ダウンロード、および削除します。
	 *
	 * @param args コマンドライン引数（使用されません）。
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープは、認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスを可能にし、
		// SSL接続を使用するためのリクエストを必要とします。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "captions");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-captions-sample").build();

			// 達成するアクションを指定するようにユーザーに促します。
			String actionString = getActionFromUser();
			System.out.println(actionString + "を選択しました。");

			Action action = Action.valueOf(actionString.toUpperCase());
			switch (action) {
			case UPLOAD:
				uploadCaption(getVideoId(), getLanguage(), getName(), getCaptionFromUser());
				break;
			case LIST:
				listCaptions(getVideoId());
				break;
			case UPDATE:
				updateCaption(getCaptionIDFromUser(), getUpdateCaptionFromUser());
				break;
			case DOWNLOAD:
				downloadCaption(getCaptionIDFromUser());
				break;
			case DELETE:
				deleteCaption(getCaptionIDFromUser());
				break;
			default:
				// 例として、使用可能なすべてのメソッドを順番に使用します。

				// キャプショントラックをアップロードするビデオと、キャプショントラックの言語、名前、バイナリファイルを指定するようにユーザーに求めます。
				// 次に、ユーザーが選択した値を使用してキャプショントラックをアップロードします。
				String videoId = getVideoId();
				uploadCaption(videoId, getLanguage(), getName(), getCaptionFromUser());
				List<Caption> captions = listCaptions(videoId);
				if (captions.isEmpty()) {
					System.out.println("ビデオキャプショントラックを取得できません。");
				} else {
					// 最初にアップロードされたキャプショントラックを取得します。
					String firstCaptionId = captions.get(0).getId();

					updateCaption(firstCaptionId, null);
					downloadCaption(firstCaptionId);
					deleteCaption(firstCaptionId);
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

	/**
	 * YouTubeビデオのキャプショントラックを削除します。（captions.delete）
	 *
	 * @param captionId idパラメーターは、削除されるリソースのキャプションIDを指定します。
	 *                  キャプションリソースでは、idプロパティはキャプショントラックのIDを指定します。
	 * @throws IOException
	 */
	private static void deleteCaption(String captionId) throws IOException {
		// YouTube Data APIのcaptions.deleteメソッドを呼び出して、既存のキャプショントラックを削除します。
		youtube.captions().delete(captionId);
		System.out.println("  -  Deleted caption: " + captionId);
	}

	/**
	 * YouTubeビデオのキャプショントラックをダウンロードします。 （captions.download）
	 *
	 * @param captionId idパラメーターは、ダウンロードされるリソースのキャプションIDを指定します。
	 *                  キャプションリソースでは、idプロパティはキャプショントラックのIDを指定します。
	 * @throws IOException
	 */
	private static void downloadCaption(String captionId) throws IOException {
		// YouTube Data APIのcaptions.downloadメソッドへのAPIリクエストを作成して、
		// 既存のキャプショントラックをダウンロードします。
		Download captionDownload = youtube.captions().download(captionId).setTfmt(SRT);

		// ダウンロードタイプを設定し、イベントリスナーを追加します。
		MediaHttpDownloader downloader = captionDownload.getMediaHttpDownloader();

		// メディアの直接ダウンロードが有効になっているかどうかを示します。
		// 「True」の値は、メディアの直接ダウンロードが有効になっており、
		// メディアコンテンツ全体が1回のリクエストでダウンロードされることを示します。
		// デフォルトの「False」の値は、要求が再開可能なメディアダウンロードプロトコルを使用することを示します。
		// これは、ネットワークの中断またはその他の送信障害の後にダウンロード操作を再開する機能をサポートし、
		// 次の場合に時間と帯域幅を節約します。 ネットワーク障害。
		downloader.setDirectDownloadEnabled(false);

		// キャプショントラックファイルのダウンロード状態を設定します。
		MediaHttpDownloaderProgressListener downloadProgressListener = new MediaHttpDownloaderProgressListener() {
			@Override
			public void progressChanged(MediaHttpDownloader downloader) throws IOException {
				switch (downloader.getDownloadState()) {
				case MEDIA_IN_PROGRESS:
					System.out.println("ダウンロード中");
					System.out.println("ダウンロード率: " + downloader.getProgress());
					break;
				// この値は、メディアファイル全体が正常にダウンロードされた後に設定されます。
				case MEDIA_COMPLETE:
					System.out.println("ダウンロードが完了しました！");
					break;
				// この値は、ダウンロードプロセスがまだ開始されていないことを示します。
				case NOT_STARTED:
					System.out.println("ダウンロードは開始されていません！");
					break;
				}
			}
		};
		downloader.setProgressListener(downloadProgressListener);

		OutputStream outputFile = new FileOutputStream("captionFile.srt");
		// キャプショントラックをダウンロードします。
		captionDownload.executeAndDownloadTo(outputFile);
	}

	/**
	 * キャプショントラックのドラフトステータスを更新して公開します。
	 * トラックが存在する場合は、新しいバイナリファイルでトラックを更新します。 （captions.update）
	 *
	 * @param captionId idパラメーターは、更新されるリソースのキャプションIDを指定します。
	 *                  キャプションリソースでは、idプロパティはキャプショントラックのIDを指定します。
	 * @param captionFile キャプショントラックバイナリファイル。
	 * @throws IOException
	 */
	private static void updateCaption(String captionId, File captionFile) throws IOException {
		// キャプションのisDraftプロパティを変更して、キャプショントラックを非公開にします。
		CaptionSnippet updateCaptionSnippet = new CaptionSnippet();
		updateCaptionSnippet.setIsDraft(false);
		Caption updateCaption = new Caption();
		updateCaption.setId(captionId);
		updateCaption.setSnippet(updateCaptionSnippet);

		Caption captionUpdateResponse;

		if (captionFile == null) {
			// YouTube Data APIのcaptions.updateメソッドを呼び出して、既存のキャプショントラックを更新します。
			captionUpdateResponse = youtube.captions().update("snippet", updateCaption).execute();

		} else {
			// キャプションファイルの内容を含むオブジェクトを作成します。
			InputStreamContent mediaContent = new InputStreamContent(
					CAPTION_FILE_FORMAT, new BufferedInputStream(new FileInputStream(captionFile)));
			mediaContent.setLength(captionFile.length());

			// mediaContentオブジェクトが指定されたビデオのキャプションであることを指定するAPIリクエストを作成します。
			Update captionUpdate = youtube.captions().update("snippet", updateCaption, mediaContent);

			// アップロードタイプを設定し、イベントリスナーを追加します。
			MediaHttpUploader uploader = captionUpdate.getMediaHttpUploader();

			// メディアの直接アップロードが有効になっているかどうかを示します。
			// 「True」の値は、メディアの直接アップロードが有効になっており、
			// メディアコンテンツ全体が1回のリクエストでアップロードされることを示します。
			// デフォルトの「False」の値は、要求が再開可能なメディアアップロードプロトコルを使用することを示します。
			// これは、ネットワークの中断またはその他の送信障害の後にアップロード操作を再開する機能をサポートし、
			// 次の場合に時間と帯域幅を節約します。 ネットワーク障害。
			uploader.setDirectUploadEnabled(false);

			// キャプショントラックファイルのアップロード状態を設定します。
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

			// キャプショントラックをアップロードします。
			captionUpdateResponse = captionUpdate.execute();
			System.out.println("\n================== Uploaded New Caption Track ==================\n");
		}

		// API応答から情報を出力します。
		System.out.println("\n================== Updated Caption Track ==================\n");
		CaptionSnippet snippet = captionUpdateResponse.getSnippet();
		System.out.println("  - ID: " + captionUpdateResponse.getId());
		System.out.println("  - Name: " + snippet.getName());
		System.out.println("  - Language: " + snippet.getLanguage());
		System.out.println("  - Draft Status: " + snippet.getIsDraft());
		System.out.println("\n-------------------------------------------------------------\n");
	}

	/**
	 * キャプショントラックのリストを返します。 （captions.listCaptions）
	 *
	 * @param videoId videoIdパラメーターは、ビデオIDで指定されたビデオのキャプショントラックを返すようにAPIに指示します。
	 * @throws IOException
	 */
	private static List<Caption> listCaptions(String videoId) throws IOException {
		// YouTube Data APIのcaptions.listメソッドを呼び出して、ビデオキャプショントラックを取得します。
		CaptionListResponse captionListResponse = youtube.captions().list("snippet", videoId).execute();

		List<Caption> captions = captionListResponse.getItems();
		// API応答から情報を出力します。
		System.out.println("\n================== Returned Caption Tracks ==================\n");
		CaptionSnippet snippet;
		for (Caption caption : captions) {
			snippet = caption.getSnippet();
			System.out.println("  - ID: " + caption.getId());
			System.out.println("  - Name: " + snippet.getName());
			System.out.println("  - Language: " + snippet.getLanguage());
			System.out.println("\n-------------------------------------------------------------\n");
		}

		return captions;
	}

	/**
	 * APIリクエストパラメータに一致するドラフトステータスのキャプショントラックをアップロードします。 （captions.insert）
	 *
	 * @param videoId APIがキャプショントラックを返す必要があるビデオのYouTubeビデオID。
	 * @param captionLanguage キャプショントラックの言語。
	 * @param captionName キャプショントラックの名前。
	 * @param captionFile キャプショントラックバイナリファイル。
	 * @throws IOException
	 */
	private static void uploadCaption(String videoId, String captionLanguage,
			String captionName, File captionFile) throws IOException {
		// アップロードする前に、キャプションに追加情報を追加してください。
		Caption captionObjectDefiningMetadata = new Caption();

		// キャプションのメタデータのほとんどは、CaptionSnippetオブジェクトに設定されています。
		CaptionSnippet snippet = new CaptionSnippet();

		// キャプションのビデオ、言語、名前、ドラフトステータスを設定します。
		snippet.setVideoId(videoId);
		snippet.setLanguage(captionLanguage);
		snippet.setName(captionName);
		snippet.setIsDraft(true);

		// 完成したスニペットオブジェクトをキャプションリソースに追加します。
		captionObjectDefiningMetadata.setSnippet(snippet);

		// キャプションファイルの内容を含むオブジェクトを作成します。
		InputStreamContent mediaContent = new InputStreamContent(
				CAPTION_FILE_FORMAT, new BufferedInputStream(new FileInputStream(captionFile)));
		mediaContent.setLength(captionFile.length());

		// mediaContentオブジェクトが指定されたビデオのキャプションであることを指定するAPIリクエストを作成します。
		Insert captionInsert = youtube.captions().insert("snippet", captionObjectDefiningMetadata, mediaContent);

		// アップロードタイプを設定し、イベントリスナーを追加します。
		MediaHttpUploader uploader = captionInsert.getMediaHttpUploader();

		// メディアの直接アップロードが有効になっているかどうかを示します。
		// 「True」の値は、メディアの直接アップロードが有効になっており、
		// メディアコンテンツ全体が1回のリクエストでアップロードされることを示します。
		// デフォルトの「False」の値は、要求が再開可能なメディアアップロードプロトコルを使用することを示します。
		// これは、ネットワークの中断またはその他の送信障害の後にアップロード操作を再開する機能をサポートし、
		// 次の場合に時間と帯域幅を節約します。 ネットワーク障害。
		uploader.setDirectUploadEnabled(false);

		// キャプショントラックファイルのアップロード状態を設定します。
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

		// キャプショントラックをアップロードします。
		Caption uploadedCaption = captionInsert.execute();

		// アップロードされたキャプショントラックのメタデータを印刷します。
		System.out.println("\n================== Uploaded Caption Track ==================\n");
		snippet = uploadedCaption.getSnippet();
		System.out.println("  - ID: " + uploadedCaption.getId());
		System.out.println("  - Name: " + snippet.getName());
		System.out.println("  - Language: " + snippet.getLanguage());
		System.out.println("  - Status: " + snippet.getStatus());
		System.out
				.println("\n-------------------------------------------------------------\n");
	}

	/*
	 * ユーザーにキャプショントラックIDの入力を求めます。 次に、IDを返します。
	 */
	private static String getCaptionIDFromUser() throws IOException {

		String captionId = "";

		System.out.print("キャプショントラックIDを入力してください: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		captionId = bReader.readLine();

		System.out.println(captionId + "を選択しました。");
		return captionId;
	}

	/*
	 * ユーザーにビデオIDの入力を求めます。 次に、IDを返します。
	 */
	private static String getVideoId() throws IOException {

		String videoId = "";

		System.out.print("ビデオIDを入力してください: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		videoId = bReader.readLine();

		System.out.println("キャプションに " + videoId + " を選択しました。");
		return videoId;
	}

	/*
	 * キャプショントラックの名前を入力するようにユーザーに促します。 次に、名前を返します。
	 */
	private static String getName() throws IOException {

		String name = "";

		System.out.print("キャプショントラック名を入力してください: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		name = bReader.readLine();

		if (name.length() < 1) {
			// 何も入力しない場合、デフォルトで「YouTubeForDevelopers」になります。
			name = "YouTube for Developers";
		}

		System.out.println("キャプショントラック名として " + name + " を選択しました。");
		return name;
	}

	/*
	 * キャプショントラックの言語を入力するようにユーザーに促します。 次に、言語を返します。
	 */
	private static String getLanguage() throws IOException {

		String language = "";

		System.out.print("キャプション言語を入力してください: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		language = bReader.readLine();

		if (language.length() < 1) {
			// 何も入力されていない場合、デフォルトは「en」です。
			language = "en";
		}

		System.out.println("キャプショントラック言語として " + language + " を選択しました。");
		return language;
	}

	/*
	 * アップロードするキャプショントラックファイルのパスを入力するようにユーザーに促します。
	 */
	private static File getCaptionFromUser() throws IOException {

		String path = "";

		System.out.print("アップロードするキャプショントラックファイルのパスを入力してください: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		path = bReader.readLine();

		if (path.length() < 1) {
			// ユーザーがファイルへのパスを指定しない場合は終了します。
			System.out.print("パスを空にすることはできません！");
			System.exit(1);
		}

		File captionFile = new File(path);
		System.out.println("アップロードする " + captionFile + " を選択しました。");

		return captionFile;
	}

	/*
	 * 置き換えるキャプショントラックファイルのパスを入力するようにユーザーに促します。
	 */
	private static File getUpdateCaptionFromUser() throws IOException {

		String path = "";

		System.out.print("アップロードする新しいキャプショントラックファイルのパスを入力してください"
				+ "（新しいファイルをアップロードしない場合は空のままにします）。: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		path = bReader.readLine();

		if (path.length() < 1) {
			return null;
		}

		File captionFile = new File(path);
		System.out.println("アップロードする " + captionFile + " を選択しました。");

		return captionFile;
	}

	/*
	 * ユーザーにアクションの入力を求めます。 次に、アクションを返します。
	 */
	private static String getActionFromUser() throws IOException {

		String action = "";

		System.out.print("実行するアクションを選択してください。: ");
		System.out.print("オプションは次のとおりです。: 'upload', 'list', 'update', 'download', 'delete',"
				+ " and 'all' ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		action = bReader.readLine();

		return action;
	}

	public enum Action {
		UPLOAD, LIST, UPDATE, DOWNLOAD, DELETE, ALL
	}
}
