package mobile.slider.app.slider.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.SettingsWriter;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        if (SystemOverlay.service == null) {
            SettingsWriter.init(con);
            if(SettingsUtil.checkPermissions(con)) {
                SystemOverlay.start(con, null);
            }
        }
    }
}
