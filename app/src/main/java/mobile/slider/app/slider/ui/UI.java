package mobile.slider.app.slider.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.SView.SWindowLayout;
import mobile.slider.app.slider.model.window.Window;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.util.Util;

import static android.content.Context.WINDOW_SERVICE;

public class UI {
    public static boolean running = false;
    public static SWindowLayout uiLayout;
    public static View userInterface(Context c) {
        View ui = ((LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ui, null);
        ui.findViewById(R.id.ui_main_layout).setBackgroundColor(SettingsUtil.getBackgroundColor());
        return ui;
    }
    public static class UILayout extends RelativeLayout {
        Context c;
        public UILayout(Context c){
            super(c);
            this.c = c;
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) || (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH) || (event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
                if (running) {
                    remove(c);
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
    public static void launchUI() {
        if (UI.running) {
            return;
        }
        if (SystemOverlay.service.floater.floaterMovement.currentlyInTouch) {
            SystemOverlay.service.floater.floaterMovement.forceUp();
        }
        SystemOverlay.floater.hideFloater();
        int size;
        if (Util.screenHeight() > Util.screenWidth()) {
            size = Util.screenWidth() / 5;
        }else{
            size = Util.screenHeight() / 5;
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
            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        }else if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
            params.gravity = Gravity.LEFT | Gravity.BOTTOM;
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


        final UI.UILayout uiLayout = new UI.UILayout(SystemOverlay.service);
        UI.uiLayout = new SWindowLayout(uiLayout);
        View inner = UI.userInterface(SystemOverlay.service.getApplicationContext());
        uiLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int rx = (int)event.getRawX();
                    int ry = (int)event.getRawY();
                    int[] l = new int[2];
                    v.getLocationOnScreen(l);
                    int x = l[0];
                    int y = l[1];
                    int w = v.getWidth();
                    int h = v.getHeight();
                    if (rx < x || rx > x + w || ry < y || ry > y + h) {
                        if (UI.running) {
                            UI.remove(SystemOverlay.service.getApplicationContext());
                        }
                    }
                }
                return true;
            }
        });
        inner.findViewById(R.id.button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Window(SystemOverlay.service.service).create();
                    UI.remove(SystemOverlay.service.getApplicationContext());
                }
                return true;
            }
        });
        UI.uiLayout.plot(params);
        uiLayout.addView(inner);
        uiLayout.setVisibility(View.VISIBLE);

        uiLayout.updateViewLayout(inner, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (SystemOverlay.service.floater.floaterMovement.currentlyInTouch) {
            SystemOverlay.service.floater.floaterMovement.forceUp();
        }

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
                UI.running = true;
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        inner.startAnimation(a);
        final boolean phoneStatus = Util.isLocked(SystemOverlay.service.getApplicationContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (UI.running) {
                    if (phoneStatus != Util.isLocked(SystemOverlay.service.getApplicationContext())) {
                        UI.remove(SystemOverlay.service.getApplicationContext());
                    }else if (!Util.isScreenOn(SystemOverlay.service.getApplicationContext())) {
                        UI.remove(SystemOverlay.service.getApplicationContext());
                    }else {
                        new Handler().postDelayed(this, 500);
                    }
                }
            }
        }, 500);
    }
    public static void remove(final Context c) {
        running = false;
        Animation a;
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            a = AnimationUtils.loadAnimation(c, R.anim.from_middle_to_right);
        }else {
            a = AnimationUtils.loadAnimation(c, R.anim.from_middle_to_left);
        }
        final View uiLayout = UI.uiLayout.layout;
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ((WindowManager)c.getSystemService(WINDOW_SERVICE)).removeView(uiLayout);
                UI.uiLayout = null;
                SystemOverlay.floater.showFloater();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        uiLayout.findViewById(R.id.ui_main_layout).startAnimation(a);
    }
}
