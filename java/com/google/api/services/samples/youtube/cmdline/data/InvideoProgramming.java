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

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.InvideoBranding;
import com.google.api.services.youtube.model.InvideoPromotion;
import com.google.api.services.youtube.model.InvideoTiming;
import com.google.api.services.youtube.model.PromotedItem;
import com.google.api.services.youtube.model.PromotedItemId;
import com.google.common.collect.Lists;

/**
 * 注目の動画をチャンネルに追加します。
 *
 * @author Ikai Lan <ikai@google.com>
 */
public class InvideoProgramming {

	/**
	 * YouTube DataAPIリクエストを行うために使用されるYoutubeオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTube youtube;

	/**
	 * このサンプルビデオでは、YouTube DevelopersLiveでWebMを紹介しています。
	 */
	private static final String FEATURED_VIDEO_ID = "w4eiUiauo2w";

	/**
	 * このコードサンプルは、APIを使用してチャンネルコンテンツを宣伝するさまざまな方法を示しています。
	 * 次のタスクのコードが含まれています。
	 * <ol>
	 * <li>ビデオを特集します。</li>
	 * <li>ソーシャルメディアチャネルへのリンクを紹介します。</li>
	 * <li>チャンネルの動画に透かしを設定します。</li>
	 * </ol>
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		// このOAuth2.0アクセススコープにより、
		// 認証されたユーザーのアカウントへの完全な読み取り/書き込みアクセスが可能になります。
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "invideoprogramming");

			// このオブジェクトは、YouTube DataAPIリクエストを行うために使用されます。
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-invideoprogramming-sample")
					.build();

			// 現在のユーザーのチャネルIDを取得するリクエストを作成します。
			// API応答には、このユースケースに必要なチャネル情報のみを含めます。
			// チャンネルのアップロード再生リストは、チャンネルの最後にアップロードされた動画を識別します。
			// https://developers.google.com/youtube/v3/docs/channels/list を参照してください
			ChannelListResponse channelListResponse = youtube.channels().list("id,contentDetails")
					.setMine(true)
					.setFields("items(contentDetails/relatedPlaylists/uploads,id)")
					.execute();

			// ユーザーのデフォルトチャネルは、リストの最初の項目です。
			// ユーザーがチャネルを持っていない場合、
			// このコードは問題を説明するGoogleJsonResponseExceptionをスローする必要があります。
			Channel myChannel = channelListResponse.getItems().get(0);
			String channelId = myChannel.getId();

			// プロモーションは、ビデオが終了する15000ms（15秒）前に表示されます。
			InvideoTiming invideoTiming = new InvideoTiming();
			invideoTiming.setOffsetMs(BigInteger.valueOf(15000l));
			invideoTiming.setType("offsetFromEnd");

			// これはプロモーションの一種であり、動画を宣伝します。
			PromotedItemId promotedItemId = new PromotedItemId();
			promotedItemId.setType("video");
			promotedItemId.setVideoId(FEATURED_VIDEO_ID);

			// プロモートされた動画またはチャンネルに関する追加情報を提供するカスタムメッセージを設定します。
			PromotedItem promotedItem = new PromotedItem();
			promotedItem.setCustomMessage("Check out this video about WebM!");
			promotedItem.setId(promotedItemId);

			// インビデオプロモーションデータを表すオブジェクトを作成し、それをチャネルに追加します。
			InvideoPromotion invideoPromotion = new InvideoPromotion();
			invideoPromotion.setDefaultTiming(invideoTiming);
			invideoPromotion.setItems(Lists.newArrayList(promotedItem));

			Channel channel = new Channel();
			channel.setId(channelId);
			channel.setInvideoPromotion(invideoPromotion);

			Channel updateChannelResponse = youtube.channels()
					.update("invideoPromotion", channel)
					.execute();

			// API応答からデータを出力します。
			System.out.println("\n================== Updated Channel Information ==================\n");
			System.out.println("\t- Channel ID: " + updateChannelResponse.getId());

			InvideoPromotion promotions = updateChannelResponse.getInvideoPromotion();
			promotedItem = promotions.getItems().get(0); // We only care about the first item
			System.out.println("\t- Invideo promotion video ID: " + promotedItem
					.getId()
					.getVideoId());
			System.out.println("\t- Promotion message: " + promotedItem.getCustomMessage());

			// ビデオ内プログラミングを使用して、関連するWebサイト、マーチャントサイト、
			// またはソーシャルネットワーキングサイトへのリンクを表示することもできます。
			// 以下のコードは、YouTubeデベロッパーのTwitterフィードへのリンクを表示することで、
			// 上記のプロモーションビデオを上書きします。
			PromotedItemId promotedTwitterFeed = new PromotedItemId();
			promotedTwitterFeed.setType("website");
			promotedTwitterFeed.setWebsiteUrl("https://twitter.com/youtubedev");

			promotedItem = new PromotedItem();
			promotedItem.setCustomMessage("Follow us on Twitter!");
			promotedItem.setId(promotedTwitterFeed);

			invideoPromotion.setItems(Lists.newArrayList(promotedItem));
			channel.setInvideoPromotion(invideoPromotion);

			// APIを呼び出して、動画内プロモーションデータを設定します。
			updateChannelResponse = youtube.channels()
					.update("invideoPromotion", channel)
					.execute();

			// API応答からデータを出力します。
			System.out.println("\n================== Updated Channel Information ==================\n");
			System.out.println("\t- Channel ID: " + updateChannelResponse.getId());

			promotions = updateChannelResponse.getInvideoPromotion();
			promotedItem = promotions.getItems().get(0);
			System.out.println("\t- Invideo promotion URL: " + promotedItem
					.getId()
					.getWebsiteUrl());
			System.out.println("\t- Promotion message: " + promotedItem.getCustomMessage());

			// この例では、チャネルのカスタム透かしを設定します。
			// 使用される画像は、「resources /」ディレクトリのwatermark.jpgファイルです。
			InputStreamContent mediaContent = new InputStreamContent("image/jpeg",
					InvideoProgramming.class.getResourceAsStream("/watermark.jpg"));

			// 動画の最後の15秒間に透かしを表示する必要があることを示します。
			InvideoTiming watermarkTiming = new InvideoTiming();
			watermarkTiming.setType("offsetFromEnd");
			watermarkTiming.setDurationMs(BigInteger.valueOf(15000l));
			watermarkTiming.setOffsetMs(BigInteger.valueOf(15000l));

			InvideoBranding invideoBranding = new InvideoBranding();
			invideoBranding.setTiming(watermarkTiming);
			youtube.watermarks().set(channelId, invideoBranding, mediaContent).execute();

		} catch (GoogleJsonResponseException e) {
			System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());
			e.printStackTrace();

		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
