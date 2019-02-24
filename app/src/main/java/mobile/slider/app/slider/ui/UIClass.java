package mobile.slider.app.slider.ui;

import android.content.Context;
import android.os.Handler;

import mobile.slider.app.slider.util.Util;

public abstract class UIClass {
    public abstract String getID();
    public abstract void backPressed();
    private Handler handler;
    public void setup() {
        enableHandler();
    }
    public void remove() {
        disbleHandler();
    }

    public void enableHandler() {
        handler = new Handler();
    }

    public void disbleHandler() {
        handler.removeCallbacksAndMessages(null);
    }

    public int wUnit(int percent) {
        return (int)(UserInterface.UI.container.width() / 100f * percent);
    }
    public int hUnit(int percent) {
        return (int)(Util.displayHeight() / 100f * percent);
    }

    public void postDelayed(Runnable r, long millis) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (UserInterface.running() && UserInterface.UI.currentView == UIClass.this) {
                    r.run();
                }
            }
        }, millis);
    }
}
