package mobile.slider.app.slider.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.ui.Slider;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        if (SystemOverlay.service == null) {
            SettingsWriter.init(con);
            if (Slider.canUseOverlay(con)) {
                con.startActivity(new Intent(con, Slider.class));
            }
        }
    }
}
