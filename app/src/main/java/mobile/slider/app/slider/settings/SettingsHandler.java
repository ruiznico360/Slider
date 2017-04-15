package mobile.slider.app.slider.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import mobile.slider.app.slider.settings.resources.AppTheme;
import mobile.slider.app.slider.settings.resources.FloaterIcon;
import mobile.slider.app.slider.settings.resources.Language;
import mobile.slider.app.slider.settings.resources.SettingType;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.settings.resources.WindowShader;
import mobile.slider.app.slider.util.CustomToast;

public class SettingsHandler {
    public static Context appContext;
    private static SharedPreferences sharedPreferences;

    public static void init(Context c) {
        appContext = c;
        sharedPreferences = appContext.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        CustomToast.makeToast("init");
    }
    public static boolean checkForPermissions() {
        return sharedPreferences.getBoolean(SettingType.PERMISSIONS, false);
    }
    public static void refreshSettings() {
        if (!sharedPreferences.contains(SettingType.LANGUAGE)) {
            resetDefaultSettings();
        }
        SettingsUtil.language = sharedPreferences.getString(SettingType.LANGUAGE, SettingType.NULL);
        SettingsUtil.backgroundColor = sharedPreferences.getInt(SettingType.BACKGROUND_COLOR, 0);
        SettingsUtil.autoSelectAppTheme = sharedPreferences.getBoolean(SettingType.APP_THEME_AUTO_SELECT, true);
        SettingsUtil.appTheme = sharedPreferences.getString(SettingType.APP_THEME, SettingType.NULL);
        SettingsUtil.windowSize = sharedPreferences.getInt(SettingType.WINDOW_SIZE, 0);
        SettingsUtil.windowGravity = sharedPreferences.getString(SettingType.WINDOW_GRAVITY, SettingType.NULL);
        SettingsUtil.windowShaders = sharedPreferences.getString(SettingType.WINDOW_SHADERS, SettingType.NULL);
        SettingsUtil.floaterSize = sharedPreferences.getInt(SettingType.FLOATER_SIZE, 0);
        SettingsUtil.floaterGravity = sharedPreferences.getString(SettingType.FLOATER_GRAVITY, SettingType.NULL);
        SettingsUtil.floaterPos = sharedPreferences.getInt(SettingType.FLOATER_POS, 0);
        SettingsUtil.floaterIcon = sharedPreferences.getString(SettingType.FLOATER_ICON,SettingType.NULL);
    }
    public static void setSetting(String setting,int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(setting, value);
        editor.commit();
        refreshSettings();
    }
    public static void setSetting(String setting,float value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(setting, value);
        editor.commit();
        refreshSettings();
    }
    public static void setSetting(String setting,String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(setting, value);
        editor.commit();
        refreshSettings();
    }
    public static void setSetting(String setting,boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(setting, value);
        String s = null;
        editor.commit();
        refreshSettings();
    }
    public static void resetDefaultSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingType.PERMISSIONS, true);
        editor.putString(SettingType.LANGUAGE, Language.ENGLISH);
        editor.putInt(SettingType.BACKGROUND_COLOR, SettingsUtil.defaultBackgroundColor());
        editor.putString(SettingType.APP_THEME, AppTheme.LIGHT);
        editor.putBoolean(SettingType.APP_THEME_AUTO_SELECT, true);
        editor.putInt(SettingType.WINDOW_SIZE, 70);
        editor.putString(SettingType.WINDOW_GRAVITY, WindowGravity.RIGHT);
        editor.putString(SettingType.WINDOW_SHADERS, WindowShader.BOTH);
        editor.putInt(SettingType.FLOATER_SIZE, 400);
        editor.putInt(SettingType.FLOATER_GRAVITY, -1000);
        editor.putInt(SettingType.FLOATER_POS, -1000);
        editor.putString(SettingType.FLOATER_GRAVITY, WindowGravity.RIGHT);
        editor.putString(SettingType.FLOATER_ICON, FloaterIcon.TRANSLUCENT);
        editor.commit();
    }
}
