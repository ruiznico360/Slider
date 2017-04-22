package mobile.slider.app.slider.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

public class Restarter extends Service {
    public Restarter() {
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SystemOverlay.class.getName().equals(service.service.getClassName())) {
                running = true;
            }
        }
        if (!running) {
            Intent i = new Intent(getApplicationContext(),SystemOverlay.class);
            i.putExtra(IntentExtra.SAFE_REBOOT_SERVICE, true);
            startService(i);
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
