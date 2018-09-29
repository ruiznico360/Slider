package mobile.slider.app.slider.ui.Web;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.lang.UScript;
import android.net.Uri;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.ui.UIClass;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class WebUI extends UIClass {
    public Context c;
    public SView mainLayout, web,loading, searchBarLayout, searchEdit, searchButton;
    public int searchBarHeight;

    public String getID() {
        return UserInterface.WEB_WINDOW;
    }
    public int wUnit(int percent) {
        return (int) (UserInterface.UI.container.width() / 100f * percent);
    }

    public int hUnit(int percent) {
        return (int) (UserInterface.UI.container.height() / 100f * percent);
    }

    public WebUI(Context c) {
        this.c = c;
    }

    public void setup() {
        UserInterface.UI.resize(UserInterface.relativeWidth() / 2);
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        searchBarHeight = Util.getStatusBarHeight() * 2;

        web = new SView(new WebView(c), mainLayout.view);
        web.plot();
        web.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .save();

        loading = new SView(new ProgressBar(c), mainLayout.view);
        loading.plot();
        loading.view.setVisibility(View.INVISIBLE);
        loading.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(wUnit(100))
                .addRule(RelativeLayout.CENTER_IN_PARENT)
                .save();

        searchBarLayout = new SView(new RelativeLayout(c), mainLayout.view);
        searchBarLayout.view.setBackgroundColor(Color.rgb(220,220,220));
        searchBarLayout.view.setPadding(wUnit(2),wUnit(2),wUnit(2),wUnit(2));
        searchBarLayout.plot(wUnit(100),searchBarHeight);

        searchButton = new SView(new ImageView(c), searchBarLayout.view);
        Util.generateViewId(searchButton.view);
        ImageUtil.setImageDrawable(searchButton.view, R.drawable.search_icon);
        searchButton.plot();
        searchButton.openRLayout()
                .setWidth(wUnit(25))
                .setHeight(wUnit(25))
                .addRule(RelativeLayout.CENTER_VERTICAL)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .save();

        searchEdit = new SView(new EditText(c), searchBarLayout.view);
        ((EditText)searchEdit.view).setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        ((EditText)searchEdit.view).setMaxLines(1);
        ((EditText)searchEdit.view).setInputType(EditorInfo.TYPE_CLASS_TEXT);
        searchEdit.view.setBackgroundColor(Color.WHITE);
        searchEdit.plot();
        searchEdit.openRLayout()
                .setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT)
                .setHeight(wUnit(25))
                .addRule(RelativeLayout.LEFT_OF, searchButton.view.getId())
                .addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                .addRule(RelativeLayout.CENTER_VERTICAL)
                .save();

        searchEdit.post(new Runnable() {
            @Override
            public void run() {
                ((EditText)searchEdit.view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            performSearch();
                            return true;
                        }
                        return false;
                    }
                });
                searchEdit.view.requestFocus();
                ((InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(searchEdit.view,InputMethodManager.SHOW_IMPLICIT);
            }
        });

        searchButton.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
        searchBarLayout.view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ((WebView)web.view).setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ((url.contains("http://www.google") && url.contains("/search?q=")) || (url.contains("https://www.google") && url.contains("/search?q="))) {
                    return false;
                }else{
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(i);
                    UserInterface.UI.remove();
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loading.view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loading.view.setVisibility(View.INVISIBLE);
            }
        });
    }
    public void performSearch() {
        if (searchEdit != null && searchEdit.view.hasFocus()) {
            hideKeyboard();
        }
        ((WebView)web.view).loadUrl("http://www.google.com/search?q=" + ((EditText)searchEdit.view).getText());
    }
    public void backPressed() {
        if (searchEdit != null && searchEdit.view.hasFocus()) {
            hideKeyboard();
        }else{
            UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
        }
    }
    public void remove() {
        if (searchEdit != null && searchEdit.view.hasFocus()) {
            hideKeyboard();
        }
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEdit.view.getWindowToken(), 0);
        searchEdit.view.clearFocus();
    }
}