package mobile.slider.app.slider.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.ui.UserInterface;

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
    public static void log(String s) {
        Log.d("Slider", s);
    }
    public static ShapeDrawable backgroundGradientTop(View container) {
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setShader(new LinearGradient(0,0,0,container.getLayoutParams().height + 10, Color.parseColor("#303F9F"), SettingsUtil.getBackgroundColor(), Shader.TileMode.REPEAT));
        return d;
    }
    public static ShapeDrawable backgroundGradientBottom(View container) {
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setShader(new LinearGradient(0,0,0,container.getLayoutParams().height + 10, SettingsUtil.getBackgroundColor(), Color.parseColor("#303F9F"), Shader.TileMode.REPEAT));
        return d;
    }
    public static Drawable getDrawable(int id) {
        if (Build.VERSION.SDK_INT >= 21) {
            return SystemOverlay.service.getDrawable(id);
        }else{
            return SystemOverlay.service.getResources().getDrawable(id);
        }
    }
    public static void sendNotification(Context c, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c).setSmallIcon(R.drawable.floater_dots).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentTitle(title).setContentText(text);
        int NOTIFICATION_ID = new Random().nextInt(5000);
        Intent targetIntent = new Intent(c, UserInterface.class);
        PendingIntent contentIntent = PendingIntent.getActivity(c, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }
}
