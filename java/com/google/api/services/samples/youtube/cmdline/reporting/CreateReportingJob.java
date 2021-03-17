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

package com.google.api.services.samples.youtube.cmdline.reporting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtubereporting.YouTubeReporting;
import com.google.api.services.youtubereporting.model.Job;
import com.google.api.services.youtubereporting.model.ListReportTypesResponse;
import com.google.api.services.youtubereporting.model.ReportType;
import com.google.common.collect.Lists;

/**
 * このサンプルは、次の方法でレポートジョブを作成します。
 *
 * 1.「reportTypes.list」メソッドを使用して使用可能なレポートタイプを一覧表示します。
 * 2.「jobs.create」メソッドを使用してレポートジョブを作成します。
 *
 * @author Ibrahim Ulukaya
 */
public class CreateReportingJob {

	/**
	 * YouTube ReportingAPIリクエストを行うために使用される
	 * YouTubeReportingオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTubeReporting youtubeReporting;

	/**
	 * レポートジョブを作成します。
	 *
	 * @param args コマンドライン引数（使用されません）。
	 */
	public static void main(String[] args) {

		/*
		 * このOAuth2.0アクセススコープにより、認証されたユーザーのアカウントの
		 * YouTubeAnalytics通貨レポートへの読み取りアクセスが可能になります。
		 * 収益または広告のパフォーマンス指標を取得するリクエストでは、このスコープを使用する必要があります。
		 */
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/yt-analytics-monetary.readonly");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "createreportingjob");

			// このオブジェクトは、YouTube ReportingAPIリクエストを行うために使用されます。
			youtubeReporting = new YouTubeReporting.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-createreportingjob-sample").build();

			// 作成するジョブの名前を指定するようにユーザーに促します。
			String name = getNameFromUser();

			if (listReportTypes()) {
				createReportingJob(getReportTypeIdFromUser(), name);
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
	 * レポートタイプを一覧表示します。 （reportTypes.listReportTypes）
	 * @return 少なくとも1つのレポートタイプが存在する場合はtrue
	 * @throws IOException
	 */
	private static boolean listReportTypes() throws IOException {
		// YouTube Reporting APIレポートTypes.listメソッドを呼び出して、レポートタイプを取得します。
		ListReportTypesResponse reportTypesListResponse = youtubeReporting.reportTypes().list()
				.execute();
		List<ReportType> reportTypeList = reportTypesListResponse.getReportTypes();

		if (reportTypeList == null || reportTypeList.isEmpty()) {
			System.out.println("No report types found.");
			return false;
		} else {
			// API応答から情報を出力します。
			System.out.println("\n================== Report Types ==================\n");
			for (ReportType reportType : reportTypeList) {
				System.out.println("  - Id: " + reportType.getId());
				System.out.println("  - Name: " + reportType.getName());
				System.out.println("\n-------------------------------------------------------------\n");
			}
		}
		return true;
	}

	/**
	 * レポートジョブを作成します。 （jobs.create）
	 *
	 * @param reportTypeId ジョブのレポートタイプのID。
	 * @param name ジョブの名前。
	 * @throws IOException
	 */
	private static void createReportingJob(String reportTypeId, String name)
			throws IOException {
		// 名前とレポートタイプIDを使用してレポートジョブを作成します。
		Job job = new Job();
		job.setReportTypeId(reportTypeId);
		job.setName(name);

		// YouTube ReportingAPIのjobs.createメソッドを呼び出してジョブを作成します。
		Job createdJob = youtubeReporting.jobs().create(job).execute();

		// API応答から情報を出力します。
		System.out.println("\n================== Created reporting job ==================\n");
		System.out.println("  - ID: " + createdJob.getId());
		System.out.println("  - Name: " + createdJob.getName());
		System.out.println("  - Report Type Id: " + createdJob.getReportTypeId());
		System.out.println("  - Create Time: " + createdJob.getCreateTime());
		System.out.println("\n-------------------------------------------------------------\n");
	}

	/*
	 * ユーザーにジョブの名前を入力するように求めます。 次に、名前を返します。
	 */
	private static String getNameFromUser() throws IOException {

		String name = "";

		System.out.print("Please enter the name for the job [javaTestJob]: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		name = bReader.readLine();

		if (name.length() < 1) {
			// 何も入力されていない場合、デフォルトは「javaTestJob」です。
			name = "javaTestJob";
		}

		System.out.println("You chose " + name + " as the name for the job.");
		return name;
	}

	/*
	 * ジョブのレポートタイプIDを入力するようにユーザーに求めます。 次に、IDを返します。
	 */
	private static String getReportTypeIdFromUser() throws IOException {

		String id = "";

		System.out.print("Please enter the reportTypeId for the job: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		id = bReader.readLine();

		System.out.println("You chose " + id + " as the report type Id for the job.");
		return id;
	}
}
