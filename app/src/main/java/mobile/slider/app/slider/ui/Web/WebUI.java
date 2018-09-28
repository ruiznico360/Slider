package mobile.slider.app.slider.ui.Web;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.lang.UScript;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class WebUI {
    public Context c;
    public SView mainLayout, web,loading;

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
        UserInterface.UI.resize(UserInterface.relativeWidth() / 2, Util.screenHeight());
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        web = new SView(new WebView(c), mainLayout.view);
        web.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
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
        loading = new SView(new ProgressBar(c), mainLayout.view);
        loading.plot();
        loading.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(wUnit(100))
                .addRule(RelativeLayout.CENTER_IN_PARENT)
                .save();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!UserInterface.running()) return;
                if (UserInterface.shouldMove()) {
                    ((WebView) web.view).loadUrl("https://www.google.com/search?q=hello");
                }else{
                    new Handler().post(this);
                }
            }
        });
    }
}