package mobile.slider.app.slider.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.floater.Floater;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

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

    public void terminateInvalidService() {
        if (SystemOverlay.service != null) {
            SystemOverlay.floater.hideFloater();
            SystemOverlay.service.stopSelf();
        }
    }
    public void setupActivity() {
        SystemOverlay.checkForServiceEnabled(SystemOverlay.IntentExtra.FROM_UI, this);
        if (UserInterface.running()) {
            UserInterface.UI.remove();
        }
        setContentView(R.layout.activity_permissions_interface);
    }
    public void launchSetupActivity() {
        terminateInvalidService();
        finish();
        startActivity(new Intent(this, Setup.class));
    }

}