package mobile.slider.app.slider.services;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;


        import mobile.slider.app.slider.settings.SettingsWriter;
        import mobile.slider.app.slider.ui.Slider;
        import mobile.slider.app.slider.util.IntentExtra;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    @Override
    public void onReceive(Context con, Intent intent) {
        if (SystemOverlay.service == null) {
            SettingsWriter.init(con);
            if (Slider.canUseOverlay(con)) {
                Intent i = new Intent(con, Slider.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(IntentExtra.SAFE_REBOOT_SERVICE,true);
                con.startActivity(i);
            }
        }
    }
}
