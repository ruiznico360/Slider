package mobile.slider.app.slider.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;

import static android.content.Context.WINDOW_SERVICE;

public class UI {
    public static boolean running = false;
    public static UILayout uiLayout;
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
    public static void remove(final Context c) {
        running = false;
        Animation a;
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            a = AnimationUtils.loadAnimation(c, R.anim.from_middle_to_right);
        }else {
            a = AnimationUtils.loadAnimation(c, R.anim.from_middle_to_left);
        }
        final View uiLayout = UI.uiLayout;
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ((WindowManager)c.getSystemService(WINDOW_SERVICE)).removeView(uiLayout);
                UI.uiLayout = null;
                SystemOverlay.showFloater();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        uiLayout.findViewById(R.id.ui_main_layout).startAnimation(a);
    }
}
