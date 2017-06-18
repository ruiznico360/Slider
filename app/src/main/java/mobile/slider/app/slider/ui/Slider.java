package mobile.slider.app.slider.ui;

import android.animation.Animator;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.ContentFragment;
import mobile.slider.app.slider.content.InternetHandler;
import mobile.slider.app.slider.content.animations.ZoomAnimation;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.AppTheme;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.settings.resources.WindowShader;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

public class Slider extends Activity {
    public static final int SYSTEM_ALERT_WINDOW_CODE = 1;
    Button retryButton;
    TextView permissionDenied;
    public static int test = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (canUseOverlay(this)) {
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            checkForServiceEnabled();
            finish();
        }else{
            init();
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
            SystemOverlay.start(getBaseContext(), IntentExtra.FROM_UI);
        } else {
            SystemOverlay.service.launchUI();
        }
    }
    public void init() {
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