package mobile.slider.app.slider.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.settings.resources.SettingType;
import mobile.slider.app.slider.util.Util;

public class PermissionsInterface extends AppCompatActivity {
    public static final int SYSTEM_ALERT_WINDOW_CODE = 1;
    Button retryButton;
    TextView permissionDenied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_interface);
        init();
    }
    public void init() {
        SettingsWriter.init(this);
        retryButton = (Button) findViewById(R.id.retryButton);
        permissionDenied = (TextView) findViewById(R.id.permissionDenied);
        if (Build.VERSION.SDK_INT >= 23) {
            Util.log(Settings.canDrawOverlays(this) + "");
            requestSystemAlertWindow();
        }
    }
    public void requestSystemAlertWindow() {
        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(myIntent, SYSTEM_ALERT_WINDOW_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (requestCode == SYSTEM_ALERT_WINDOW_CODE) {
                if (Settings.canDrawOverlays(this)) {
                    SettingsUtil.setPerms(true);
                    Intent i = new Intent(this, UserInterface.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else {
                    retryButton.setVisibility(View.VISIBLE);
                    retryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestSystemAlertWindow();
                        }
                    });
                    permissionDenied.setText("Drawing over other apps is required for the functionality of the app. Please allow this by pressing retry.");
                    permissionDenied.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
