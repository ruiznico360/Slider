package mobile.slider.app.slider.services;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;


        import mobile.slider.app.slider.settings.SettingsWriter;
        import mobile.slider.app.slider.ui.activity.Setup;
        import mobile.slider.app.slider.ui.activity.Slider;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        if (SystemOverlay.service == null) {
            SettingsWriter.init(con);
            if (Setup.hasAllReqPermissions(con)) {
                Intent i = new Intent(con, Slider.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                con.startActivity(i);
            }
        }
    }
}
