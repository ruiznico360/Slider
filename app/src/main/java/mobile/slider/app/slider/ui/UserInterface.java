package mobile.slider.app.slider.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.ContentFragment;
import mobile.slider.app.slider.content.InternetHandler;
import mobile.slider.app.slider.content.animations.ZoomAnimation;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsHandler;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.AppTheme;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.settings.resources.WindowShader;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.Util;

public class UserInterface extends FragmentActivity {
    private Navigator currentNavigator;
    private int widthPixels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_interface);

        setupActivity();
    }
    @Override
    public void onPause() {
        super.onPause();
        finish();
    }
    @Override
    public void finish() {
        super.finish();
        SystemOverlay.overlayFloater.setVisibility(View.VISIBLE);
        setAnimation();
    }
    public void setupActivity() {
        checkForServiceEnabled();

        if (getIntent().getExtras() != null) {
            if (!getIntent().getExtras().containsKey("FromSettings")) {
                setAnimation();
            }
        }else{
            setAnimation();
        }

        initializeColors();
        setUpNavigator();
        setUpContentFragment();
        setUpWindow();
        setUpResizer();
    }
    public void initializeColors() {
        findViewById(R.id.user_interface_main_layout).setBackgroundColor(SettingsUtil.getBackgroundColor());
        if (SettingsUtil.getWindowShaders().equals(WindowShader.BOTH) || SettingsUtil.getWindowShaders().equals(WindowShader.TOP)) {
            View backgroundHolder = findViewById(R.id.activty_user_interface_background_holder_top);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)backgroundHolder.getLayoutParams();
            params.height = (int)(dm.heightPixels * .3);
            backgroundHolder.setLayoutParams(params);

            if (Build.VERSION.SDK_INT >= 21) {
                backgroundHolder.setBackground(Util.backgroundGradientTop(backgroundHolder));
            }else{
                backgroundHolder.setBackgroundDrawable(Util.backgroundGradientTop(backgroundHolder));
            }
        }
        if (SettingsUtil.getWindowShaders().equals(WindowShader.BOTH) || SettingsUtil.getWindowShaders().equals(WindowShader.BOTTOM)) {
            View backgroundHolder = findViewById(R.id.activty_user_interface_background_holder_bottom);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)backgroundHolder.getLayoutParams();
            params.height = (int)(dm.heightPixels * .3);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            backgroundHolder.setLayoutParams(params);

            if (Build.VERSION.SDK_INT >= 21) {
                backgroundHolder.setBackground(Util.backgroundGradientBottom(backgroundHolder));
            }else{
                backgroundHolder.setBackgroundDrawable(Util.backgroundGradientBottom(backgroundHolder));
            }
        }
    }
    public void checkForServiceEnabled() {
        SettingsHandler.appContext = getApplicationContext();
        if (SettingsHandler.checkForPermissions()) {
            if (SystemOverlay.service == null) {
                Intent i = new Intent(this,SystemOverlay.class);
                i.putExtra("FromUI",true);
                startService(i);
                CustomToast.c = getApplicationContext();
                SettingsHandler.refreshSettings();
            }else{
                disableFloater();
            }
        }else{
            //create permissions activity
            Intent i = new Intent(this, PermissionsInterface.class);
            startActivity(i);
        }
    }
    public void disableFloater() {
        if (SystemOverlay.service != null) {
            SystemOverlay.overlayFloater.setVisibility(View.INVISIBLE);
        }else{

        }
    }
    public void setUpNavigator() {
        currentNavigator = new Navigator();
        currentNavigator.setUp();
        currentNavigator.setSelected(0);
    }
    public void setUpResizer() {
        findViewById(R.id.user_interface_resizer_layout).setSoundEffectsEnabled(false);
        findViewById(R.id.user_interface_resizer_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final ImageView resizer = (ImageView) findViewById(R.id.user_interface_resize_icon);
        resizer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    System.out.println(widthPixels + " " + event.getRawX());
                    getWindow().setLayout((int)(widthPixels - event.getRawX()),dm.heightPixels);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    System.out.println(widthPixels + " " + event.getRawX());
                    getWindow().setLayout((int)(widthPixels - event.getRawX()),dm.heightPixels);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    System.out.println(widthPixels + " " + event.getRawX());
                    getWindow().setLayout((int)(widthPixels - event.getRawX()),dm.heightPixels);
                    return true;
                }
                return false;
            }
        });
    }

    public void setUpContentFragment() {
        final List<ContentFragment> frags = getLayoutResources();
        final ViewPager pager = (ViewPager) findViewById(R.id.content_layout_in_activity);
        pager.setPageTransformer(true, new ZoomAnimation());
        PagerAdapter adapter = new ContentFragment.ContentFragmentAdapter(getSupportFragmentManager(),frags);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    getNavigator().setSelected(1);
                    new InternetHandler(UserInterface.this, frags.get(1));
                } else if (position == 3) {
                    pager.setCurrentItem(2, false);
                    getNavigator().setSelected(3);
                    startActivity(new Intent(UserInterface.this, SettingsActivity.class));
                } else {
                    getNavigator().setSelected(position);
                }
            }
        });
    }
    public void setUpWindow() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * ((double) SettingsUtil.getWindowSize() / 100)), height);

        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            getWindow().setGravity(Gravity.RIGHT | Gravity.TOP);
        }else if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
            getWindow().setGravity(Gravity.LEFT | Gravity.TOP);
        }
        widthPixels = width;
    }
    public List<ContentFragment> getLayoutResources(){
        List<ContentFragment> fList = new ArrayList();
        fList.add(ContentFragment.newInstance(R.layout.content_layout));
        WebView web = new WebView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        web.setLayoutParams(params);
        fList.add(ContentFragment.newInstance(web));
        fList.add(ContentFragment.newInstance(R.layout.content_layout));
        fList.add(ContentFragment.newInstance(R.layout.content_layout));
        return fList;
    }
    public Navigator getNavigator() {
        return currentNavigator;
    }
    public void setAnimation() {
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            overridePendingTransition(R.anim.from_right_to_middle, R.anim.from_middle_to_right);
        }else if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
            overridePendingTransition(R.anim.from_left_to_middle, R.anim.from_middle_to_left);
        }
    }

    public void setCurrentContent(int page) {
        ViewPager pager = (ViewPager) findViewById(R.id.content_layout_in_activity);
        pager.setCurrentItem(page);
    }
    private class Navigator implements View.OnClickListener {
        private int r,g,b;
        private ImageView imageLeft,imageCenter,imageRight,imageSettings, navLeft,navCenter,navRight,navSettings;
        private ImageView[] topNavigators;
        private ImageView[] imageNavigators;

        public void setUp() {
            r = Color.red(SettingsUtil.getBackgroundColor());
            g = Color.green(SettingsUtil.getBackgroundColor());
            b = Color.blue(SettingsUtil.getBackgroundColor());

            imageLeft = (ImageView) findViewById(R.id.navigator_left);
            imageCenter = (ImageView) findViewById(R.id.navigator_center);
            imageRight = (ImageView) findViewById(R.id.navigator_right);
            imageSettings = (ImageView) findViewById(R.id.navigator_settings);
            navLeft = (ImageView) findViewById(R.id.navigatorDrawableTop_left);
            navCenter = (ImageView) findViewById(R.id.navigatorDrawableTop_center);
            navRight = (ImageView) findViewById(R.id.navigatorDrawableTop_right);
            navSettings = (ImageView) findViewById(R.id.navigatorDrawableTop_settings);

            imageLeft.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            imageCenter.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            imageRight.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            imageSettings.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            navLeft.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            navCenter.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            navRight.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            navSettings.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

            if (!(SettingsUtil.getWindowShaders().equals(WindowShader.BOTTOM)) && !(SettingsUtil.getWindowShaders().equals(WindowShader.BOTH))) {
                if (SettingsUtil.getAppTheme().equals(AppTheme.DARK)) {
                    imageLeft.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    imageCenter.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    imageRight.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    imageSettings.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    navLeft.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    navCenter.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    navRight.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    navSettings.getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                } else {
                    imageLeft.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    imageCenter.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    imageRight.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    imageSettings.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    navLeft.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    navCenter.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    navRight.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    navSettings.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                }
            }

            imageLeft.setOnClickListener(this);
            imageCenter.setOnClickListener(this);
            imageRight.setOnClickListener(this);
            imageSettings.setOnClickListener(this);

            topNavigators = new ImageView[]{navLeft, navCenter, navRight, navSettings};
            imageNavigators = new ImageView[]{imageLeft, imageCenter, imageRight, imageSettings};
        }
        public void setSelected(int selected) {
            r = Color.red(SettingsUtil.getBackgroundColor());
            g = Color.green(SettingsUtil.getBackgroundColor());
            b = Color.blue(SettingsUtil.getBackgroundColor());
            int bg = Color.BLUE;
            if (!(SettingsUtil.getWindowShaders().equals(WindowShader.BOTTOM)) && !(SettingsUtil.getWindowShaders().equals(WindowShader.BOTH))) {
                bg = Color.rgb((int)(r * .8),(int)(g * .8),(int)(b * .8));
            }
            for (int i = 0;i < topNavigators.length; i++) {
                if (selected == i) {
                    topNavigators[i].setVisibility(View.VISIBLE);
                    imageNavigators[i].setBackgroundColor(bg);
                }else{
                    topNavigators[i].setVisibility(View.INVISIBLE);
                    imageNavigators[i].setBackgroundColor(Color.TRANSPARENT);
                }
            }

        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.navigator_left:
                    setSelected(0);
                    setCurrentContent(0);
                    break;
                case R.id.navigator_center:
                    setSelected(1);
                    setCurrentContent(1);
                    break;
                case R.id.navigator_right:
                    setSelected(2);
                    setCurrentContent(2);
                    break;
                case R.id.navigator_settings:
                    setSelected(3);
                    startActivity(new Intent(UserInterface.this,SettingsActivity.class));
            }
        }
    }
}
