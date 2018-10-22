package mobile.slider.app.slider.services;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;


        import mobile.slider.app.slider.settings.SettingsWriter;
        import mobile.slider.app.slider.ui.activity.Setup;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        if (SystemOverlay.service == null) {
            SettingsWriter.init(con);
            if (Setup.hasAllReqPermissions(con)) {
                SystemOverlay.checkForServiceEnabled(SystemOverlay.IntentExtra.SAFE_REBOOT_SERVICE, con);
            }
        }
    }
}
