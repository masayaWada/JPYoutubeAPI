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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelSection;
import com.google.api.services.youtube.model.ChannelSectionListResponse;
import com.google.api.services.youtube.model.ChannelSectionLocalization;
import com.google.common.collect.Lists;

/**
 * このサンプルは、次の方法でチャネルセクションのローカライズされたメタデータを設定および取得します。
 *
 * 1. 「channelSections.update」メソッドを使用して、デフォルトのメタデータの言語を更新し、
 *    チャネルセクションのローカライズされたメタデータを設定します。
 * 2. 「channelSections.list」メソッドを使用して「hl」パラメータを設定し、
 *    選択した言語でチャネルセクションのローカライズされたメタデータを取得します。
 * 3. 「channelSections.list」メソッドを使用してチャネルセクションのローカライズされたメタデータを一覧表示し、
 *   「part」パラメータに「localizations」を含めます。
 *
 * @author Ibrahim Ulukaya
 */
public class ChannelSectionLocalizations {

	/**
	 * YouTubeデータAPIリクエストを行うために使用されるYouTubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * チャネルセクションのローカライズされたメタデータを設定および取得します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "localizations");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-localizations-sample").build();

			// 達成するアクションを指定するようにユーザーに促します。
			String actionString = getActionFromUser();
			System.out.println("You chose " + actionString + ".");
			// ユーザー入力を列挙値にマップします。
			Action action = Action.valueOf(actionString.toUpperCase());

			switch (action) {
			case SET:
				setChannelSectionLocalization(getId("channel section"),
						getDefaultLanguage(), getLanguage(), getMetadata("title"));
				break;
			case GET:
				getChannelSectionLocalization(getId("channel section"), getLanguage());
				break;
			case LIST:
				listChannelSectionLocalizations(getId("channel section"));
				break;
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
	 * チャネルセクションのデフォルト言語を更新し、ローカライズされたメタデータを設定します。
	 *
	 * @param channelSectionId idパラメーターは、更新されるリソースのチャネルセクションIDを指定します。
	 * @param defaultLanguage チャネルセクションのデフォルトメタデータの言語
	 * @param language ローカライズされたメタデータの言語
	 * @param title 設定するローカライズされたタイトル
	 * @throws IOException
	 */
	private static void setChannelSectionLocalization(String channelSectionId,
			String defaultLanguage, String language, String title)
			throws IOException {
		// YouTube Data APIチャンネルSections.listメソッドを呼び出して、チャンネルセクションを取得します。
		ChannelSectionListResponse channelSectionListResponse = youtube.channelSections().list("snippet,localizations")
				.setId(channelSectionId).execute();

		// APIリクエストは一意のチャネルセクションIDを指定しているため、
		// APIレスポンスは正確に1つのチャネルセクションを返す必要があります。
		// 応答にチャネルセクションが含まれていない場合、指定されたチャネルセクションIDが見つかりませんでした。
		List<ChannelSection> channelSectionList = channelSectionListResponse.getItems();
		if (channelSectionList.isEmpty()) {
			System.out.println("Can't find a channel section with ID: " + channelSectionId);
			return;
		}
		ChannelSection channelSection = channelSectionList.get(0);

		// チャネルセクションのデフォルトの言語とローカリゼーションのプロパティを変更します。
		// リソースのsnippet.defaultLanguageプロパティに値が設定されていることを確認します。
		channelSection.getSnippet().setDefaultLanguage(defaultLanguage);

		// チャネルセクションにすでに関連付けられているローカリゼーションを保持します。
		// チャネルセクションにローカリゼーションがない場合は、新しいアレイを作成します。
		// 提供されたローカリゼーションを、チャネルセクションに関連付けられているローカリゼーションのリストに追加します。
		Map<String, ChannelSectionLocalization> localizations = channelSection.getLocalizations();
		if (localizations == null) {
			localizations = new ArrayMap<String, ChannelSectionLocalization>();
			channelSection.setLocalizations(localizations);
		}
		ChannelSectionLocalization channelSectionLocalization = new ChannelSectionLocalization();
		channelSectionLocalization.setTitle(title);
		localizations.put(language, channelSectionLocalization);

		// channelSections.update()メソッドを呼び出して、チャネルセクションリソースを更新します。
		ChannelSection channelSectionResponse = youtube.channelSections()
				.update("snippet,localizations", channelSection).execute();

		// API応答から情報を出力します。
		System.out.println("\n================== Updated Channel Section ==================\n");
		System.out.println("  - ID: " + channelSectionResponse.getId());
		System.out.println("  - Default Language: " +
				channelSectionResponse.getSnippet().getDefaultLanguage());
		System.out.println("  - Title(" + language + "): " +
				channelSectionResponse.getLocalizations().get(language).getTitle());
		System.out.println("\n-------------------------------------------------------------\n");
	}

