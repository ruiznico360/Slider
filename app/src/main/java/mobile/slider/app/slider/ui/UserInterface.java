package mobile.slider.app.slider.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.SView.SView;
import mobile.slider.app.slider.content.SView.SWindowLayout;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class UserInterface {
    public static UserInterface UI;

    public Context c;
    public Runnable deviceStateRunnable;
    public SWindowLayout container;
    public SView inner;
    public MainUI mainUI;

    public UserInterface(Context c) {
        this.c = c;
    }

    public static void launchUI() {
        if (UI.running()) {
            return;
        }
        if (SystemOverlay.floater.floaterMovement.currentlyInTouch) {
            SystemOverlay.floater.floaterMovement.forceUp();
        }
        SystemOverlay.floater.hideFloater();

        UserInterface ui = new UserInterface(SystemOverlay.service);
        ui.setup();
    }

    public void setup() {
        float WUNIT, HUNIT;
        int size;
        if (Util.screenHeight() > Util.screenWidth()) {
            size = Util.screenWidth() / 4;
            HUNIT = Util.screenHeight() / 100f;
            WUNIT = size / 100f;
        }else{
            size = Util.screenHeight() / 4;
            HUNIT = Util.screenWidth() / 100f;
            WUNIT = size / 100f;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = size;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD + WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        if (Util.isLocked(SystemOverlay.service.getApplicationContext())) {
            if (params.type != WindowManager.LayoutParams.TYPE_SYSTEM_ERROR) {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
        }else{
            if (params.type != WindowManager.LayoutParams.TYPE_PHONE) {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
        }
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            params.gravity = Gravity.BOTTOM;
            params.x = Util.screenWidth() - params.width;
        }else if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
            params.gravity = Gravity.BOTTOM;
            params.x = 0;
        }


        if (SystemOverlay.service.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (params.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        }else{
            if (params.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            }
        }
        container = new SWindowLayout(new UIContainer(c));
        inner = new SView(((LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ui, null), container);

        inner.view.findViewById(R.id.ui_main_layout).setBackgroundColor(SettingsUtil.getBackgroundColor());

        container.layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (running()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        int[] loc = new int[2];
                        container.layout.getLocationOnScreen(loc);
                        int rx = (int) event.getRawX();
                        int x = loc[0];
                        int w = container.width();
                        if (rx < x || rx > x + w ) {
                            UI.remove();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        mainUI = new MainUI(WUNIT, HUNIT, c, inner);
        mainUI.setup();

        container.plot(params);
        inner.plot();
        container.layout.setVisibility(View.VISIBLE);

        SView.Layout editor = inner.openLayout();
        editor.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        editor.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        editor.save();

        Animation a;
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            a = AnimationUtils.loadAnimation(SystemOverlay.service.getApplicationContext(), R.anim.from_right_to_middle);
        }else {
            a = AnimationUtils.loadAnimation(SystemOverlay.service.getApplicationContext(), R.anim.from_left_to_middle);
        }
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                UI = UserInterface.this;
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        inner.view.startAnimation(a);

        final boolean phoneStatus = Util.isLocked(SystemOverlay.service.getApplicationContext());
        deviceStateRunnable = new Runnable() {
            @Override
            public void run() {
                if (running()) {
                    if (phoneStatus != Util.isLocked(SystemOverlay.service.getApplicationContext())) {
                        remove();
                        SystemOverlay.deviceStateListener.tasks.remove(this);
                    }else if (!Util.isScreenOn(SystemOverlay.service.getApplicationContext())) {
                        remove();
                        SystemOverlay.deviceStateListener.tasks.remove(this);
                    }
                }
            }
        };
        SystemOverlay.deviceStateListener.tasks.add(deviceStateRunnable);
    }

    public void backPressed() {
        if (running()) {
            remove();
        }
    }
    public void remove() {
        if (SystemOverlay.deviceStateListener.tasks.contains(deviceStateRunnable)) {
            SystemOverlay.deviceStateListener.tasks.remove(deviceStateRunnable);
        }

        deviceStateRunnable = null;

        Animation a;
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            a = AnimationUtils.loadAnimation(c, R.anim.from_middle_to_right);
        }else {
            a = AnimationUtils.loadAnimation(c, R.anim.from_middle_to_left);
        }
        final UserInterface deleteUI = UI;
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                deleteUI.container.remove();
                SystemOverlay.floater.showFloater();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        inner.view.startAnimation(a);
        UI = null;
    }
    public static boolean running() {
        return UI != null;
    }
    public class UIContainer extends RelativeLayout {
        public UIContainer(Context c){
            super(c);
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) || (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH) || (event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
                backPressed();
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
