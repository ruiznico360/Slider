package mobile.slider.app.slider.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.SView.SView;
import mobile.slider.app.slider.content.SView.SWindowLayout;
import mobile.slider.app.slider.content.animations.ZoomAnimation;
import mobile.slider.app.slider.content.fragments.UIFragment;
import mobile.slider.app.slider.model.window.Window;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.util.Contact;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

import static android.content.Context.WINDOW_SERVICE;

public class UserInterface {
    public static UserInterface UI;

    public float WUNIT, HUNIT;
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
        int size;
        if (Util.screenHeight() > Util.screenWidth()) {
            size = Util.screenWidth() / 4;
            HUNIT = Util.screenHeight() / 100;
            WUNIT = size / 100f;
        }else{
            size = Util.screenHeight() / 4;
            HUNIT = Util.screenWidth() / 100;
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
                        int rx = (int) event.getRawX();
                        int ry = (int) event.getRawY();
                        int x = container.x();
                        int y = container.y();
                        int w = container.width();
                        int h = container.height();
                        if (rx < x || rx > x + w || ry < y || ry > y + h) {
                            UI.remove();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
//        inner.view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (running()) {
//                    new Window(SystemOverlay.service).create();
//                    UI.remove();
//                    Contact.retrieveContacts(new ArrayList<Contact>());
//                }
//            }
//        });
//        inner.view.findViewById(R.id.button).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                if (running()) {
//                    SettingsUtil.setBackgroundColor(Color.rgb(new Random().nextInt(255),new Random().nextInt(255),new Random().nextInt(255)));
//                }
//                return true;
//            }
//        });
        mainUI = new MainUI();
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
    public class MainUI {
        public ViewPager uiSelector;
        public ListView phoneApps, quickApps, windowApps, dummyPhoneApps, dummyWindowApps;
        public ImageView logo, uiPos, uiIndicatorText;
        public RelativeLayout mainLayout;

        public void setup() {
            uiSelector = new ViewPager(c);
            quickApps = new ListView(c);
            phoneApps = new ListView(c);
            windowApps = new ListView(c);
            dummyPhoneApps = new ListView(c);
            dummyWindowApps = new ListView(c);

            logo = new ImageView(c);
            uiPos = new ImageView(c);
            uiIndicatorText = new ImageView(c);
            mainLayout = inner.view.findViewById(R.id.ui_main_layout);


            mainLayout.addView(uiSelector);
            mainLayout.addView(uiPos);
            mainLayout.addView(uiIndicatorText);
            mainLayout.addView(logo);

            uiIndicatorText.setImageDrawable(ImageUtil.getDrawable(R.drawable.quick_apps_title));
            Util.generateViewId(uiIndicatorText);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) uiIndicatorText.getLayoutParams();
            params.topMargin = (int) (HUNIT * 3);
            params.width = (int) (WUNIT * 100);
            params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.quick_apps_title), params.width);
            mainLayout.updateViewLayout(uiIndicatorText, params);

            uiPos.setImageDrawable(ImageUtil.getDrawable(R.drawable.main_ui_indicator_center));
            Util.generateViewId(uiPos);
            params = (RelativeLayout.LayoutParams) uiPos.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, uiIndicatorText.getId());
            params.width = (int) (WUNIT * 100);
            params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.main_ui_indicator_center), params.width);
            mainLayout.updateViewLayout(uiPos, params);

            logo.setImageDrawable(ImageUtil.getDrawable(R.drawable.app_logo));
            Util.generateViewId(logo);
            params = (RelativeLayout.LayoutParams) logo.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.width = (int) (WUNIT * 100);
            params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.app_logo), params.width);
            mainLayout.updateViewLayout(logo, params);

            dummyWindowApps.setBackgroundColor(Color.GREEN);
            phoneApps.setBackgroundColor(Color.CYAN);
            quickApps.setBackgroundColor(Color.RED);
            windowApps.setBackgroundColor(Color.GREEN);
            dummyPhoneApps.setBackgroundColor(Color.CYAN);

            ArrayList<View> pages = new ArrayList<>();
            pages.add(dummyWindowApps);
            pages.add(phoneApps);
            pages.add(quickApps);
            pages.add(windowApps);
            pages.add(dummyPhoneApps);

            params = (RelativeLayout.LayoutParams) uiSelector.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, uiPos.getId());
            params.addRule(RelativeLayout.ABOVE, logo.getId());
            params.width = (int) (WUNIT * 100);
            mainLayout.updateViewLayout(uiSelector, params);

            uiSelector.setBackgroundColor(Color.MAGENTA);
            uiSelector.setOverScrollMode(View.OVER_SCROLL_NEVER);
            uiSelector.setAdapter(new UIFragment.Adapter(c, pages));
            uiSelector.setCurrentItem(2, false);
            uiSelector.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                private float mLastPositionOffset = 0f;

                @Override
                public void onPageSelected(int position) {
                    if (position == 1) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_left);
                    }else if (position == 2) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_center);
                    }else if (position == 3) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_right);
                    }
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
                }

                @Override
                public void onPageScrollStateChanged (int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        int curr = uiSelector.getCurrentItem();
                        int lastReal = uiSelector.getAdapter().getCount() - 2;
                        if (curr == 0) {
                            uiSelector.setCurrentItem(lastReal, false);
                        } else if (curr > lastReal) {
                            uiSelector.setCurrentItem(1, false);
                        }
                    }
                }
            });
//            uiSelector.setPageTransformer(false, new ViewPager.PageTransformer() {
//                private static final float MIN_SCALE = 0.8f;
//                private static final float MIN_ALPHA = 0.5f;
//
//                @Override
//                public void transformPage(View page, float position) {
//
//                    if (position <-1){  // [-Infinity,-1)
//                        // This page is way off-screen to the left.
//                        page.setAlpha(1f);
//                        page.setScaleX(1f);
//                        page.setScaleY(1f);
//                    }
//                    else if (position <=1){ // [-1,1]
//
//                        page.setScaleX(Math.max(MIN_SCALE,1-Math.abs(position)));
//                        page.setScaleY(Math.max(MIN_SCALE,1-Math.abs(position)));
//                        page.setAlpha(Math.max(MIN_ALPHA,1-Math.abs(position)));
//
//                    }
//                    else {  // (1,+Infinity]
//                        // This page is way off-screen to the right.
//                        page.setAlpha(1f);
//                        page.setScaleX(1f);
//                        page.setScaleY(1f);
//                    }
//                }
//            });
        }
    }
}
