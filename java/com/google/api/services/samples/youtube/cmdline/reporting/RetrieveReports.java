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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtubereporting.YouTubeReporting;
import com.google.api.services.youtubereporting.YouTubeReporting.Media.Download;
import com.google.api.services.youtubereporting.model.Job;
import com.google.api.services.youtubereporting.model.ListJobsResponse;
import com.google.api.services.youtubereporting.model.ListReportsResponse;
import com.google.api.services.youtubereporting.model.Report;
import com.google.common.collect.Lists;

/**
 * このサンプルは、特定のジョブによって作成されたレポートを次の方法で取得します。
 *
 * 1.「jobs.list」メソッドを使用してジョブを一覧表示します。
 * 2.「reports.list」メソッドを使用してレポートを取得します。
 *
 * @author Ibrahim Ulukaya
 */
public class RetrieveReports {

	/**
	 * YouTube ReportingAPIリクエストを行うために使用される、
	 * YouTubeReportingオブジェクトのグローバルインスタンスを定義します。
	 */
	private static YouTubeReporting youtubeReporting;

	/**
	 * レポートを取得します。
	 *
	 * @param args command line args (not used).
	 */
	public static void main(String[] args) {

		/*
		 * このOAuth2.0アクセススコープにより、
		 * 認証されたユーザーのアカウントのYouTubeAnalytics通貨レポートへの読み取りアクセスが可能になります。
		 * 収益または広告のパフォーマンス指標を取得するリクエストでは、このスコープを使用する必要があります。
		 */
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/yt-analytics-monetary.readonly");

		try {
			// リクエストを承認します。
			Credential credential = Auth.authorize(scopes, "retrievereports");

			// このオブジェクトは、YouTube ReportingAPIリクエストを行うために使用されます。
			youtubeReporting = new YouTubeReporting.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-retrievereports-sample").build();

			if (listReportingJobs()) {
				if (retrieveReports(getJobIdFromUser())) {
					downloadReport(getReportUrlFromUser());
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
	 * レポートジョブを一覧表示します。（jobs.listJobs）
	 * @return 少なくとも1つのレポートジョブが存在する場合はtrue
	 * @throws IOException
	 */
	private static boolean listReportingJobs() throws IOException {
		// YouTube Reporting APIのjobs.listメソッドを呼び出して、レポートジョブを取得します。
		ListJobsResponse jobsListResponse = youtubeReporting.jobs().list().execute();
		List<Job> jobsList = jobsListResponse.getJobs();

		if (jobsList == null || jobsList.isEmpty()) {
			System.out.println("No jobs found.");
			return false;
		} else {
			// API応答から情報を出力します。
			System.out.println("\n================== Reporting Jobs ==================\n");
			for (Job job : jobsList) {
				System.out.println("  - Id: " + job.getId());
				System.out.println("  - Name: " + job.getName());
				System.out.println("  - Report Type Id: " + job.getReportTypeId());
				System.out.println("\n-------------------------------------------------------------\n");
			}
		}
		return true;
	}

	/**
	 * 特定のジョブによって作成されたレポートを一覧表示します。 （reports.listJobsReports）
	 *
	 * @param jobId ジョブのID。
	 * @throws IOException
	 */
	private static boolean retrieveReports(String jobId)
			throws IOException {
		// YouTube Reporting APIのreports.listメソッドを呼び出して、ジョブによって作成されたレポートを取得します。
		ListReportsResponse reportsListResponse = youtubeReporting.jobs().reports().list(jobId).execute();
		List<Report> reportslist = reportsListResponse.getReports();

		if (reportslist == null || reportslist.isEmpty()) {
			System.out.println("No reports found.");
			return false;
		} else {
			// API応答から情報を出力します。
			System.out.println("\n============= Reports for the job " + jobId + " =============\n");
			for (Report report : reportslist) {
				System.out.println("  - Id: " + report.getId());
				System.out.println("  - From: " + report.getStartTime());
				System.out.println("  - To: " + report.getEndTime());
				System.out.println("  - Download Url: " + report.getDownloadUrl());
				System.out.println("\n-------------------------------------------------------------\n");
			}
		}
		return true;
	}

	/**
	 * URLで指定されたレポートをダウンロードします。（media.download）
	 *
	 * @param reportUrl ダウンロードするレポートのURL。
	 * @throws IOException
	 */
	private static boolean downloadReport(String reportUrl)
			throws IOException {
		// YouTube Reporting APIのmedia.downloadメソッドを呼び出して、レポートをダウンロードします。
		Download request = youtubeReporting.media().download("");
		FileOutputStream fop = new FileOutputStream(new File("report"));
		request.getMediaHttpDownloader().download(new GenericUrl(reportUrl), fop);
		return true;
	}

	/*
	 * レポートを取得するためのジョブIDの入力をユーザーに求めます。次に、IDを返します。
	 */
	private static String getJobIdFromUser() throws IOException {

		String id = "";

		System.out.print("Please enter the job id for the report retrieval: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		id = bReader.readLine();

		System.out.println("You chose " + id + " as the job Id for the report retrieval.");
		return id;
	}

	/*
	 * レポートをダウンロードするためのURLを入力するようにユーザーに促します。 次に、URLを返します。
	 */
	private static String getReportUrlFromUser() throws IOException {

		String url = "";

		System.out.print("Please enter the report URL to download: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		url = bReader.readLine();

		System.out.println("You chose " + url + " as the URL to download.");
		return url;
	}
}
