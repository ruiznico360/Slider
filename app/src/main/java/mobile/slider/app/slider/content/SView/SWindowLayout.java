package mobile.slider.app.slider.content.SView;

import android.content.Context;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.model.window.Window;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.ui.UI;
import mobile.slider.app.slider.util.Util;

public class SWindowLayout {
    public RelativeLayout layout;
    public WindowManager.LayoutParams params;
    public int x, y, width, height;

    public SWindowLayout(RelativeLayout layout) {
        this.layout = layout;
    }

    public void plot(WindowManager.LayoutParams params) {
        plot(params.x, params.y,params.width,params.height,params.type,params.flags,params.format,params.gravity);
    }
    public void plot(int x, int y, int width, int height, int type, int flags, int format, int gravity) {
        params = new WindowManager.LayoutParams(width, height, x, y, type, flags, format);
        params.gravity = gravity;
        ((WindowManager) SystemOverlay.service.getSystemService(Context.WINDOW_SERVICE)).addView(layout, params);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public void remove() {
        ((WindowManager) SystemOverlay.service.getSystemService(Context.WINDOW_SERVICE)).removeView(layout);
    }
    public SWindowLayout.Layout openLayout() {
        SWindowLayout.Layout l = new SWindowLayout.Layout();
        l.toX = x;
        l.toY = y;
        l.toWidth = width;
        l.toHeight = height;
        return l;
    }

    public class Layout {
        private float toX, toY, toWidth, toHeight;

        public void setGravity(int gravity) {
            params.gravity = gravity;
        }

        public void setOrientation(int orientation) {
            params.screenOrientation = orientation;
        }

        public void setX(float toX) {
            this.toX = toX;
        }

        public void setY(float toY) {
            this.toY = toY;
        }

        public void setWidth(float width) {
            toWidth = width;
        }

        public void setHeight(float height) {
            toHeight = height;
        }

        public void save() {
            params = (WindowManager.LayoutParams) layout.getLayoutParams();
            params.x = (int) toX;
            params.y = (int) toY;
            params.width = (int) toWidth;
            params.height = (int) toHeight;
            ((WindowManager) SystemOverlay.service.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(layout, params);

            x = (int) toX;
            y = (int) toY;
            width = (int) toWidth;
            height = (int) toHeight;
        }
    }
}