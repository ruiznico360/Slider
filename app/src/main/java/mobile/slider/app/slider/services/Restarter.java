package mobile.slider.app.slider.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.SettingsWriter;

public class Restarter extends BroadcastReceiver {
    public Restarter() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        con.startService(new Intent(con, SystemOverlay.class));
    }
}
