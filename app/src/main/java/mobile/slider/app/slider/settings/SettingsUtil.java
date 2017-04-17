package mobile.slider.app.slider.settings;

import android.graphics.Color;

import mobile.slider.app.slider.settings.resources.SettingType;

public class SettingsUtil {
    protected static String floaterGravity;
    protected static String windowShaders;
    protected static String windowGravity;
    protected static String language;
    protected static String appTheme;
    protected static String floaterIcon;
    protected static int backgroundColor,windowSize, floaterSize,floaterPos;
    protected static boolean autoSelectAppTheme,perms;

    public static int oppositeBackgroundShade(int color) {
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return Color.BLACK;
        }else{
            return Color.WHITE;
        }
    }
    public static int oppositeBackgroundColor(int color) {
        return Color.rgb(255 - Color.red(color),255 - Color.green(color),255 - Color.blue(color));
    }
    public static int defaultBackgroundColor() {
        return Color.rgb(51,102,255);
    }
    public static String getLanguage() {
        return language;
    }
    public static int getBackgroundColor() {
        return backgroundColor;
    }
    public static String getAppTheme() {
        return appTheme;
    }
    public static boolean getAutoSelectAppTheme() {
        return autoSelectAppTheme;
    }
    public static int getWindowSize() {
        return windowSize;
    }

    public static String getWindowGravity() {
        return windowGravity;
    }

    public static String getWindowShaders() {
        return windowShaders;
    }
    public static int getFloaterSize() {
        return floaterSize;
    }
    public static String getFloaterGravity() {
        return floaterGravity;
    }
    public static int getFloaterPos() {
        return floaterPos;
    }
    public static String getFloaterIcon() {
        return floaterIcon;
    }

    public static boolean getPerms() {
        return perms;
    }

    public static void setPerms(boolean perms) {
        SettingsUtil.perms = perms;
        SettingsWriter.setSetting(SettingType.PERMISSIONS, perms);
    }
    public static void setFloaterGravity(String floaterGravity) {
        SettingsUtil.floaterGravity = floaterGravity;
        SettingsWriter.setSetting(SettingType.FLOATER_GRAVITY , floaterGravity);
    }

    public static void setWindowShaders(String windowShaders) {
        SettingsUtil.windowShaders = windowShaders;
        SettingsWriter.setSetting(SettingType.WINDOW_SHADERS , windowShaders);

    }

    public static void setWindowGravity(String windowGravity) {
        SettingsUtil.windowGravity = windowGravity;
        SettingsWriter.setSetting(SettingType.WINDOW_GRAVITY , windowGravity);

    }

    public static void setLanguage(String language) {
        SettingsUtil.language = language;
        SettingsWriter.setSetting(SettingType.LANGUAGE , language);

    }

    public static void setAppTheme(String appTheme) {
        SettingsUtil.appTheme = appTheme;
        SettingsWriter.setSetting(SettingType.APP_THEME , appTheme);

    }

    public static void setFloaterIcon(String floaterIcon) {
        SettingsUtil.floaterIcon = floaterIcon;
        SettingsWriter.setSetting(SettingType.FLOATER_ICON , floaterIcon);

    }

    public static void setBackgroundColor(int backgroundColor) {
        SettingsUtil.backgroundColor = backgroundColor;
        SettingsWriter.setSetting(SettingType.BACKGROUND_COLOR , backgroundColor);

    }

    public static void setWindowSize(int windowSize) {
        SettingsUtil.windowSize = windowSize;
        SettingsWriter.setSetting(SettingType.WINDOW_SIZE , windowSize);

    }

    public static void setFloaterSize(int floaterSize) {
        SettingsUtil.floaterSize = floaterSize;
        SettingsWriter.setSetting(SettingType.FLOATER_SIZE , floaterSize);

    }

    public static void setFloaterPos(int floaterPos) {
        SettingsUtil.floaterPos = floaterPos;
        SettingsWriter.setSetting(SettingType.FLOATER_POS , floaterPos);

    }

    public static void setAutoSelectAppTheme(boolean autoSelectAppTheme) {
        SettingsUtil.autoSelectAppTheme = autoSelectAppTheme;
        SettingsWriter.setSetting(SettingType.APP_THEME_AUTO_SELECT , autoSelectAppTheme);

    }
}
