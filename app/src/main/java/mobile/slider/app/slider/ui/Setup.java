package mobile.slider.app.slider.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    public static final int READ_CONTACTS_CODE = 444;
    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
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
    public static boolean hasAllReqPermissions(Context c) {
        if (canUseOverlay(c) && canReadContacts(c)) {
            return true;
        }
        return false;
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
    public static boolean canReadContacts(Context c) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (c.checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public void requestReadContactsWindow() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                requestPermissions(new String[]{READ_CONTACTS}, READ_CONTACTS_CODE);
            } else {
                requestPermissions(new String[]{READ_CONTACTS}, READ_CONTACTS_CODE);
            }
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
                if (canReadContacts(this)) {
                    finish();
                    Intent i = new Intent(this, Slider.class);
                    startActivity(i);
                }else {
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
            }else if (requestCode == READ_CONTACTS_CODE) {
                if (hasAllReqPermissions(this)) {
                    finish();
                    Intent i = new Intent(this, Slider.class);
                    startActivity(i);
                }else{
                    retryButton.setVisibility(View.VISIBLE);
                    retryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestSystemAlertWindow();
                        }
                    });
                    permissionDenied.setText("Reading contacts is required for this app!");
                    permissionDenied.setVisibility(View.VISIBLE);                }
            }
        }
    }
}
