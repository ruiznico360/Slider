package mobile.slider.app.slider.util;

import android.content.Context;
import android.widget.Toast;

import mobile.slider.app.slider.settings.SettingsHandler;

public class CustomToast {
    public static Toast t;
    public static void makeToast(String text) {
        if (t != null) {
            t.cancel();
        }
        t = Toast.makeText(SettingsHandler.appContext,text,Toast.LENGTH_LONG);
        t.show();
    }
}
