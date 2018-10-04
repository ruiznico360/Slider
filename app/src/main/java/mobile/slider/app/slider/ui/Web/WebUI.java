package mobile.slider.app.slider.ui.Web;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.ui.UIClass;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class WebUI extends UIClass {
    public Context c;
    public SView mainLayout, web, webContainer,loading, searchBarLayout, logo, searchEdit, clearButton;
    public int searchBarHeight;

    public String getID() {
        return UserInterface.WEB_WINDOW;
    }

    public WebUI(Context c) {
        this.c = c;
    }

    public void setup() {
        UserInterface.UI.resize(Util.displayWidth() / 2);
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        searchBarHeight = Util.getStatusBarHeight() * 2;

        webContainer = new SView(new RelativeLayout(c), mainLayout.view);
        webContainer.view.setBackgroundColor(Color.rgb(220,220,220));
        webContainer.view.setPadding(wUnit(2),0,wUnit(2),0);
        webContainer.plot();
        webContainer.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT)
                .save();

        web = new SView(new WebView(c), webContainer.view);
        web.plot();
        web.openRLayout()
                .setWidth(RelativeLayout.LayoutParams.MATCH_PARENT)
                .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT)
                .save();

        loading = new SView(new ProgressBar(c), mainLayout.view);
        loading.plot();
        loading.view.setVisibility(View.INVISIBLE);
        loading.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(wUnit(100))
                .addRule(RelativeLayout.CENTER_IN_PARENT)
                .save();

        logo = new SView(new ImageView(c), mainLayout.view);
        ImageUtil.setImageDrawable(logo.view, R.drawable.powered_by_google);
        logo.plot(wUnit(100), ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.powered_by_google),wUnit(100)));

        searchBarLayout = new SView(new RelativeLayout(c), mainLayout.view);
        searchBarLayout.view.setBackgroundColor(Color.rgb(220,220,220));
        searchBarLayout.view.setPadding(wUnit(2),wUnit(2),wUnit(2),wUnit(2));
        searchBarLayout.plot();
        searchBarLayout.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(searchBarHeight)
                .setTopM(logo.height())
                .save();

        searchEdit = new SView(Util.customEdit(c), searchBarLayout.view);
        ((EditText)searchEdit.view).setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        ((EditText)searchEdit.view).setInputType(EditorInfo.TYPE_CLASS_TEXT);
        searchEdit.view.setBackgroundColor(Color.WHITE);

        searchEdit.plot();
        searchEdit.openRLayout()
                .setWidth(RelativeLayout.LayoutParams.MATCH_PARENT)
                .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT)
                .addRule(RelativeLayout.CENTER_VERTICAL)
                .save();

        clearButton = new SView(new ImageView(c), searchBarLayout.view);
        clearButton.view.setVisibility(View.INVISIBLE);
        ImageUtil.setImageDrawable(clearButton.view, R.drawable.window_close_icon);
        clearButton.plot();
        clearButton.openRLayout()
                .setWidth(wUnit(15))
                .setHeight(wUnit(25))
                .addRule(RelativeLayout.CENTER_VERTICAL)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .save();

        searchEdit.view.setPadding(0,0,clearButton.width(),0);
        
        searchEdit.post(new Runnable() {
            @Override
            public void run() {
                ((EditText)searchEdit.view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH && !v.getText().toString().equals("") && v.getText().toString().trim().length() > 0) {
                            performSearch();
                        }
                        return true;
                    }
                });
                searchEdit.view.requestFocus();
                ((InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(searchEdit.view,InputMethodManager.SHOW_IMPLICIT);
            }
        });

        mainLayout.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom != bottom && logo.view.getVisibility() == View.VISIBLE) {
                    if (logo.height() + searchBarHeight > mainLayout.height()) {
                        searchBarLayout.openRLayout().setTopM(mainLayout.height() - searchBarHeight).setHeight(searchBarHeight).save();
                    }else{
                        searchBarLayout.openRLayout().setTopM(logo.height()).save();
                    }
                }
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (UserInterface.running()) {
//                    Util.log(searchBarLayout.y() + "omegalul");
//                    new Handler().postDelayed(this,5);
//                }
//            }
//        },5);
        ((TextView) searchEdit.view).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    clearButton.view.setVisibility(View.INVISIBLE);
                }else{
                    clearButton.view.setVisibility(View.VISIBLE);
                }
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
                if ((url.contains("http://www.google.") || url.contains("https://www.google.")) && url.contains("/search?") && !url.contains("/search?nomo")) {
                    ((EditText)searchEdit.view).setText(unpackSearch(url.substring(url.indexOf("q=") + 2,url.indexOf("&", url.indexOf("q=")))));
                    return false;
                }else {
                    final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    if (Util.isLocked(c)) {
                        SystemOverlay.periodicRunnableHandler.tasks.add(new Runnable() {
                            @Override
                            public void run() {
                                if (!Util.isLocked(c)) {
                                    c.startActivity(i);
                                    SystemOverlay.periodicRunnableHandler.tasks.remove(this);
                                }
                            }
                        });
                    }else{
                        c.startActivity(i);
                    }

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

        clearButton.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) searchEdit.view).setText("");
            }
        });
    }
    public String packSearch(String search) {
        return search
                .replace("&","%26")
                .replace("#","%23")
                .replace("+","%2B")
                .replace("?","%3F");

    }
    public String unpackSearch(String url) {
        return url
                .replace("+", " ")
                .replace("%2B", "+")
                .replace("%3F", "?")
                .replace("%23", "#")
                .replace("%26", "&")
                .replace("%20", " ");
    }
    public void performSearch() {
        if (searchEdit != null && searchEdit.view.hasFocus()) {
            hideKeyboard();
        }
        final String url = "http://www.google.com/search?q=" + packSearch(((EditText)searchEdit.view).getText().toString());
        ((EditText)searchEdit.view).setText(unpackSearch(url.substring(url.indexOf("q=") + 2,url.length())));

        if (logo.view.getVisibility() == View.VISIBLE) {
            final Anim a = UserInterface.uiAnim(c,searchBarLayout,100);
            a.setEnd(new Runnable() {
                @Override
                public void run() {
                    ((WebView)web.view).loadUrl(url);
                    searchBarLayout.openRLayout().setTopM(0).save();
                }
            });
            a.setStart(new Runnable() {
                @Override
                public void run() {
                    a.addTranslate(0,-searchBarLayout.openRLayout().topM);
                    logo.view.setVisibility(View.INVISIBLE);
                }
            });
            a.start();
        }else{
            ((WebView)web.view).loadUrl(url);
        }
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