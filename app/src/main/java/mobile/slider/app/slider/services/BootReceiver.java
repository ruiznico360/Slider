package mobile.slider.app.slider.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        Util.log(intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CustomToast.makeToast("startup");
            if (SystemOverlay.service == null) {
                SettingsWriter.init(con);
                if (SettingsUtil.checkPermissions(con)) {
                    SystemOverlay.start(con, null);
                }
            }
        }else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Util.log("OFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFf");
            ActivityManager manager = (ActivityManager) con.getSystemService(Context.ACTIVITY_SERVICE);
            boolean running = false;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (SystemOverlay.class.getName().equals(service.service.getClassName())) {
                    running = true;
                }
            }
            if (running) {
               CustomToast.makeToast("power off service running");
                if (SystemOverlay.floaterMovement.inTouch) {
                    SystemOverlay.floaterMovement.forceUp();
                }
            }
        }
    }
}
