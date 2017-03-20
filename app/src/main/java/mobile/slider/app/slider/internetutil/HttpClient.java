package mobile.slider.app.slider.internetutil;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 2016-04-16.
 */
public class HttpClient {
    String search_url = "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&q=";
    String search_query;
    String search_item;
    String searchResult = "";
    public HttpClient(String query) {
        search_item = query;
        search_query = search_url + query.replaceAll("\\s+", " ").trim().replace(" ", "%20");
    }
    public String sendQuery(){
        String result = null;

        try {
            URL searchURL = new URL(search_query);
            HttpURLConnection httpURLConnection = (HttpURLConnection) searchURL.openConnection();
            httpURLConnection.setConnectTimeout(6000);
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                result = "";
                InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader,
                        8192);

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

                bufferedReader.close();
            }
        }catch (java.net.SocketTimeoutException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<SearchResult> parseResult(String json) throws JSONException{
        if (json == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject(json);
        JSONObject jsonObject_responseData = jsonObject.getJSONObject("responseData");
        JSONArray jsonArray_results = jsonObject_responseData.getJSONArray("entries");
        if (jsonArray_results.length() == 0) {
            ArrayList<SearchResult> results = new ArrayList<>();
            return results;
        }
        String firstContent = jsonArray_results.getJSONObject(0).getString("contentSnippet");
        String firstTitle = jsonArray_results.getJSONObject(0).getString("title");
        String firstUrl = jsonArray_results.getJSONObject(0).getString("url");
        ArrayList<SearchResult> results = new ArrayList<>();
        ArrayList<SearchResult> wikipedias = new ArrayList<>();
        ArrayList<SearchResult> others = new ArrayList<>();
        for(int i = 0; i < jsonArray_results.length(); i++){
            JSONObject resultJson = jsonArray_results.getJSONObject(i);
            SearchResult searchResult = new SearchResult();

            if (resultJson.getString("url").contains("wikipedia.org")) {
                searchResult.title = resultJson.getString("title");
                searchResult.content = "</a><br/>" + resultJson.getString("contentSnippet") + "...<br/><br/>";
                searchResult.url = resultJson.getString("url");
                wikipedias.add(searchResult);
            }else{
                searchResult.title = resultJson.getString("title");
                searchResult.content = "</a><br/>" + resultJson.getString("contentSnippet") + "...<br/><br/>";
                searchResult.url = resultJson.getString("url");
                others.add(searchResult);
            }
        }
        for (SearchResult result : wikipedias) {
            results.add(result);
        }
        for (SearchResult result : others) {
            results.add(result);
        }
        return results;
    }
}


