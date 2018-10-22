package mobile.slider.app.slider.ui.activity;

import android.app.Activity;

public class SettingsActivity extends Activity {

//    public SettingPopup currentPopup;
//    public SettingInflater inflater;
//    public RelativeLayout settingView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
////        setContentView(R.layout.activity_settings);
//
//        setupActivity();
//    }
//
//    public void setupActivity() {
//        SystemOverlay.floater.hideFloater();
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//        settingView = (RelativeLayout) findViewById(R.id.settings_scroll_view);
//
//        initializeColors();
//        initializeListView();
//        initializeBackButton();
//    }
//    @Override
//    public void onBackPressed() {
//        if (currentPopup != null && currentPopup.isVisible()) {
//            currentPopup.remove();
//        }else {
//            backPressed();
//        }
//    }
//    @Override
//    public void onPause() {
//        super.onPause();
//        finish();
//    }
//    @Override
//    public void finish() {
//        super.finish();
//        if (currentPopup != null && currentPopup.isVisible()) {
//            currentPopup.remove();
//        }
////        SystemOverlay.service.createFloater(View.VISIBLE);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//    }
//
//    public void backPressed() {
//        finish();
//        Intent i = new Intent(this, Slider.class);
//        i.putExtra(IntentExtra.FROM_SETTINGS, true);
//        startActivity(i);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//    }
//    public void refreshActivity() {
//        initializeColors();
//        initializeListView();
//    }
//    public void initializeColors() {
//        getWindow().getDecorView().setBackgroundColor(SettingsUtil.getBackgroundColor());
//        findViewById(R.id.settings_top_line).setBackgroundColor(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()));
//        findViewById(R.id.settings_bottom_line).setBackgroundColor(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()));
//        ((TextView)findViewById(R.id.settingsHeader)).setTextColor(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()));
//
//        Drawable d;
//        if (Build.VERSION.SDK_INT >= 21) {
//            d = getDrawable(R.drawable.back_arrow);
//        }else{
//            d = getResources().getDrawable(R.drawable.back_arrow);
//        }
//        d.setColorFilter(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()), PorterDuff.Mode.MULTIPLY);
//        ((ImageView)findViewById(R.id.settings_back_arrow)).setImageDrawable(d);
//    }
//    public void initializeListView() {
//        if (inflater == null) {
//            inflater = new SettingInflater(this);
//        }
//        inflater.clear();
//        inflater.inflateSettings();
//    }
//    public void initializeBackButton() {
//        final ImageView back = (ImageView)findViewById(R.id.settings_back_arrow);
//        back.setOnTouchListener(Util.darkenAsPressed(new Runnable() {
//            @Override
//            public void run() {
//                backPressed();
//            }
//        }));
//    }
//    public HashMap<String,Setting> getSettings() {
//        HashMap<String,Setting> settings = new HashMap<>();
//        settings.put(SettingType.LANGUAGE, new Setting("App Language", "Change the language of the application", SettingsUtil.getLanguage().toLowerCase()));
//        settings.put(SettingType.BACKGROUND_COLOR, new Setting("Background color", "Change the color of the background surrounding the application", null));
//        settings.put(SettingType.APP_THEME, new Setting("App theme", "Set the color scheme for the images and text of the application", SettingsUtil.getAppTheme().toLowerCase()));
//        settings.put(SettingType.APP_THEME_AUTO_SELECT, new Setting("Auto Select theme", "Select app theme automatically based on the background color", SettingsUtil.getAutoSelectAppTheme() + ""));
//        settings.put(SettingType.WINDOW_SIZE, new Setting("Window Size", "Change what percent of the screen is covered by the application", SettingsUtil.getWindowSize() + "%"));
//        settings.put(SettingType.WINDOW_GRAVITY, new Setting("Window Gravity", "Change which side of your screen the window will be on", SettingsUtil.getWindowGravity().toLowerCase()));
//        settings.put(SettingType.WINDOW_SHADERS, new Setting("Window Shaders", "Select how the shaders will be displayed (recommended to leave top shader as lighter backgrounds may hide the status bar)", SettingsUtil.getWindowShaders().toLowerCase()));
//        settings.put(SettingType.FLOATER_SIZE,new Setting("Floater Size", "Change the windowSize of the floater on your screen", SettingsUtil.getFloaterSize() + " px"));
//        settings.put(SettingType.FLOATER_GRAVITY,new Setting("Floater Gravity", "Change which side of your screen the floater will be on", SettingsUtil.getFloaterGravity().toLowerCase()));
//        settings.put(SettingType.FLOATER_ICON,new Setting("Floater minimizedIcon", "Set the minimizedIcon for the floater which is shown on your screen",SettingsUtil.getFloaterIcon()));
//        settings.put(SettingType.RESET_SETTINGS,new Setting("Reset Settings", "Reset the default settings for the application on this device", null));
//
//
//        return settings;
//    }
}
