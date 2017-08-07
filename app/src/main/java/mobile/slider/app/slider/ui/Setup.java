package mobile.slider.app.slider.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mobile.slider.app.slider.R;


public class Setup extends Activity {
    public static final int SYSTEM_ALERT_WINDOW_CODE = 1;
    Button retryButton;
    TextView permissionDenied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_interface);
        retryButton = (Button) findViewById(R.id.retryButton);
        permissionDenied = (TextView) findViewById(R.id.permissionDenied);
        if (Build.VERSION.SDK_INT >= 23) {
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
                    finish();
                    Intent i = new Intent(this, Slider.class);
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
