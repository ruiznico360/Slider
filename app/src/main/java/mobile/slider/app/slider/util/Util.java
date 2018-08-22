package mobile.slider.app.slider.util;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.display.DisplayManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.ui.Slider;

import static android.content.Context.POWER_SERVICE;

public class Util {
    public static View.OnTouchListener darkenAsPressed(final Runnable onClick) {
        return new View.OnTouchListener() {
            private boolean movable = true;
            private Rect bounds;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundColor(Color.parseColor("#50d3d3d3"));
                    movable = true;
                    bounds = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if(!bounds.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                        movable = false;
                    }else{
                        movable = true;
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (movable) {
                        onClick.run();
                    }
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                return true;
            }
        };
    }
    public static int screenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) SystemOverlay.service.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
    public static int screenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) SystemOverlay.service.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static void logM(Object... args) {
        String s = "MULTI-LOG";
        for (Object j : args) {
            s = s + " " + j.toString();
        }
        Util.log(s);
    }
    public static void log(Object s) {
        Log.d("Slider", s + "");
    }

    public static void sendNotification(Context c, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c).setSmallIcon(R.drawable.floater_dots).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentTitle(title).setContentText(text);
        int NOTIFICATION_ID = new Random().nextInt(5000);
        Intent targetIntent = new Intent(c, Slider.class);
        PendingIntent contentIntent = PendingIntent.getActivity(c, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }
    public static boolean isLocked(Context c) {
        KeyguardManager myKM = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.inKeyguardRestrictedInputMode()) {
            return true;
        } else {
            return false;
        }
    }
    public static boolean isScreenOn(Context c) {
        if (Build.VERSION.SDK_INT >= 20) {
            DisplayManager dm = (DisplayManager)  c.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    return true;
                }
            }
            return false;
        }else{
            PowerManager powerManager = (PowerManager)  c.getSystemService(POWER_SERVICE);
            if (powerManager.isScreenOn()) return true;
            return false;
        }
    }
    public static void generateViewId(View v) {
        AtomicInteger sNextGeneratedId = new AtomicInteger(1);
        int result;
        for (;;) {
            result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                break;
            }
        }
        if (Build.VERSION.SDK_INT >= 17) {
            v.setId(View.generateViewId());
        }else{
            v.setId(result);
        }
    }
}