	/**
	 * 選択した言語のチャネルセクションのローカライズされたメタデータを返します。
	 * ローカライズされたテキストが要求された言語で利用できない場合、このメソッドはデフォルトの言語でテキストを返します。
	 *
	 * @param channelSectionId idパラメーターは、更新されるリソースのチャネルセクションIDを指定します。
	 * @param language ローカライズされたメタデータの言語
	 * @throws IOException
	 */
	private static void getChannelSectionLocalization(String channelSectionId, String language)
			throws IOException {
		// YouTube Data APIチャンネルSections.listメソッドを呼び出して、チャンネルセクションを取得します。
		ChannelSectionListResponse channelSectionListResponse = youtube.channelSections().list("snippet")
				.setId(channelSectionId).set("hl", language).execute();

		// APIリクエストは一意のチャネルIDを指定しているため、APIレスポンスは正確に1つのチャネルを返す必要があります。
		// 応答にチャネルが含まれていない場合、指定されたチャネルIDが見つかりませんでした。
		List<ChannelSection> channelSectionList = channelSectionListResponse.getItems();
		if (channelSectionList.isEmpty()) {
			System.out.println("Can't find a channel section with ID: " + channelSectionId);
			return;
		}
		ChannelSection channelSection = channelSectionList.get(0);

		// API応答から情報を出力します。
		System.out.println("\n================== Channel Section==================\n");
		System.out.println("  - ID: " + channelSection.getId());
		System.out.println("  - Title(" + language + "): " +
				channelSection.getLocalizations().get(language).getTitle());
		System.out.println("\n-------------------------------------------------------------\n");
	}

	/**
	 * チャネルセクションのローカライズされたメタデータのリストを返します。
	 *
	 * @param channelSectionId idパラメーターは、更新されるリソースのチャネルセクションIDを指定します。
	 * @throws IOException
	 */
	private static void listChannelSectionLocalizations(String channelSectionId) throws IOException {
		// YouTube Data APIチャンネルSections.listメソッドを呼び出して、チャンネルセクションを取得します。
		ChannelSectionListResponse channelSectionListResponse = youtube.channelSections().list("snippet,localizations")
				.setId(channelSectionId).execute();

		// APIリクエストは一意のチャネルセクションIDを指定しているため、
		// APIレスポンスは正確に1つのチャネルセクションを返す必要があります。
		// 応答にチャネルセクションが含まれていない場合、指定されたチャネルセクションIDが見つかりませんでした。
		List<ChannelSection> channelSectionList = channelSectionListResponse.getItems();
		if (channelSectionList.isEmpty()) {
			System.out.println("Can't find a channel section with ID: " + channelSectionId);
			return;
		}
		ChannelSection channelSection = channelSectionList.get(0);
		Map<String, ChannelSectionLocalization> localizations = channelSection.getLocalizations();

		// API応答から情報を出力します。
		System.out.println("\n================== Channel ==================\n");
		System.out.println("  - ID: " + channelSection.getId());
		for (String language : localizations.keySet()) {
			System.out.println("  - Title(" + language + "): " +
					localizations.get(language).getTitle());
		}
		System.out.println("\n-------------------------------------------------------------\n");
	}

	/*
	 * ユーザーにリソースIDの入力を求めます。 次に、IDを返します。
	 */
	private static String getId(String resource) throws IOException {

		String id = "";

		System.out.print("Please enter a " + resource + " id: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		id = bReader.readLine();

		System.out.println("You chose " + id + " for localizations.");
		return id;
	}

	/*
	 * ローカライズされたメタデータを入力するようにユーザーに促します。 次に、メタデータを返します。
	 */
	private static String getMetadata(String type) throws IOException {

		String metadata = "";

		System.out.print("Please enter a localized " + type + ": ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		metadata = bReader.readLine();

		if (metadata.length() < 1) {
			// 何も入力されていない場合、デフォルトでタイプになります。
			metadata = type + "(localized)";
		}

		System.out.println("You chose " + metadata + " as localized " + type + ".");
		return metadata;
	}

	/*
	 * リソースのデフォルトメタデータの言語を入力するようにユーザーに促します。 次に、言語を返します。
	 */
	private static String getDefaultLanguage() throws IOException {

		String defaultlanguage = "";

		System.out.print("Please enter the language for the resource's default metadata: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		defaultlanguage = bReader.readLine();

		if (defaultlanguage.length() < 1) {
			// 何も入力されていない場合、デフォルトは「en」です。
			defaultlanguage = "en";
		}

		System.out.println("You chose " + defaultlanguage +
				" as the language for the resource's default metadata.");
		return defaultlanguage;
	}

	/*
	 * ローカライズされたメタデータの言語を入力するようにユーザーに促します。 次に、言語を返します。
	 */
	private static String getLanguage() throws IOException {

		String language = "";

		System.out.print("Please enter the localized metadata language: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		language = bReader.readLine();

		if (language.length() < 1) {
			// 何も入力されていない場合、デフォルトで「de」になります。
			language = "de";
		}

		System.out.println("You chose " + language + " as the localized metadata language.");
		return language;
	}

	/*
	 * ユーザーにアクションの入力を求めます。 次に、アクションを返します。
	 */
	private static String getActionFromUser() throws IOException {

		String action = "";

		System.out.print("Please choose action to be accomplished: ");
		System.out.print("Options are: 'set', 'get' and 'list' ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		action = bReader.readLine();

		return action;
	}

	public enum Action {
		SET, GET, LIST
	}
}
