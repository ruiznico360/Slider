package mobile.slider.app.slider.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.floater.Floater;
import mobile.slider.app.slider.services.NotificationListener;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.services.IntentExtra;
import mobile.slider.app.slider.util.Util;

import static android.service.notification.NotificationListenerService.requestRebind;

public class Slider extends Activity {
    public static long START_TIME;
    public boolean launchingSetup;
    public static boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        running = true;
        START_TIME = SystemClock.uptimeMillis();
        launchingSetup = false;

        super.onCreate(savedInstanceState);
        if (Setup.hasAllReqPermissions(this)) {
            setupActivity();
        }else{
            launchingSetup = true;
            launchSetupActivity();
        }
    }

    @Override
    protected void onPause() {
        if (!launchingSetup) {
            SystemOverlay.floater.showFloater(Floater.SHOW_DELAY);
        }
        launchingSetup = false;
        running = false;

        if (!isFinishing()) {
            finish();
        }
        super.onPause();
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
            SystemOverlay.floater.hideFloater();
            if (UserInterface.running()) {
                UserInterface.UI.remove();
            }
        }
    }

    public void terminateInvalidService() {
        if (SystemOverlay.service != null) {
            SystemOverlay.floater.hideFloater();
            SystemOverlay.service.stopSelf();
        }
    }
    public void setupActivity() {
        checkForServiceEnabled();
        setContentView(R.layout.notification_layout);
    }
    public void launchSetupActivity() {
        terminateInvalidService();
        finish();
        startActivity(new Intent(this, Setup.class));
    }

}