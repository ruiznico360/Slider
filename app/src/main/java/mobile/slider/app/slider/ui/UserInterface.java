package mobile.slider.app.slider.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.SView.SWindowLayout;
import mobile.slider.app.slider.model.contact.Contact;
import mobile.slider.app.slider.model.floater.Floater;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.ui.Contacts.ContactsUI;
import mobile.slider.app.slider.util.Util;

public class UserInterface {
    public static final String CONTACTS_WINDOW = "CONTACTS_WINDOW";
    public static final String UI_WINDOW = "UI_WINDOW";

    public static final int  TITLE_TOP_MARGIN = 3;

    public static UserInterface UI;

    public Context c;
    public Runnable deviceStateRunnable;
    public SWindowLayout container;
    public SView inner;
    public MainUI mainUI;
    public String currentView;
    public boolean touchEnabled = true, running = false;

    public UserInterface(Context c) {
        this.c = c;
    }

    public static void launchUI() {
        if (UserInterface.running()) {
            return;
        }
        if (SystemOverlay.floater.floaterMovement.currentlyInTouch) {
            SystemOverlay.floater.floaterMovement.forceUp();
        }
        SystemOverlay.floater.hideFloater();

        UI = new UserInterface(SystemOverlay.service);
        UI.setup();

    }

    public static boolean shouldMove() {
        if (UserInterface.running() && UI.touchEnabled) {
            return true;
        }else{
            return false;
        }
    }

