package mobile.slider.app.slider.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;

import mobile.slider.app.slider.services.NotificationListener;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

import static android.service.notification.NotificationListenerService.requestRebind;

public class Slider extends Activity {
    public static long START_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        START_TIME = SystemClock.uptimeMillis();
        super.onCreate(savedInstanceState);
        if (Setup.hasAllReqPermissions(this)) {
            checkForServiceEnabled();
            finish();
        }else{
            terminateInvalidService();
            finish();
            startActivity(new Intent(this, Setup.class));
        }
    }

    public void checkForServiceEnabled() {
        if (SystemOverlay.service == null) {
            if (getIntent().getExtras() != null && getIntent().hasExtra(IntentExtra.SAFE_REBOOT_SERVICE)) {
                SystemOverlay.start(getBaseContext(), IntentExtra.SAFE_REBOOT_SERVICE);
            }else {
                SystemOverlay.start(getBaseContext(), IntentExtra.FROM_UI);
            }
            if (Build.VERSION.SDK_INT >= 26) {
                requestRebind(new ComponentName(this, NotificationListener.class));
            }
        }else {
            UserInterface.launchUI();
        }
    }

    public void terminateInvalidService() {
        if (SystemOverlay.service != null) {
            SystemOverlay.service.stopSelf();
        }
    }
}