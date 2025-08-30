import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Properties;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * YouTube Data API v3を使用したシンプルなクイックスタートサンプル
 * APIキーを使用してHTTP APIを直接呼び出します
 */
public class SimpleQuickstart {

    /** アプリケーション名 */
    private static final String APPLICATION_NAME = "YouTube API Simple Quickstart";

    /** JSONファクトリのグローバルインスタンス */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** HTTPトランスポートのグローバルインスタンス */
    private static HttpTransport HTTP_TRANSPORT;

    /** YouTube Data API v3のベースURL */
    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3";

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * HTTPリクエストファクトリを取得します
     * @param apiKey YouTube Data APIキー
     * @return HTTPリクエストファクトリ
     * @throws IOException
     */
    public static HttpRequestFactory getRequestFactory(String apiKey) throws IOException {
        return HTTP_TRANSPORT.createRequestFactory(request -> {
            request.getUrl().set("key", apiKey);
        });
    }

    /**
     * youtube.propertiesファイルからAPIキーを読み込みます
     * @return APIキー
     * @throws IOException
     */
    public static String getApiKey() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = SimpleQuickstart.class.getResourceAsStream("/youtube.properties");
        if (inputStream == null) {
            throw new IOException("youtube.propertiesファイルが見つかりません");
        }
        properties.load(inputStream);
        String apiKey = properties.getProperty("youtube.apikey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("youtube.apikeyが設定されていません");
        }
        return apiKey.trim();
    }

    /**
     * チャンネル情報を検索します
     * @param requestFactory HTTPリクエストファクトリ
     * @param channelName チャンネル名
     * @return チャンネル情報のJSON文字列
     * @throws IOException
     */
    public static String searchChannels(HttpRequestFactory requestFactory, String channelName) throws IOException {
        String encodedQuery = URLEncoder.encode(channelName, "UTF-8");
        String url = YOUTUBE_API_BASE_URL + "/search?part=snippet&type=channel&q=" + encodedQuery + "&maxResults=5";
        
        HttpRequest request = requestFactory.buildGetRequest(new com.google.api.client.http.GenericUrl(url));
        HttpResponse response = request.execute();
        
        return response.parseAsString();
    }

    public static void main(String[] args) {
        try {
            // APIキーを取得
            String apiKey = getApiKey();
            System.out.println("APIキーを取得しました: " + apiKey.substring(0, 10) + "...");

            // HTTPリクエストファクトリを初期化
            HttpRequestFactory requestFactory = getRequestFactory(apiKey);

            // GoogleDevelopersチャンネルを検索
            String searchQuery = "GoogleDevelopers";
            System.out.println("チャンネルを検索中: " + searchQuery);
            
            String jsonResponse = searchChannels(requestFactory, searchQuery);
            
            // JSONレスポンスを解析
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode itemsNode = rootNode.get("items");
            
            if (itemsNode == null || itemsNode.size() == 0) {
                System.out.println("チャンネルが見つかりませんでした");
                return;
            }

            System.out.println("=== YouTube チャンネル検索結果 ===");
            for (int i = 0; i < itemsNode.size(); i++) {
                JsonNode item = itemsNode.get(i);
                JsonNode snippet = item.get("snippet");
                
                String channelId = snippet.get("channelId").asText();
                String title = snippet.get("title").asText();
                String description = snippet.get("description").asText();
                String publishedAt = snippet.get("publishedAt").asText();
                
                System.out.println("\n--- チャンネル " + (i + 1) + " ---");
                System.out.printf("チャンネルID: %s\n", channelId);
                System.out.printf("チャンネル名: %s\n", title);
                System.out.printf("説明: %s\n", description.length() > 100 ? description.substring(0, 100) + "..." : description);
                System.out.printf("作成日: %s\n", publishedAt);
            }

        } catch (Exception e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
