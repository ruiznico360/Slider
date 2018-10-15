package mobile.slider.app.slider.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.Collection;

import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class UIView {

    public static class UIContainer extends RelativeLayout {
        public UIContainer(Context c){
            super(c);
        }

        @Override
        public boolean dispatchKeyEventPreIme(KeyEvent event) {
            if (((event.getKeyCode() == KeyEvent.KEYCODE_BACK) || (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH) || (event.getKeyCode() == KeyEvent.KEYCODE_HOME)) && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (UserInterface.running()) UserInterface.UI.backPressed();
                return true;
            }
            return super.dispatchKeyEventPreIme(event);
        }
    }

    public static class MHScrollView extends HorizontalScrollView {
        public MHScrollView(Context c) {
            super(c);
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (UserInterface.shouldMove()) {
                return super.onTouchEvent(event);
            }else{
                return false;
            }
        }
    }

    public static class MScrollView extends ScrollView {
        private Runnable scrollEvent;
        public int prevScrollY = 0;
        public MScrollView(Context c) {
            super(c);

            scrollEvent = new Runnable() {
                @Override
                public void run() {

                }
            };

            this.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (UserInterface.shouldMove()) {
                        scrollEvent.run();
                        prevScrollY = MScrollView.this.getScrollY();
                    }else{
                        smoothScrollTo(0,prevScrollY);
                    }
                }
            });
        }
        public void setScrollEvent(Runnable r) {
            scrollEvent = r;
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (UserInterface.shouldMove()) {
                return super.onTouchEvent(event);
            }else{
                return false;
            }
        }
    }
}
