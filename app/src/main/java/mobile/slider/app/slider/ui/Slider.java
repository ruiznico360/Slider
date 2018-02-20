package mobile.slider.app.slider.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.util.IntentExtra;

public class Slider extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Setup.hasAllReqPermissions(this)) {
            checkForServiceEnabled();
            finish();
        }else{
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
        } else {
            UserInterface.launchUI();
        }
    }
}