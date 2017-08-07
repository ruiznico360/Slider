package mobile.slider.app.slider.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.util.IntentExtra;

public class Slider extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (canUseOverlay(this)) {
            checkForServiceEnabled();
            finish();
        }else{
            finish();
            startActivity(new Intent(this, Setup.class));
        }
    }
    public static boolean canUseOverlay(Context c) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(c)) {
                return true;
            }
        }else{
            return true;
        }
        return false;
    }
    public void checkForServiceEnabled() {
        if (SystemOverlay.service == null) {
            if (getIntent().getExtras() != null && getIntent().hasExtra(IntentExtra.SAFE_REBOOT_SERVICE)) {
                SystemOverlay.start(getBaseContext(), IntentExtra.SAFE_REBOOT_SERVICE);
            }else {
                SystemOverlay.start(getBaseContext(), IntentExtra.FROM_UI);
            }
        } else {
            SystemOverlay.service.launchUI();
        }
    }
}