    public static int relativeWidth() {
        if (Util.screenHeight() > Util.screenWidth()) {
            return Util.screenWidth();
        }else{
            return Util.screenHeight();

        }
    }
    public static int relativeHeight() {
        if (Util.screenHeight() > Util.screenWidth()) {
            return Util.screenHeight();
        }else{
            return Util.screenWidth();

        }
    }
    public void resize(int width, int height) {
        SWindowLayout.Layout edit = container.openLayout();
        edit.setWidth(width);
        edit.setHeight(height);
        edit.save();
    }
    public void setup() {
        running = true;
        int size = relativeWidth() / 4;

        final WindowManager.LayoutParams params = SystemOverlay.newWindow(true);
        params.width = size;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
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
        container = new SWindowLayout(new UIView.UIContainer(c));
        UserInterface.UI.container.layout.setVisibility(View.INVISIBLE);
        inner = new SView(new RelativeLayout(c), container.layout);

        UserInterface.UI.container.plot(params);
        inner.plot();

        inner.view.setBackgroundColor(SettingsUtil.getBackgroundColor());

        SView.Layout editor = inner.openLayout();
        editor.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        editor.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        editor.save();


        mainUI = new MainUI(c);
        mainUI.setup();

        genDeviceStateRunnable();

        UserInterface.UI.container.layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (UserInterface.shouldMove()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        int[] loc = new int[2];
                        UserInterface.UI.container.layout.getLocationOnScreen(loc);
                        int rx = (int) event.getRawX();
                        int x = loc[0];
                        int w = UserInterface.UI.container.width();
                        if (rx < x || rx > x + w ) {
                            UserInterface.UI.remove();
                        }
                    }
                    return false;
                }
                return false;
            }
        });

        Anim anim = new Anim(SystemOverlay.service, inner, 75);
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            anim.addTranslate(inner.width(), -inner.width(),0,0);
        }else {
            anim.addTranslate(-inner.width(), inner.width(),0,0);
        }
        anim.setStart(new Runnable() {
            @Override
            public void run() {
                container.layout.setVisibility(View.VISIBLE);
            }
        });
        anim.setEnd(new Runnable() {
            @Override
            public void run() {
                touchEnabled = true;
            }
        });
        touchEnabled = false;
        currentView = UI_WINDOW;
        anim.start();
    }

    public void backPressed() {
        if (shouldMove()) {
            if (UserInterface.UI.currentView.equals(UserInterface.UI_WINDOW)) {
                remove();
            }else if (UserInterface.UI.currentView.equals(UserInterface.CONTACTS_WINDOW)) {
                UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
            }
        }
    }

    public void genDeviceStateRunnable() {
        if (Util.VERSION < 26) {
            final boolean phoneStatus = Util.isLocked(SystemOverlay.service.getApplicationContext());
            deviceStateRunnable = new Runnable() {
                @Override
                public void run() {
                    if (running()) {
                        if (phoneStatus != Util.isLocked(SystemOverlay.service.getApplicationContext())) {
                            remove();
                            SystemOverlay.periodicRunnableHandler.tasks.remove(this);
                        } else if (!Util.isScreenOn(SystemOverlay.service.getApplicationContext())) {
                            remove();
                            SystemOverlay.periodicRunnableHandler.tasks.remove(this);
                        }
                    }
                }
            };
            SystemOverlay.periodicRunnableHandler.tasks.add(deviceStateRunnable);
        }else {
            deviceStateRunnable = new Runnable() {
                @Override
                public void run() {
                    if (running()) {
                        if (Util.isLocked(SystemOverlay.service.getApplicationContext())) {
                            remove();
                            SystemOverlay.periodicRunnableHandler.tasks.remove(this);
                        }else if (!Util.isScreenOn(SystemOverlay.service.getApplicationContext())) {
                            remove();
                            SystemOverlay.periodicRunnableHandler.tasks.remove(this);
                        }
                    }
                }
            };
            SystemOverlay.periodicRunnableHandler.tasks.add(deviceStateRunnable);
        }
    }
    public void remove() {
        if (SystemOverlay.periodicRunnableHandler.tasks.contains(deviceStateRunnable)) {
            SystemOverlay.periodicRunnableHandler.tasks.remove(deviceStateRunnable);
        }

        if (inner.currentAnim != null) {
            inner.currentAnim.cancel();
        }
        deviceStateRunnable = null;


        Anim anim = new Anim(SystemOverlay.service, inner, 150);
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            anim.addTranslate(inner.width(),0);
        }else {
            anim.addTranslate(-inner.width(),0);
        }
        touchEnabled = false;
        anim.setEnd(new Runnable() {
            @Override
            public void run() {
                UI.container.remove();
                UI = null;
                running = false;
                SystemOverlay.floater.showFloater(Floater.SHOW_DELAY);
            }
        });
        anim.start();

        SystemOverlay.periodicRunnableHandler.sysTask.run();
    }

    public void launchNewWindow(String windowID) {
        final Runnable end;

        if (windowID.equals(CONTACTS_WINDOW)) {
            end = new Runnable() {
                @Override
                public void run() {
                    new ContactsUI(c).setup();
                }
            };
        }else if (windowID.equals(UI_WINDOW)) {
            end = new Runnable() {
                @Override
                public void run() {
                    new MainUI(c).setup();
                }
            };
        }
        else{
            end = new Runnable() {
                @Override
                public void run() {

                }
            };
        }
        UserInterface.UI.currentView = windowID;

        final Anim anim = new Anim(c, inner, 150);
        anim.hideAfter = true;
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            anim.addTranslate(inner.width(),0);
        }else{
            anim.addTranslate(-inner.width(),0);
        }
        UserInterface.UI.touchEnabled = false;
        anim.setEnd(new Runnable() {
            @Override
            public void run() {
                if (!anim.cancelled) {
                    ((ViewGroup)inner.view).removeAllViews();
                    end.run();

                    final Anim anim = new Anim(c, inner, 100);
                    int toWidth = UserInterface.UI.container.width();

                    if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
                        anim.addTranslate(toWidth, -toWidth, 0, 0);
                    } else {
                        anim.addTranslate(-toWidth, toWidth, 0, 0);
                    }
                    anim.setStart(new Runnable() {
                        @Override
                        public void run() {
                            inner.view.setVisibility(View.VISIBLE);
                        }
                    });
                    anim.setEnd(new Runnable() {
                        @Override
                        public void run() {
                            UserInterface.UI.touchEnabled = true;
                        }
                    });
                    UserInterface.UI.container.post(new Runnable() {
                        @Override
                        public void run() {
                            anim.start();
                        }
                    });
                }
            }
        });
        anim.start();
    }
    public static boolean running() {
        if (UI != null && UI.running) {
            return true;
        }
        return false;
    }
    public static Anim uiAnim(Context c, SView view, int duration) {
        final Anim a = new Anim(c,view,duration);
        a.setCondition(new Runnable() {
            @Override
            public void run() {
                if (!shouldMove()) {
                    a.cancel();
                }
            }
        });
        return a;
    }


    //    public static class GS extends GestureDetector.SimpleOnGestureListener {
//        public ScrollView scroller;
//        public boolean scrolling = false;
//
//        public GS(ScrollView scroller) {
//            this.scroller = scroller;
//        }
//
//        @Override
//        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            return super.onScroll(e1, e2, distanceX, distanceY);
//        }
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
//            new Handler().postDelayed(new Runnable() {
//                float velocity = velocityY;
//                @Override
//                public void run() {
//                    if (shouldMove() && !scrolling) {
//                        if (velocityY < 0) {
//                            velocity = velocity + (velocityY * -.05f);
//                            scroller.scrollTo(0, scroller.getScrollY() - ((int) (velocity / 100f)));
//                            if (velocity <= 0) {
//                                new Handler().postDelayed(this, 42);
//                            }
//                        }else{
//                            velocity = velocity - (velocityY * -.05f);
//                            scroller.scrollTo(0, scroller.getScrollY() + ((int) (velocity / 100f)));
//                            if (velocity <= 0) {
//                                new Handler().postDelayed(this, 42);
//                            }
//                        }
//
//                    }
//
//                }
//            }, 42);
//            Util.log("On Fling " + velocityY);
//            return true;
//        }
//
//    }
}
