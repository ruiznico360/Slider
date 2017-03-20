package mobile.slider.app.slider.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Nicolas on 2016-03-13.
 */
public class CustomToast {
    public static Toast t;
    public static Context c;
    public static void makeToast(String text) {
        if (t != null) {
            t.cancel();
        }
        t = Toast.makeText(c,text,Toast.LENGTH_LONG);
        t.show();
    }
}
