package mobile.slider.app.slider.model.SView;

import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.util.Util;

public class SView {
    public View view;
    public ViewGroup container;
    public ViewGroup.LayoutParams params;
    public Anim currentAnim;

    public SView(View view, View container) {
        this.view = view;
        this.container = (ViewGroup) container;
    }

    public int x() {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        return loc[0] - (int)view.getTranslationX();

    }
    public int y() {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        return loc[1] - (int)view.getTranslationY();
    }
    public int width() {
        if (view.getMeasuredWidth() > 0) {
            return view.getMeasuredWidth();
        }else{
            if (params.width == -1) {
                return container.getLayoutParams().width;
            }
            return params.width;
        }
    }
    public int height() {
        if (view.getMeasuredHeight() > 0) {
            return view.getMeasuredHeight();
        }else{
            if (params.height == -1) {
                return container.getLayoutParams().height;
            }
            return params.height;
        }
    }
    public void post(final Runnable r) {
        view.getViewTreeObserver().addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > 16) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                view.post(r);
            }
        });
    }
    public void plot() {
        container.addView(view);
        params = view.getLayoutParams();
    }
    public void remove() {
        view.setVisibility(View.INVISIBLE);
        container.removeView(view);
    }
    public void plot(int width, int height) {
        params = new ViewGroup.LayoutParams(width,height);
        container.addView(view, params);
    }
    public Layout openLayout() {
        params = view.getLayoutParams();
        SView.Layout l = new Layout();
        l.toHeight = height();
        l.toWidth = width();
        return l;
    }

    public RLayout openRLayout() {
        params = view.getLayoutParams();
        SView.RLayout l = new RLayout();
        l.toHeight = height();
        l.toWidth = width();
        l.leftM = ((RelativeLayout.LayoutParams) params).leftMargin;
        l.topM = ((RelativeLayout.LayoutParams) params).topMargin;
        l.rightM = ((RelativeLayout.LayoutParams) params).rightMargin;
        l.bottomM = ((RelativeLayout.LayoutParams) params).bottomMargin;

        return l;
    }
    public class Layout {
        public float toWidth, toHeight;
        public Layout setWidth(float width) {
            toWidth = width;
            return this;
        }
        public Layout setHeight(float height) {
            toHeight = height;
            return this;
        }

        public void save() {
            params.width = (int)toWidth;
            params.height = (int)toHeight;
            container.updateViewLayout(view, params);
        }

        public Layout setLayout(ViewGroup.LayoutParams newParams) {
            setWidth(newParams.width);
            setHeight(newParams.height);
            return this;
        }
    }
    public class RLayout extends Layout {
        public int leftM, topM, rightM, bottomM;

        public RLayout setWidth(float width) {
            super.setWidth(width);
            return this;
        }
        public RLayout setHeight(float height) {
            super.setHeight(height);
            return this;
        }

        public RLayout addRule(int rule) {
            ((RelativeLayout.LayoutParams) params).addRule(rule);
            return this;
        }
        public RLayout addRule(int rule, int arg) {
            ((RelativeLayout.LayoutParams) params).addRule(rule, arg);
            return this;
        }
        public RLayout removeRule(int rule) {
            if (Build.VERSION.SDK_INT >= 17) {

                ((RelativeLayout.LayoutParams) params).removeRule(rule);
            }else{
                ((RelativeLayout.LayoutParams) params).addRule(rule,0);
            }
            return this;
        }
        public void save() {
            ((RelativeLayout.LayoutParams) params).setMargins(leftM, topM, rightM, bottomM);
            super.save();
        }

        public RLayout setLayout(RelativeLayout.LayoutParams newParams) {
            setWidth(newParams.width);
            setHeight(newParams.height);
            setTopM(newParams.topMargin);
            setLeftM(newParams.leftMargin);
            setRightM(newParams.rightMargin);
            setBottomM(newParams.bottomMargin);

            for (int i : newParams.getRules()) {
                addRule(i);
            }
            return this;
        }

        public int getLeftM() {
            return leftM;
        }

        public RLayout setLeftM(int leftM) {
            this.leftM = leftM;
            return this;
        }

        public int getTopM() {
            return topM;
        }

        public RLayout setTopM(int topM) {
            this.topM = topM;
            return this;
        }

        public int getRightM() {
            return rightM;
        }

        public RLayout setRightM(int rightM) {
            this.rightM = rightM;
            return this;
        }

        public int getBottomM() {
            return bottomM;
        }

        public RLayout setBottomM(int bottomM) {
            this.bottomM = bottomM;
            return this;
        }
    }
}
