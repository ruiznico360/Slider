package mobile.slider.app.slider.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsHandler;
public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        if (SystemOverlay.service == null) {
            SettingsHandler.init(con);
            SettingsHandler.refreshSettings();
            if(SettingsHandler.checkForPermissions()) {
                Intent i = new Intent(con, SystemOverlay.class);
                con.startService(i);
            }
        }
    }
}
