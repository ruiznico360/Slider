package mobile.slider.app.slider.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class UIView {
    public static boolean shouldMove() {
        if (UserInterface.UI != null && UserInterface.UI.touchEnabled) {
            return true;
        }else{
            return false;
        }
    }

    public static class UIContainer extends RelativeLayout {
        public UIContainer(Context c){
            super(c);
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) || (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH) || (event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
                UserInterface.UI.backPressed();
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }

    public static class MHScrollView extends HorizontalScrollView {
        public MHScrollView(Context c) {
            super(c);
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (shouldMove()) {
                return super.onTouchEvent(event);
            }else{
                return false;
            }
        }
    }

    public static class MScrollView extends ScrollView {
        public MScrollView(Context c) {
            super(c);

            this.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                int scrollY = 0;
                @Override
                public void onScrollChanged() {
                    if (shouldMove()) {
                        scrollY = MScrollView.this.getScrollY();
                    }else{
                        smoothScrollTo(0,scrollY);
                    }
                }
            });
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (shouldMove()) {
                return super.onTouchEvent(event);
            }else{
                return false;
            }
        }
    }
}
