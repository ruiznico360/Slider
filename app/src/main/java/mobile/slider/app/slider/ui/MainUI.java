package mobile.slider.app.slider.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.SView.SView;
import mobile.slider.app.slider.content.adapters.MainUIAdapter;
import mobile.slider.app.slider.content.adapters.MainUIListAdapter;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class MainUI {
    public float WUNIT,HUNIT;
    public SView inner;
    public Context c;
    public ViewPager uiSelector;
    public ListView yourApps, quickApps, miniWindows, dummyYourApps, dummyMiniWindows;
    public ImageView logo, uiPos, uiIndicatorText;
    public RelativeLayout mainLayout;
    public MotionEvent updateMiniWindows, updateYourapps;

    public MainUI(float widthUnit, float heightUnit, Context context, SView inner) {
        this.WUNIT = widthUnit;
        this.HUNIT = heightUnit;
        this.c = context;
        this.inner = inner;
    }

    public void setup() {
        uiSelector = new ViewPager(c);
        quickApps = new ListView(c);
        yourApps = new ListView(c);
        miniWindows = new ListView(c);
        dummyYourApps = new ListView(c);
        dummyMiniWindows = new ListView(c);

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
        params.topMargin = (int) (HUNIT * 3);
        params.width = (int) (WUNIT * 100);
        params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.quick_apps_title), params.width);
        mainLayout.updateViewLayout(uiIndicatorText, params);

        uiPos.setImageDrawable(ImageUtil.getDrawable(R.drawable.main_ui_indicator_center));
        Util.generateViewId(uiPos);
        params = (RelativeLayout.LayoutParams) uiPos.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, uiIndicatorText.getId());
        params.width = (int) (WUNIT * 100);
        params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.main_ui_indicator_center), params.width);
        mainLayout.updateViewLayout(uiPos, params);

        logo.setImageDrawable(ImageUtil.getDrawable(R.drawable.app_logo));
        Util.generateViewId(logo);
        params = (RelativeLayout.LayoutParams) logo.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.width = (int) (WUNIT * 100);
        params.height = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.app_logo), params.width);
        mainLayout.updateViewLayout(logo, params);

        setupUiSelector();

    }
    private void setupUiSelector() {
        setupListViews();

        ArrayList<View> pages = new ArrayList<>();
        pages.add(dummyMiniWindows);
        pages.add(yourApps);
        pages.add(quickApps);
        pages.add(miniWindows);
        pages.add(dummyYourApps);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) uiSelector.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, uiPos.getId());
        params.addRule(RelativeLayout.ABOVE, logo.getId());
        params.width = (int) (WUNIT * 100);
        mainLayout.updateViewLayout(uiSelector, params);

        uiSelector.setBackgroundColor(Color.MAGENTA);
        uiSelector.setOverScrollMode(View.OVER_SCROLL_NEVER);
        uiSelector.setAdapter(new MainUIAdapter(c, pages));
        uiSelector.setCurrentItem(2, false);
        uiSelector.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
            }

            @Override
            public void onPageScrollStateChanged (int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int curr = uiSelector.getCurrentItem();
                    int lastReal = uiSelector.getAdapter().getCount() - 2;
                    if (curr == 0) {
                        uiSelector.setCurrentItem(lastReal, false);
                    } else if (curr > lastReal) {
                        uiSelector.setCurrentItem(1, false);
                    }
                    int position = uiSelector.getCurrentItem();
                    if (position == 1) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_left);
                        ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.your_apps_title);

                        if (updateMiniWindows != null) {
                            Util.log("miniwindows");
                            dummyMiniWindows.dispatchTouchEvent(updateMiniWindows);
                            updateMiniWindows = null;
                        }
                    }else if (position == 2) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_center);
                        ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.quick_apps_title);
                    }else if (position == 3) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_right);
                        ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.mini_windows_title);

                        if (updateYourapps != null) {
                            Util.log("yourapps");
                            dummyYourApps.dispatchTouchEvent(updateYourapps);
                            updateYourapps = null;
                        }
                    }
                }
            }
        });
//            uiSelector.setPageTransformer(false, new ViewPager.PageTransformer() {
//                private static final float MIN_SCALE = 0.8f;
//                private static final float MIN_ALPHA = 0.5f;
//
//                @Override
//                public void transformPage(View page, float position) {
//
//                    if (position <-1){  // [-Infinity,-1)
//                        // This page is way off-screen to the left.
//                        page.setAlpha(1f);
//                        page.setScaleX(1f);
//                        page.setScaleY(1f);
//                    }
//                    else if (position <=1){ // [-1,1]
//
//                        page.setScaleX(Math.max(MIN_SCALE,1-Math.abs(position)));
//                        page.setScaleY(Math.max(MIN_SCALE,1-Math.abs(position)));
//                        page.setAlpha(Math.max(MIN_ALPHA,1-Math.abs(position)));
//
//                    }
//                    else {  // (1,+Infinity]
//                        // This page is way off-screen to the right.
//                        page.setAlpha(1f);
//                        page.setScaleX(1f);
//                        page.setScaleY(1f);
//                    }
//                }
//            });
    }
    private void setupListViews() {
        dummyMiniWindows.setBackgroundColor(Color.GREEN);
        yourApps.setBackgroundColor(Color.CYAN);
        quickApps.setBackgroundColor(Color.RED);
        miniWindows.setBackgroundColor(Color.GREEN);
        dummyYourApps.setBackgroundColor(Color.CYAN);

        ArrayList<View> data = new ArrayList<>();
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));
        data.add(new ImageView(c));

        quickApps.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
        quickApps.setDivider(null);
        quickApps.setDividerHeight((int)HUNIT * 3);

        yourApps.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
        yourApps.setDivider(null);
        yourApps.setDividerHeight((int)HUNIT * 3);

        dummyMiniWindows.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
        dummyMiniWindows.setDivider(null);
        dummyMiniWindows.setDividerHeight((int)HUNIT * 3);

        dummyYourApps.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
        dummyYourApps.setDivider(null);
        dummyYourApps.setDividerHeight((int)HUNIT * 3);

        miniWindows.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
        miniWindows.setDivider(null);
        miniWindows.setDividerHeight((int)HUNIT * 3);


        //try using scrollview
        miniWindows.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                // onScroll will be called and there will be an infinite loop.
//                // That's why i set a boolean value
//                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
//                    isRightListEnabled = false;
//                } else if (scrollState == SCROLL_STATE_IDLE) {
//                    isRightListEnabled = true;
//                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }
}
