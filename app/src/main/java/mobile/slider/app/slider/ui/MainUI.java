package mobile.slider.app.slider.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.RoundedImageView;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.util.Anim;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class MainUI {
    public float WUNIT,HUNIT;
    public boolean canMoveUISelector = true;
    public SView inner;
    public Context c;
    public HorizontalScrollView uiSelector;
    public ScrollView yourApps, quickApps, miniWindows;
    public ImageView logo, uiPos, uiIndicatorText;
    public RelativeLayout mainLayout;

    public MainUI(float widthUnit, float heightUnit, Context context, SView inner) {
        this.WUNIT = widthUnit;
        this.HUNIT = heightUnit;
        this.c = context;
        this.inner = inner;
    }


    public int wUnit(int percent) {
        return (int)(WUNIT * percent);
    }
    public int hUnit(int percent) {
        return (int)(HUNIT * percent);
    }
    public void setup() {
        uiSelector = new UIView.MHScrollView(c);
        quickApps = new UIView.MScrollView(c);
        yourApps = new UIView.MScrollView(c);
        miniWindows = new UIView.MScrollView(c);

        logo = new ImageView(c);
        uiPos = new ImageView(c);
        uiIndicatorText = new ImageView(c);
        mainLayout = inner.view.findViewById(R.id.ui_main_layout);


        mainLayout.addView(uiSelector);
        mainLayout.addView(uiPos);
        mainLayout.addView(uiIndicatorText);
        mainLayout.addView(logo);

        uiIndicatorText.setImageDrawable(ImageUtil.getDrawable(R.drawable.quick_apps_title));
        Util.generateViewId(uiIndicatorText);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) uiIndicatorText.getLayoutParams();
        params.topMargin = hUnit(3);
        params.width = wUnit(100);
        params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.quick_apps_title), params.width);
        mainLayout.updateViewLayout(uiIndicatorText, params);

        uiPos.setImageDrawable(ImageUtil.getDrawable(R.drawable.main_ui_indicator_center));
        Util.generateViewId(uiPos);
        params = (RelativeLayout.LayoutParams) uiPos.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, uiIndicatorText.getId());
        params.width = wUnit(100);
        params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.main_ui_indicator_center), params.width);
        mainLayout.updateViewLayout(uiPos, params);

        logo.setImageDrawable(ImageUtil.getDrawable(R.drawable.app_logo));
        Util.generateViewId(logo);
        params = (RelativeLayout.LayoutParams) logo.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.width = wUnit(100);
        params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.app_logo), params.width);
        mainLayout.updateViewLayout(logo, params);

        setupUiSelector();
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupUiSelector() {
        setupListViews();
        final View[] uiSelectorViews = new View[]{yourApps,quickApps,miniWindows};
        final int sWidth = wUnit(100);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) uiSelector.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, uiPos.getId());
        params.addRule(RelativeLayout.ABOVE, logo.getId());
        params.width = sWidth;
        mainLayout.updateViewLayout(uiSelector, params);

        uiSelector.setOverScrollMode(View.OVER_SCROLL_NEVER);
        uiSelector.setHorizontalScrollBarEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uiSelector.scrollTo(sWidth,0);
            }
        },1);

        final RelativeLayout uiSelectorLayout = new RelativeLayout(c);
        uiSelector.addView(uiSelectorLayout, sWidth * 3, HorizontalScrollView.LayoutParams.MATCH_PARENT);

        final RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(sWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
        final RelativeLayout.LayoutParams middleParams = new RelativeLayout.LayoutParams(sWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
        final RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(sWidth, RelativeLayout.LayoutParams.MATCH_PARENT);

        middleParams.leftMargin = sWidth;
        rightParams.leftMargin = sWidth * 2;

        uiSelectorLayout.addView(yourApps, leftParams);
        uiSelectorLayout.addView(quickApps, middleParams);
        uiSelectorLayout.addView(miniWindows, rightParams);

        uiSelector.setOnTouchListener(new View.OnTouchListener() {
            public int initialX = -1;
            boolean updateReq = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (canMoveUISelector) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (initialX == -1) {
                            initialX = uiSelector.getScrollX();
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (initialX == -1) {
                            return true;
                        }

                        final int sX = uiSelector.getScrollX();
                        final int scrollTo;
                        int leftSize,rightRize;
                        int uiLoc[] = new int[2];
                        uiSelectorLayout.getLocationOnScreen(uiLoc);
                        final int uiSelecLoc[] = new int[2];
                        uiSelector.getLocationOnScreen(uiSelecLoc);

                        if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
                            leftSize = 4;
                            rightRize = 8;
                        }else{
                            leftSize = 8;
                            rightRize = 4;
                        }
                        if (sX - initialX > sWidth / rightRize) {
                            scrollTo = uiSelecLoc[0] - sWidth * 2 - uiLoc[0];
                            updateReq = true;
                            View temp = uiSelectorViews[0];
                            for (int i = 0; i < 2; i++){
                                uiSelectorViews[i] = uiSelectorViews[i + 1];
                            }
                            uiSelectorViews[2] = temp;

                        } else if (sX - initialX < -sWidth / leftSize) {
                            scrollTo = uiSelecLoc[0] - uiLoc[0];
                            updateReq = true;
                            View temp = uiSelectorViews[2];
                            for (int i = 2; i > 0; i--){
                                uiSelectorViews[i] = uiSelectorViews[i - 1];
                            }
                            uiSelectorViews[0] = temp;
                        }else{
                            scrollTo = uiSelecLoc[0] - sWidth - uiLoc[0];
                        }

                        Anim anim = UserInterface.uiAnim(c, uiSelectorLayout, 150);
                        anim.addTranslate(scrollTo,0);
                        anim.setEnd(new Runnable() {
                            @Override
                            public void run() {
                                uiSelectorLayout.updateViewLayout(uiSelectorViews[0], leftParams);
                                uiSelectorLayout.updateViewLayout(uiSelectorViews[1], middleParams);
                                uiSelectorLayout.updateViewLayout(uiSelectorViews[2], rightParams);
                                uiSelector.scrollTo(sWidth,0);
                                canMoveUISelector = true;
                                updateReq = false;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (uiSelectorViews[1] == yourApps) {
                                            ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.your_apps_title);
                                            ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_left);
                                        }else if (uiSelectorViews[1] == quickApps) {
                                            ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.quick_apps_title);
                                            ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_center);
                                        }else if (uiSelectorViews[1] == miniWindows) {
                                            ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.mini_windows_title);
                                            ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_right);
                                        }
                                    }
                                },1);
                            }
                        });
                        anim.start();
                        canMoveUISelector = false;
                        initialX = -1;
                        return true;
                    }
                }else{
                    return true;
                }
                return false;
            }
        });
    }
    private void setupListViews() {
        new QuickApps().setup();
        yourApps.setBackgroundColor(Color.CYAN);
        miniWindows.setBackgroundColor(Color.GREEN);

        RelativeLayout l = new RelativeLayout(c);
        ImageUtil.setBackground(l, R.drawable.garbage);
        miniWindows.addView(l);
        ScrollView.LayoutParams params = (ScrollView.LayoutParams) l.getLayoutParams();
        params.width = wUnit(100);
        params.height = hUnit(200);
        miniWindows.updateViewLayout(l, params);

        l = new RelativeLayout(c);
        ImageUtil.setBackground(l, R.drawable.garbage);
        yourApps.addView(l);
        params = (ScrollView.LayoutParams) l.getLayoutParams();
        params.width = wUnit(100);
        params.height = hUnit(200);
        yourApps.updateViewLayout(l, params);
    }
    public class QuickApps {
        public void setup() {
            RelativeLayout container = new RelativeLayout(c);
            quickApps.addView(container,new ScrollView.LayoutParams(wUnit(100), ScrollView.LayoutParams.WRAP_CONTENT));
            int[] drawables = new int[]{R.drawable.quick_apps_phone,R.drawable.quick_apps_sms,R.drawable.quick_apps_internet,R.drawable.quick_apps_calculator,R.drawable.quick_apps_contacts};
            for (int i = 0; i < drawables.length; i++) {
                Item item = genItem(container);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item.container.getLayoutParams();
                params.topMargin = i * params.height + wUnit(15);
                ImageUtil.setImageDrawable(item.appIcon, drawables[i]);
            }
        }
        private Item genItem(RelativeLayout parent) {
            Item item;
            RelativeLayout container = new RelativeLayout(c);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(wUnit(85)), wUnit(100));
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            parent.addView(container, params);

            ImageView appIcon = new RoundedImageView(c);
            RelativeLayout.LayoutParams iParams = new RelativeLayout.LayoutParams(params.width,params.width);
            container.addView(appIcon, iParams);
            item = new Item(container, appIcon);
            return item;
        }
        public class Item {
            public RelativeLayout container;
            public ImageView appIcon;
            public Item(RelativeLayout container, ImageView appIcon) {
                this.container = container;
                this.appIcon = appIcon;
            }
        }
    }
}
