package mobile.slider.app.slider.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.SView.SView;
import mobile.slider.app.slider.content.adapters.MainUIAdapter;
import mobile.slider.app.slider.content.adapters.MainUIListAdapter;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class MainUI {
    public float WUNIT,HUNIT;
    public int updateYourApps, updateMiniWindows;
    public SView inner;
    public Context c;
    public ViewPager uiSelector;
    public ScrollView yourApps, quickApps, miniWindows, dummyYourApps, dummyMiniWindows;
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
        uiSelector = new ViewPager(c);
        quickApps = new ScrollView(c);
        yourApps = new ScrollView(c);
        miniWindows = new ScrollView(c);
        dummyYourApps = new ScrollView(c);
        dummyMiniWindows = new ScrollView(c);

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

//        setupUiSelector();
        final HorizontalScrollView sc = new HorizontalScrollView(c);
        sc.setOverScrollMode(View.OVER_SCROLL_NEVER);
        sc.setHorizontalScrollBarEnabled(false);
        mainLayout.addView(sc);
        params = (RelativeLayout.LayoutParams) sc.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, uiPos.getId());
        params.addRule(RelativeLayout.ABOVE, logo.getId());
        params.width = wUnit(100);
        mainLayout.updateViewLayout(sc, params);

        final RelativeLayout l = new RelativeLayout(c);
        sc.addView(l);
        final HorizontalScrollView.LayoutParams sp = (HorizontalScrollView.LayoutParams) l.getLayoutParams();
        sp.width = wUnit(300);
        sp.height = hUnit(200);
        sc.updateViewLayout(l, sp);

        final ImageView l1 = new ImageView(c);
        final ImageView l2 = new ImageView(c);
        final ImageView l3 = new ImageView(c);

        l1.setBackgroundColor(Color.CYAN);
        l2.setBackgroundColor(Color.RED);
        l3.setBackgroundColor(Color.GREEN);

        l.addView(l1, new RelativeLayout.LayoutParams(sp.width / 3, RelativeLayout.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(sp.width / 3, RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.leftMargin = sp.width / 3;
        l.addView(l2, lp);

        lp = new RelativeLayout.LayoutParams(sp.width / 3, RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.leftMargin = (sp.width / 3) * 2;
        l.addView(l3, lp);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sc.scrollTo(sc.getMeasuredWidth(),0);
            }
        },1);
        final LHold lhold = new LHold();
        lhold.needsToUpdate = false;
        lhold.curr = l2;
        sc.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (lhold.needsToUpdate) {
                    Util.log("lescroll " + sc.getScrollX());
                    if (sc.getScrollX() == 0 || sc.getScrollX() == 360 || sc.getScrollX() == 720) {
                        RelativeLayout.LayoutParams l1p = (RelativeLayout.LayoutParams) l1.getLayoutParams();
                        RelativeLayout.LayoutParams l2p = (RelativeLayout.LayoutParams) l2.getLayoutParams();
                        RelativeLayout.LayoutParams l3p = (RelativeLayout.LayoutParams) l3.getLayoutParams();

//                        if (lhold.curr == l1) {
//                            Util.log(lhold.scrollTo + " l1");
//                            if (lhold.scrollTo.equals("left")) {
//                                l1p.leftMargin = 2 * sp.width / 3;
//                                l2p.leftMargin = 0;
//                                l3p.leftMargin = sp.width / 3;
//                                lhold.curr = l3;
//                            } else {
//                                l1p.leftMargin = 0;
//                                l2p.leftMargin = sp.width / 3;
//                                l3p.leftMargin = 2 * sp.width / 3;
//                                lhold.curr = l2;
//                            }
//                        } else if (lhold.curr == l2) {
//                            Util.log(lhold.scrollTo + " l2");
//                            if (lhold.scrollTo.equals("left")) {
//                                l1p.leftMargin = sp.width / 3;
//                                l2p.leftMargin = 2 * sp.width / 3;
//                                l3p.leftMargin = 0;
//                                lhold.curr = l1;
//                            } else {
//                                l1p.leftMargin = 2 * sp.width / 3;
//                                l2p.leftMargin = 0;
//                                l3p.leftMargin = sp.width / 3;
//                                lhold.curr = l3;
//                            }
//                        } else {
//                            Util.log(lhold.scrollTo + " l3");
//                            if (lhold.scrollTo.equals("left")) {
//                                l1p.leftMargin = 0;
//                                l2p.leftMargin = sp.width / 3;
//                                l3p.leftMargin = 2 * sp.width / 3;
//                                lhold.curr = l2;
//                            } else {
//                                l1p.leftMargin = sp.width / 3;
//                                l2p.leftMargin = 2 * sp.width / 3;
//                                l3p.leftMargin = 0;
//                                lhold.curr = l1;
//                            }
//                        }
//
//                        l.updateViewLayout(l1, l1p);
//                        l.updateViewLayout(l2, l2p);
//                        l.updateViewLayout(l3, l3p);
                        lhold.needsToUpdate = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Util.log("scrolled");
                                sc.scrollTo(sc.getMeasuredWidth(), 0);
                            }
                        },1);
                    }
                }
            }
        });
        //make infinite by moving pages around
        sc.setOnTouchListener(new View.OnTouchListener() {
            public int initialX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int range = sc.getChildAt(0).getMeasuredWidth() - sc.getMeasuredWidth();
                int pageSize = l2.getMeasuredWidth();
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    Util.log(sc.getScrollX());
                }else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialX = sc.getScrollX();
                }else if (event.getAction() == MotionEvent.ACTION_UP) {
                    int sX = sc.getScrollX();
                    if (sX - initialX > pageSize / 4) {
                        sc.scrollTo(initialX + sc.getMeasuredWidth(),0);
                        lhold.needsToUpdate = true;
                        lhold.scrollTo = "right";
                    }else if (sX - initialX < -pageSize / 4) {
                        sc.scrollTo(initialX - sc.getMeasuredWidth(),0);
                        lhold.needsToUpdate = true;
                        lhold.scrollTo = "left";
                    }else {
                        sc.scrollTo(initialX,0);
                    }
                    return true;
                }
                return false;
            }
        });
    }
    public class LHold {
        public String scrollTo;
        public boolean needsToUpdate;
        public View curr;
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
        params.width = wUnit(100);
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
                    }else if (position == 2) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_center);
                        ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.quick_apps_title);
                    }else if (position == 3) {
                        ImageUtil.setImageDrawable(uiPos, R.drawable.main_ui_indicator_right);
                        ImageUtil.setImageDrawable(uiIndicatorText, R.drawable.mini_windows_title);
                    }
                }else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    int position = uiSelector.getCurrentItem();
                    if (position == 1) {
                       Util.log("miniwindows");
                        dummyMiniWindows.scrollTo(0,updateMiniWindows);
                    }else if (position == 3) {
                        Util.log("your apps");
                        dummyYourApps.scrollTo(0,updateYourApps);
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

        RelativeLayout l = new RelativeLayout(c);
        ImageUtil.setBackground(l, R.drawable.garbage);
        quickApps.addView(l);
        ScrollView.LayoutParams params = (ScrollView.LayoutParams) l.getLayoutParams();
        params.width = wUnit(100);
        params.height = hUnit(200);
        quickApps.updateViewLayout(l, params);

        l = new RelativeLayout(c);
        ImageUtil.setBackground(l, R.drawable.garbage);
        miniWindows.addView(l);
        params = (ScrollView.LayoutParams) l.getLayoutParams();
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

        l = new RelativeLayout(c);
        ImageUtil.setBackground(l, R.drawable.garbage);
        dummyYourApps.addView(l);
        params = (ScrollView.LayoutParams) l.getLayoutParams();
        params.width = wUnit(100);
        params.height = hUnit(200);
        dummyYourApps.updateViewLayout(l, params);

        l = new RelativeLayout(c);
        ImageUtil.setBackground(l, R.drawable.garbage);
        dummyMiniWindows.addView(l);
        params = (ScrollView.LayoutParams) l.getLayoutParams();
        params.width = wUnit(100);
        params.height = hUnit(200);
        dummyMiniWindows.updateViewLayout(l, params);

        miniWindows.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                updateMiniWindows = miniWindows.getScrollY();
                return false;
            }
        });

        yourApps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                updateYourApps = yourApps.getScrollY();
                return false;
            }
        });
