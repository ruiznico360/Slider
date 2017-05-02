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
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (SystemOverlay.service == null) {
                SettingsWriter.init(con);
                if (SettingsUtil.checkPermissions(con)) {
                    SystemOverlay.start(con, null);
                }
            }
        }else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (SystemOverlay.service != null) {
                if (SystemOverlay.floaterMovement.inTouch) {
                    SystemOverlay.floaterMovement.forceUp();
                }
            }
        }
    }
}
