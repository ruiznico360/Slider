package mobile.slider.app.slider.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.Random;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.RoundedImageView;
import mobile.slider.app.slider.model.UIView;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class MainUI extends UIClass{
    public boolean canMoveUISelector = true;
    public SView inner, uiSelector, yourApps, quickApps, miniWindows, logo, uiPos, uiIndicatorText;
    public ViewGroup mainLayout;
    public Context c;

    public String getID() {
        return UserInterface.UI_WINDOW;
    }

    public MainUI(Context context) {
        this.c = context;
    }

    public void remove() {

    }

    public void backPressed() {
        UserInterface.UI.remove();
    }
    public void setup() {
        UserInterface.UI.resize(Util.displayWidth() / 4);
        this.inner = UserInterface.UI.inner;
        mainLayout = (ViewGroup)inner.view;

        logo = new SView(new ImageView(c), mainLayout);
        uiPos = new SView(new ImageView(c), mainLayout);
        uiIndicatorText = new SView(new ImageView(c), mainLayout);

        uiIndicatorText.plot();
        uiPos.plot();
        logo.plot();

        ImageUtil.setImageDrawable(uiIndicatorText.view, R.drawable.quick_apps_title);
        Util.generateViewId(uiIndicatorText.view);

        SView.RLayout edit = uiIndicatorText.openRLayout();
        edit.setTopM(hUnit(UserInterface.TITLE_TOP_MARGIN));
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

                        Anim anim = UserInterface.uiAnim(c, uiSelectorLayout, 150);
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
        public int C_HEIGHT = 100;

        public void setup() {
            final SView container = new SView(new RelativeLayout(c), quickApps.view);
            container.plot(wUnit(100), ScrollView.LayoutParams.WRAP_CONTENT);

            addPhone(0,container);
            addSMS(1,container);
            addCalculator(2,container);
            addInternet(3,container);
            addContacts(4,container);
            addSettings(5,container);

        }
        public void addPhone(int pos, SView container) {
            final Item item = genItem(container.view);
            item.container.openRLayout().setTopM(((int)(pos * wUnit(C_HEIGHT))) + wUnit(15)).save();
            ImageUtil.setImageDrawable(item.appIcon.view, R.drawable.quick_apps_phone);
        }
        public void addSMS(int pos, SView container) {
            final Item item = genItem(container.view);
            item.container.openRLayout().setTopM(((int) (pos * wUnit(C_HEIGHT))) + wUnit(15)).save();
            ImageUtil.setImageDrawable(item.appIcon.view, R.drawable.quick_apps_sms);

            item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SettingsUtil.setBackgroundColor(Color.rgb(new Random().nextInt(255),new Random().nextInt(255),new Random().nextInt(255)));
                }
            });
        }
        public void addInternet(int pos, SView container) {
            final Item item = genItem(container.view);
            item.container.openRLayout().setTopM(((int) (pos * wUnit(C_HEIGHT))) + wUnit(15)).save();
            ImageUtil.setImageDrawable(item.appIcon.view, R.drawable.quick_apps_internet);

            item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserInterface.UI.launchNewWindow(UserInterface.WEB_WINDOW);
                }
            });
        }
        public void addCalculator(int pos, SView container) {
            final Item item = genItem(container.view);
            item.container.openRLayout().setTopM(((int) (pos * wUnit(C_HEIGHT))) + wUnit(15)).save();
            ImageUtil.setImageDrawable(item.appIcon.view, R.drawable.quick_apps_calculator);

            item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserInterface.UI.launchNewWindow(UserInterface.CALCULATOR_WINDOW);
                }
            });
        }
        public void addContacts(int pos, SView container) {
            final Item item = genItem(container.view);
            item.container.openRLayout().setTopM(((int) (pos * wUnit(C_HEIGHT))) + wUnit(15)).save();
            ImageUtil.setImageDrawable(item.appIcon.view, R.drawable.quick_apps_contacts);

            item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserInterface.UI.launchNewWindow(UserInterface.CONTACTS_WINDOW);
                }
            });
        }
        public void addSettings(int pos, SView container) {
            final Item item = genItem(container.view);
            item.container.openRLayout().setTopM(((int) (pos * wUnit(C_HEIGHT))) + wUnit(15)).save();
            ImageUtil.setImageDrawable(item.appIcon.view, R.drawable.quick_apps_settings);
        }
        private Item genItem(View parent) {
            Item item;

            SView container = new SView(new RelativeLayout(c), parent);
            container.plot();
            container.openRLayout().addRule(RelativeLayout.CENTER_HORIZONTAL).setWidth(wUnit(85)).setHeight(wUnit(C_HEIGHT)).save();

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

