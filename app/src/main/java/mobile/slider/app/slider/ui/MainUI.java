package mobile.slider.app.slider.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.media.Image;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
    public SView inner, uiSelector, yourApps, quickApps, miniWindows, logo, uiPos, uiIndicatorText;
    public ViewGroup mainLayout;
    public Context c;

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
        mainLayout = inner.view.findViewById(R.id.ui_main_layout);

        logo = new SView(new ImageView(c), mainLayout);
        uiPos = new SView(new ImageView(c), mainLayout);
        uiIndicatorText = new SView(new ImageView(c), mainLayout);

        uiIndicatorText.plot();
        uiPos.plot();
        logo.plot();

        ImageUtil.setImageDrawable(uiIndicatorText.view, R.drawable.quick_apps_title);
        Util.generateViewId(uiIndicatorText.view);

        SView.RLayout edit = uiIndicatorText.openRLayout();
        edit.setTopM(hUnit(3));
        edit.setWidth(wUnit(100));
        edit.setHeight(ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.quick_apps_title), (int) edit.toWidth));
        edit.save();

        ImageUtil.setImageDrawable(uiPos.view, R.drawable.main_ui_indicator_center);
        Util.generateViewId(uiPos.view);
        edit = uiPos.openRLayout();
        edit.addRule(RelativeLayout.BELOW, uiIndicatorText.view.getId());
        edit.setWidth(wUnit(100));
        edit.setHeight(ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.main_ui_indicator_center), (int) edit.toWidth));
        edit.save();

        ImageUtil.setImageDrawable(logo.view, R.drawable.app_logo);
        Util.generateViewId(logo.view);
        edit = logo.openRLayout();
        edit.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        edit.setWidth(wUnit(100));
        edit.setHeight(ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.app_logo), (int) edit.toWidth));
        edit.save();

        setupUiSelector();
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupUiSelector() {
        final int sWidth = wUnit(100);

        uiSelector = new SView(new UIView.MHScrollView(c), mainLayout);
        uiSelector.view.setOverScrollMode(View.OVER_SCROLL_NEVER);
        uiSelector.view.setHorizontalScrollBarEnabled(false);
        uiSelector.plot();
        SView.RLayout edit = uiSelector.openRLayout();
        edit.setWidth(sWidth);
        edit.setHeight(300);
        edit.addRule(RelativeLayout.BELOW, uiPos.view.getId());
        edit.addRule(RelativeLayout.ABOVE, logo.view.getId());
        edit.save();

        final SView uiSelectorLayout = new SView(new RelativeLayout(c), (ViewGroup) uiSelector.view);
        uiSelectorLayout.plot(sWidth * 3, HorizontalScrollView.LayoutParams.MATCH_PARENT);

        final RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(sWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
        final RelativeLayout.LayoutParams middleParams = new RelativeLayout.LayoutParams(sWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
        final RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(sWidth, RelativeLayout.LayoutParams.MATCH_PARENT);

        middleParams.leftMargin = sWidth;
        rightParams.leftMargin = sWidth * 2;

        yourApps = new SView(new UIView.MScrollView(c), (ViewGroup) uiSelectorLayout.view);
        quickApps = new SView(new UIView.MScrollView(c), (ViewGroup) uiSelectorLayout.view);
        miniWindows = new SView(new UIView.MScrollView(c), (ViewGroup) uiSelectorLayout.view);

        yourApps.plot();
        edit = yourApps.openRLayout();
        edit.setLayout(leftParams);
        edit.save();

        quickApps.plot();
        edit = quickApps.openRLayout();
        edit.setLayout(middleParams);
        edit.save();

        miniWindows.plot();
        edit = miniWindows.openRLayout();
        edit.setLayout(rightParams);
        edit.save();

        final SView[] uiSelectorViews = new SView[]{yourApps,quickApps,miniWindows};
        setupListViews();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uiSelector.view.scrollTo(sWidth,0);
            }
        },1);

        uiSelector.view.setOnTouchListener(new View.OnTouchListener() {
            public int initialX = -1;
            boolean updateReq = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (canMoveUISelector) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (initialX == -1) {
                            initialX = uiSelector.view.getScrollX();
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (initialX == -1) {
                            return true;
                        }

                        final int sX = uiSelector.view.getScrollX();
                        final int scrollTo;
                        int leftSize,rightRize;
                        int uiLoc[] = new int[2];
                        uiSelectorLayout.view.getLocationOnScreen(uiLoc);
                        final int uiSelecLoc[] = new int[2];
                        uiSelector.view.getLocationOnScreen(uiSelecLoc);

                        if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
                            leftSize = 4;
                            rightRize = 8;
                        }else{
                            leftSize = 8;
                            rightRize = 4;
                        }
                        if (sX - initialX > sWidth / rightRize) {
                            scrollTo = uiSelector.x() - sWidth * 2 - uiSelectorLayout.x();
                            updateReq = true;
                            SView temp = uiSelectorViews[0];
                            for (int i = 0; i < 2; i++){
                                uiSelectorViews[i] = uiSelectorViews[i + 1];
                            }
                            uiSelectorViews[2] = temp;

                        } else if (sX - initialX < -sWidth / leftSize) {
                            scrollTo = uiSelector.x() - uiSelectorLayout.x();
                            updateReq = true;
                            SView temp = uiSelectorViews[2];
                            for (int i = 2; i > 0; i--){
                                uiSelectorViews[i] = uiSelectorViews[i - 1];
                            }
                            uiSelectorViews[0] = temp;
                        }else{
                            scrollTo = uiSelector.x() - sWidth - uiSelectorLayout.x();
                        }

                        Anim anim = UserInterface.uiAnim(c, uiSelectorLayout.view, 150);
                        anim.addTranslate(scrollTo,0);
                        anim.setEnd(new Runnable() {
                            @Override
                            public void run() {
                                uiSelectorViews[0].openRLayout().setLayout(leftParams).save();
                                uiSelectorViews[1].openRLayout().setLayout(middleParams).save();
                                uiSelectorViews[2].openRLayout().setLayout(rightParams).save();

                                uiSelector.view.scrollTo(sWidth,0);
                                canMoveUISelector = true;
                                updateReq = false;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (uiSelectorViews[1] == yourApps) {
                                            ImageUtil.setImageDrawable(uiIndicatorText.view, R.drawable.your_apps_title);
                                            ImageUtil.setImageDrawable(uiPos.view, R.drawable.main_ui_indicator_left);
                                        }else if (uiSelectorViews[1] == quickApps) {
                                            ImageUtil.setImageDrawable(uiIndicatorText.view, R.drawable.quick_apps_title);
                                            ImageUtil.setImageDrawable(uiPos.view, R.drawable.main_ui_indicator_center);
                                        }else if (uiSelectorViews[1] == miniWindows) {
                                            ImageUtil.setImageDrawable(uiIndicatorText.view, R.drawable.mini_windows_title);
                                            ImageUtil.setImageDrawable(uiPos.view, R.drawable.main_ui_indicator_right);
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
        yourApps.view.setBackgroundColor(Color.CYAN);
        miniWindows.view.setBackgroundColor(Color.GREEN);

        SView l = new SView(new RelativeLayout(c), (ViewGroup) miniWindows.view);
        ImageUtil.setBackground(l.view, R.drawable.garbage);
        l.plot();
        SView.Layout edit = l.openLayout();
        edit.setWidth(wUnit(100));
        edit.setHeight(hUnit(200));
        edit.save();

        l = new SView(new RelativeLayout(c), (ViewGroup) yourApps.view);
        ImageUtil.setBackground(l.view, R.drawable.garbage);
        l.plot();
        edit = l.openLayout();
        edit.setWidth(wUnit(100));
        edit.setHeight(hUnit(200));
        edit.save();
    }
    public class QuickApps {
        public void setup() {
            SView container = new SView(new RelativeLayout(c), quickApps.view);
            container.plot(wUnit(100), ScrollView.LayoutParams.WRAP_CONTENT);
            int[] drawables = new int[]{R.drawable.quick_apps_phone,R.drawable.quick_apps_sms,R.drawable.quick_apps_internet,R.drawable.quick_apps_calculator,R.drawable.quick_apps_contacts};
            for (int i = 0; i < drawables.length; i++) {
                final Item item = genItem(container.view);
                SView.RLayout edit = item.container.openRLayout();


                if (i == 4) {
                    Util.generateViewId(item.appIcon.view);
                    final SView et = new SView(new EditText(c), item.container.view);
                    et.plot();
                    SView.RLayout re = et.openRLayout();
                    re.addRule(RelativeLayout.BELOW, item.appIcon.view.getId());
                    re.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                    re.setHeight(wUnit(50));
                    re.save();

                    et.view.setBackgroundColor(Color.RED);
                    item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            et.view.requestFocus();
                        }
                    });
                    ImageUtil.setImageDrawable(item.appIcon.view, drawables[i]);
                    edit.setTopM(i * item.container.height() + wUnit(15)).setHeight(item.container.height() + wUnit(50)).save();
                }else if (i == 3) {
                    item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((InputMethodManager)c.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
                        }
                    });
                    ImageUtil.setImageDrawable(item.appIcon.view, drawables[i]);
                    edit.setTopM(i * item.container.height() + wUnit(15)).save();
                }else{
                    ImageUtil.setImageDrawable(item.appIcon.view, drawables[i]);
                    edit.setTopM(i * item.container.height() + wUnit(15)).save();
                }
            }
        }
        private Item genItem(View parent) {
            Item item;

            SView container = new SView(new RelativeLayout(c), parent);
            container.plot();
            container.openRLayout().addRule(RelativeLayout.CENTER_HORIZONTAL).setWidth(wUnit(75)).setHeight(wUnit(100)).save();

            SView appIcon = new SView(new RoundedImageView(c), container.view);
            appIcon.plot(container.width(), container.width());

            item = new Item(container, appIcon);
            return item;
        }
        public class Item {
            public SView container, appIcon;

            public Item(SView container, SView appIcon) {
                this.container = container;
                this.appIcon = appIcon;
            }
        }
    }
}

