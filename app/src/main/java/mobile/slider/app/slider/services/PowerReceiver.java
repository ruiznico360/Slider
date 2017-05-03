package mobile.slider.app.slider.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.Util;

public class PowerReceiver extends BroadcastReceiver {
    public PowerReceiver() {
    }

    @Override
    public void onReceive(Context con, Intent intent) {
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
