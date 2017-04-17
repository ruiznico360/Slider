package mobile.slider.app.slider.util;

import android.widget.Toast;

import mobile.slider.app.slider.settings.SettingsWriter;

public class CustomToast {
    public static Toast t;
    public static void makeToast(String text) {
        if (t != null) {
            t.cancel();
        }
        t = Toast.makeText(SettingsWriter.appContext,text,Toast.LENGTH_LONG);
        t.show();
    }
}
