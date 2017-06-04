package mobile.slider.app.slider.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobile.slider.app.slider.ui.UI;

public class PowerReceiver extends BroadcastReceiver {
    public PowerReceiver() {
    }

    @Override
    public void onReceive(Context con, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            if (UI.running) {
                UI.remove(con);
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (UI.running) {
                UI.remove(con);
            }
        }
    }
}
