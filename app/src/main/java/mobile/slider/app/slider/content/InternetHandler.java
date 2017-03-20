package mobile.slider.app.slider.content;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.internetutil.HttpClient;
import mobile.slider.app.slider.internetutil.SearchResult;
import mobile.slider.app.slider.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternetHandler {
    private Activity act;
    private ContentFragment frag;
    private EditText searchEditText;
    private ImageView searchButton,poweredByGoogle;
    private ListView searchResults;
    private TextView emergencyMessage;
    private ProgressBar progress;
    private SearchJson currentQuery;
    private WebView webView;
    public InternetHandler(Activity c,ContentFragment frag) {
        act = c;
        this.frag = frag;
//        searchEditText = (EditText) frag.getView().findViewById(R.id.searchInternetEditText);
//        searchButton = (ImageView) frag.getView().findViewById(R.id.searchInternetButton);
//        poweredByGoogle = (ImageView) frag.getView().findViewById(R.id.poweredByGoogle);
//        searchResults = (ListView) frag.getView().findViewById(R.id.searchResultsListView);
//        progress = (ProgressBar) frag.getView().findViewById(R.id.searchProgress);
//        emergencyMessage = (TextView) frag.getView().findViewById(R.id.search_internet_emergency_message);
//
//        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId== EditorInfo.IME_ACTION_SEARCH){
//                    sendQuery(searchEditText.getText().toString());
//                }
//                return false;
//            }
//        });
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               sendQuery(searchEditText.getText().toString());
//            }
//        });
        webView = (WebView)frag.layoutResourceView;
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ((url.contains("http://www.google") && url.contains("/search?q=")) || (url.contains("https://www.google") && url.contains("/search?q="))) {
                    return false;
                }else{
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
            }
        });
        webView.loadUrl("https://www.google.com/search?q=hello");

    }
    public void sendQuery(String query) {
        emergencyMessage.setVisibility(View.INVISIBLE);
        if (!query.isEmpty()) {
            if (currentQuery != null) {
                currentQuery.cancel(true);
            }
            View focused = act.getCurrentFocus();
            if (focused != null) {
                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
            progress.setVisibility(View.VISIBLE);
            currentQuery = new SearchJson();
            currentQuery.query = query;
            currentQuery.execute();
        }
    }
    private class SearchJson extends AsyncTask<Void,Void,Void> {
        public String query;
        ArrayList<SearchResult> searchResult;
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpClient client = new HttpClient(query);
            try {
                searchResult = client.parseResult(client.sendQuery());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.setVisibility(View.INVISIBLE);
            if (searchResult == null) {
                emergencyMessage.setVisibility(View.VISIBLE);
                emergencyMessage.setText("Unable to connect to internet. Your device is offline");
                return;
            }else if (searchResult.isEmpty()) {
                emergencyMessage.setVisibility(View.VISIBLE);
                emergencyMessage.setText(Html.fromHtml("<br/>Your search: <b>" + query.replaceAll("\\s+", " ") + "</b> did not match any results<br/>"));
                return;
            }
            SearchAdapter adapter = new SearchAdapter(searchResult);
            searchResults.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            searchResults.setHorizontalScrollBarEnabled(true);
            super.onPostExecute(result);
        }
    }
    private class SearchAdapter extends ArrayAdapter<SearchResult> {
        private List<SearchResult> results;
        public SearchAdapter(List<SearchResult> results) {
            super(act, R.layout.search_result,results);
            this.results = results;
        }
        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public SearchResult getItem(int position) {
            return results.get(position);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final ViewHolder holder;
            if (view == null || (view.getTag() == null)) {
                holder = new ViewHolder();
                view = LayoutInflater.from(act).inflate(R.layout.search_result, null);
                holder.result = getItem(position);
                holder.title = (TextView) view.findViewById(R.id.search_result_title);
                holder.content = (TextView) view.findViewById(R.id.search_result_content);
                holder.divider = (ImageView) view.findViewById(R.id.search_result_divider);
                holder.url = (TextView) view.findViewById(R.id.search_result_url);
                holder.titleQuickAnswer = (TextView) view.findViewById(R.id.search_result_title_quick_answer);
                holder.urlQuickAnswer = (TextView) view.findViewById(R.id.search_result_url_quick_answer);
            }else{
                holder = (ViewHolder) view.getTag();
            }
            if (position != 0) {
                holder.title.setText(Html.fromHtml(holder.result.title));
                holder.content.setText(Html.fromHtml(holder.result.content));
                holder.url.setText(holder.result.url);

                holder.title.setOnTouchListener(Util.darkenAsPressed(new Runnable() {
                    @Override
                    public void run() {
                        act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(holder.result.url)));
                    }
                }));
            }else{
                holder.title.setVisibility(View.GONE);
                holder.url.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
                holder.content.setText(Html.fromHtml(holder.result.content) + "\n");
                holder.titleQuickAnswer.setVisibility(View.VISIBLE);
                holder.titleQuickAnswer.setText(Html.fromHtml(holder.result.title));
                holder.urlQuickAnswer.setVisibility(View.VISIBLE);
                holder.urlQuickAnswer.setText((holder.result.url));
                holder.titleQuickAnswer.setOnTouchListener(Util.darkenAsPressed(new Runnable() {
                    @Override
                    public void run() {
                        act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(holder.result.url)));
                    }
                }));
            }
            return view;
        }
        private class ViewHolder {
            public SearchResult result;
            public TextView title;
            public TextView content;
            public ImageView divider;
            public TextView url;
            public TextView titleQuickAnswer,urlQuickAnswer;
        }
    }
}
