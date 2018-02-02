package mobile.slider.app.slider.content.SView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.services.SystemOverlay;

public class SView {
    public View view;
    public SWindowLayout container;
    public RelativeLayout.LayoutParams params;
    public int x() {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        return loc[0];
    }
    public int y() {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        return loc[1];
    }
    public int width() {
        return params.width;
    }
    public int height() {
        return params.height;
    }

    public SView(View view, SWindowLayout container) {
        this.view = view;
        this.container = container;
    }
    public void plot() {
        container.layout.addView(view);
        params = (RelativeLayout.LayoutParams) view.getLayoutParams();
    }
    public Layout openLayout() {
        params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        SView.Layout l = new Layout();
        l.toHeight = height();
        l.toWidth = width();
        return l;
    }

    public class Layout {
        private float toWidth, toHeight;
        public void setWidth(float width) {
            toWidth = width;
        }
        public void setHeight(float height) {
            toHeight = height;
        }
        public void addRule(int rule) {
            params.addRule(rule);
        }
        public void save() {
            params.width = (int)toWidth;
            params.height = (int)toHeight;
            view.setLayoutParams(params);
            container.layout.updateViewLayout(view, params);
        }
    }
}
