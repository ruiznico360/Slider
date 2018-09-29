package mobile.slider.app.slider.model.SView;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.util.Util;

public class SWindowLayout {
    public RelativeLayout layout;
    public WindowManager.LayoutParams params;

    public SWindowLayout(RelativeLayout layout) {
        this.layout = layout;
    }

    public void post(final Runnable r) {
        layout.getViewTreeObserver().addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > 16) {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                layout.post(r);
            }
        });
    }

    public int x() {
        return params.x;
    }
    public int y() {
        return params.y;
    }
    public int width() {
        if (params.width > 0) {
            return params.width;
        }else{
            return layout.getWidth();
        }
    }
    public int height() {
        if (params.height > 0) {
            return params.height;
        }else {
            return layout.getHeight();
        }
    }


    public void plot(WindowManager.LayoutParams params) {
        plot(params.x, params.y,params.width,params.height,params.type,params.flags,params.format,params.gravity, params.screenOrientation);
    }
    public void plot(int x, int y, int width, int height, int type, int flags, int format, int gravity, int screenOrientation) {
        params = new WindowManager.LayoutParams(width, height, x, y, type, flags, format);
        params.gravity = gravity;
        params.screenOrientation = screenOrientation;
        ((WindowManager) SystemOverlay.service.getSystemService(Context.WINDOW_SERVICE)).addView(layout, params);
    }
    public void remove() {
        layout.setVisibility(View.INVISIBLE);
        ((WindowManager) SystemOverlay.service.getSystemService(Context.WINDOW_SERVICE)).removeView(layout);
    }
    public SWindowLayout.Layout openLayout() {
        SWindowLayout.Layout l = new SWindowLayout.Layout();
        l.toX = params.x;
        l.toY = params.y;
        l.toWidth = params.width;
        l.toHeight = params.height;
        return l;
    }

    public class Layout {
        public float toX, toY, toWidth, toHeight;

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
        }
    }
}