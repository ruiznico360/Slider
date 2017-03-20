package mobile.slider.app.slider.settings;

import android.graphics.Color;

/**
 * Created by Nicolas on 2016-04-19.
 */
public class SettingsUtil {
    protected static String floaterGravity;
    protected static String windowShaders;
    protected static String windowGravity;
    protected static String language;
    protected static String appTheme;
    protected static String floaterIcon;

    protected static int backgroundColor,windowSize, floaterSize,floaterPos;
    protected static boolean autoSelectAppTheme;

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

}
