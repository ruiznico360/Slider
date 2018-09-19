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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.RoundedImageView;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.contact.Contact;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class MainUI {
    public boolean canMoveUISelector = true;
    public SView inner, uiSelector, yourApps, quickApps, miniWindows, logo, uiPos, uiIndicatorText;
    public ViewGroup mainLayout;
    public Context c;

    public MainUI(Context context, SView inner) {
        this.c = context;
        this.inner = inner;
    }


    public int wUnit(int percent) {
        return (int)(UserInterface.UI.width / 100f * percent);
    }
    public int hUnit(int percent) {
        return (int)(UserInterface.UI.height / 100f * percent);
    }

    public void remove() {
        mainLayout.removeAllViews();
    }

    public void setup() {
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
        public void setup() {
            final SView container = new SView(new RelativeLayout(c), quickApps.view);
            container.plot(wUnit(100), ScrollView.LayoutParams.WRAP_CONTENT);
            int[] drawables = new int[]{R.drawable.quick_apps_phone,R.drawable.quick_apps_sms,R.drawable.quick_apps_internet,R.drawable.quick_apps_calculator,R.drawable.quick_apps_contacts};
            for (int i = 0; i < drawables.length; i++) {
                final Item item = genItem(container.view);
                ImageUtil.setImageDrawable(item.appIcon.view, drawables[i]);

                if (i == 0) {
                    item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Anim anim = new Anim(c, inner, 150);
                            anim.hideAfter = true;
                            if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
                                anim.addTranslate(inner.width(),0);
                            }else{
                                anim.addTranslate(-inner.width(),0);
                            }
                            UserInterface.UI.touchEnabled = false;
                            anim.setEnd(new Runnable() {
                                @Override
                                public void run() {
                                    if (!anim.cancelled) {
                                        remove();
                                        int toWidth = UserInterface.relativeWidth() / 2;
                                        UserInterface.UI.resize(toWidth, UserInterface.relativeHeight());

                                        final Anim anim = new Anim(c, inner, 100);
                                        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
                                            anim.addTranslate(toWidth, -toWidth, 0, 0);
                                        } else {
                                            anim.addTranslate(-toWidth, toWidth, 0, 0);
                                        }
                                        anim.setStart(new Runnable() {
                                            @Override
                                            public void run() {
                                                inner.view.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        anim.setEnd(new Runnable() {
                                            @Override
                                            public void run() {
                                                UserInterface.UI.touchEnabled = true;
                                            }
                                        });
                                        UserInterface.UI.container.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                anim.start();
                                            }
                                        });
                                    }
                                }
                            });
                            anim.start();
                        }
                    });
                    SView.RLayout edit = item.container.openRLayout();
                    edit.setTopM(i * item.container.height() + wUnit(15)).save();
                }else if (i == 4) {
                    SView.RLayout edit = item.container.openRLayout();
                    edit.setTopM(i * item.container.height() + wUnit(15)).setHeight(item.container.height() + hUnit(10)).save();

                    final SView text = new SView(new TextView(c), item.container.view);
                    text.plot();
                    text.openRLayout().setHeight(hUnit(10)).setWidth(item.container.width()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM).save();
                    item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ArrayList<Contact> cList = new ArrayList<Contact>();
                            Contact.retrieveContacts(cList);
                            int sel = new Random().nextInt(cList.size());
                            ((ImageView)item.appIcon.view).setImageBitmap(cList.get(sel).getPhoto());
                            ((TextView)text.view).setText(cList.get(sel).name);
                        }
                    });

                }else if (i == 1) {
                    SView.RLayout edit = item.container.openRLayout();
                    edit.setTopM(i * item.container.height() + wUnit(15)).setHeight(item.container.height() + hUnit(10)).save();
                    item.appIcon.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0,1);
                        }
                    });
                    item.appIcon.view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ((InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(1,0);
                            return false;
                        }
                    });
                }else{
                    SView.RLayout edit = item.container.openRLayout();
                    edit.setTopM(i * item.container.height() + wUnit(15)).save();
                }
            }
        }
        private Item genItem(View parent) {
            Item item;

            SView container = new SView(new RelativeLayout(c), parent);
            container.plot();
            container.openRLayout().addRule(RelativeLayout.CENTER_HORIZONTAL).setWidth(wUnit(85)).setHeight(wUnit(100)).save();

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

