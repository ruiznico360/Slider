package mobile.slider.app.slider.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

import static android.content.Context.WINDOW_SERVICE;

public class PowerReceiver extends BroadcastReceiver {
    public PowerReceiver() {
    }

    @Override
    public void onReceive(Context con, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            if (UserInterface.running) {
                UserInterface.remove(con);
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (UserInterface.running && Util.isLocked(con)) {
                UserInterface.remove(con);
            }
        }
    }
}
