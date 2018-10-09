package mobile.slider.app.slider.util;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.display.DisplayManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.ViewIdGenerator;
import mobile.slider.app.slider.ui.activity.Slider;

import static android.content.Context.POWER_SERVICE;

public class Util {
    public static final int VERSION = Build.VERSION.SDK_INT;

    public static View.OnTouchListener darkenAsPressed(final Runnable onClick, final boolean hasBitmap) {
        return new View.OnTouchListener() {
            private boolean oob;
            private Rect bounds;
            private Bitmap original;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (hasBitmap) {
                        ((ImageView)v).setDrawingCacheEnabled(true);
                        original = ((ImageView)v).getDrawingCache();
                    }else{
                        original = null;
                    }

                    setDarkened(v,true);
                    oob = false;
                    bounds = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if(!bounds.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                        oob = true;
                    }else{
                        oob = false;
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!oob) {
                        onClick.run();
                    }
                    setDarkened(v,false);
                }
                return true;
            }
            public void setDarkened(View v, boolean dark) {
                if (hasBitmap) {
                    if (dark) {
                        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
                        Paint p = new Paint();
                        Canvas c = new Canvas(b);

                        p.setColor(Color.argb(100,50,50,50));
                        c.drawBitmap(original,(b.getWidth() - original.getWidth()) / 2,(b.getHeight() - original.getHeight()) / 2,p);
                        c.drawRect(0,0,b.getWidth(),b.getHeight(),p);

                        ((ImageView)v).setImageBitmap(b);
                    }else{
                        ((ImageView)v).setImageBitmap(original);
                    }
                }else{
                    if (dark) {
                        v.setBackgroundColor(Color.parseColor("#50d3d3d3"));
                    }else{
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            }
        };
    }
    public static int displayWidth() {
        if (Util.screenHeight() > Util.screenWidth()) {
            return Util.screenWidth();
        }else{
            return Util.screenHeight();

        }
    }
    public static int displayHeight() {
        if (Util.screenHeight() > Util.screenWidth()) {
            return Util.screenHeight();
        }else{
            return Util.screenWidth();

        }
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
        String s = "MULTI-LOG: " + args[0].toString();

        for (int i = 1; i < args.length; i++) {
            s = s + " NEXT ITEM " + args[i].toString();
        }
        Util.log(s + " END OF MULTI-LOG");
    }
    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = SystemOverlay.service.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = SystemOverlay.service.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public static String hex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
    public static void log(Object s) {
        Log.d("SliderLog", s + "");
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
    public static EditText customEdit(Context c) {
        return (EditText) ((LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.edittext_custom,null);
    }
    public static boolean isLocked(Context c) {
        KeyguardManager myKM = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
        if(myKM.inKeyguardRestrictedInputMode()) {
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
    public static int randomColor() {
        return Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
    }
    public static int generateViewId(View v) {
        AtomicInteger sNextGeneratedId = new AtomicInteger(1);
        int result;
        if (Build.VERSION.SDK_INT >= 17) {
            result = View.generateViewId();
        }else{
            result = ViewIdGenerator.generateViewId();
        }
        v.setId(result);
        return result;
    }
}
