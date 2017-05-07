package mobile.slider.app.slider.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.util.Util;

import static android.content.Context.WINDOW_SERVICE;

public class UILayout {
    public static View init(Context c) {
        View ui = ((LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ui, null);
        ui.findViewById(R.id.ui_main_layout).setBackgroundColor(SettingsUtil.getBackgroundColor());
        return ui;
    }
    public static class LockedActivityView extends RelativeLayout {
        Context c;
        public LockedActivityView(Context c){
            super(c);
            this.c = c;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) || (event.getKeyCode() == KeyEvent.KEYCODE_HOME) || (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH)) {
                Util.log("one of em");
                UserInterface.remove(c);
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
