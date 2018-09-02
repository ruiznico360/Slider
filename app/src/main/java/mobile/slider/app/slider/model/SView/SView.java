package mobile.slider.app.slider.model.SView;

import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;

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
        if (view.getMeasuredWidth() > 0) {
            return view.getMeasuredWidth();
        }else{
            if (params.width == -1) {
                return container.width();
            }
            return params.width;
        }
    }
    public int height() {
        if (view.getMeasuredHeight() > 0) {
            return view.getMeasuredHeight();
        }else{
            if (params.height == -1) {
                return container.height();
            }
            return params.height;
        }
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
        public void removeRule(int rule) {
            if (Build.VERSION.SDK_INT >= 17) {
                params.removeRule(rule);
            }else{
                params.addRule(rule,0);
            }
        }
        public void save() {
            params.width = (int)toWidth;
            params.height = (int)toHeight;
            view.setLayoutParams(params);
            container.layout.updateViewLayout(view, params);
        }
    }
}