//        ArrayList<View> data = new ArrayList<>();
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//        data.add(new ImageView(c));
//
//        quickApps.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
//        quickApps.setDivider(null);
//        quickApps.setDividerHeight((int)HUNIT * 3);
//
//        yourApps.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
//        yourApps.setDivider(null);
//        yourApps.setDividerHeight((int)HUNIT * 3);
//
//        dummyMiniWindows.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
//        dummyMiniWindows.setDivider(null);
//        dummyMiniWindows.setDividerHeight((int)HUNIT * 3);
//
//        dummyYourApps.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
//        dummyYourApps.setDivider(null);
//        dummyYourApps.setDividerHeight((int)HUNIT * 3);
//
//        miniWindows.setAdapter(new MainUIListAdapter(data,c,(int) (WUNIT*100)));
//        miniWindows.setDivider(null);
//        miniWindows.setDividerHeight((int)HUNIT * 3);
//
//
//        //try using scrollview
//        miniWindows.setOnScrollListener(new AbsListView.OnScrollListener() {
//
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
////                // onScroll will be called and there will be an infinite loop.
////                // That's why i set a boolean value
////                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
////                    isRightListEnabled = false;
////                } else if (scrollState == SCROLL_STATE_IDLE) {
////                    isRightListEnabled = true;
////                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            }
//        });
    }
}
