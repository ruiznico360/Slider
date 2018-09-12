package mobile.slider.app.slider.services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.TimeUtils;

import java.util.concurrent.TimeUnit;

import mobile.slider.app.slider.ui.Slider;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

@TargetApi (26)
public class NotificationListener extends NotificationListenerService {

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }
    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onListenerConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }

    @Override
    public void onCreate() {
        Util.log("created nl");
        super.onCreate();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (getActiveNotifications() != null) {
                        for (StatusBarNotification s : getActiveNotifications()) {
                            checkNotification(s);
                        }
                    }
                }catch (RuntimeException e) {
                    new Handler().postDelayed(this,1);
                }
            }
        },1);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void checkNotification(StatusBarNotification sbn) {
        if ((sbn.getTag() != null && sbn.getTag().contains(getPackageName()))) {
            snoozeNotification(sbn.getKey(), TimeUnit.DAYS.toMillis(1000));
        }else if (sbn.getNotification().extras != null && (sbn.getNotification().extras.getInt(IntentExtra.SLIDER_NOTIFICATION_SETUP) == -1)) {
            snoozeNotification(sbn.getKey(), (100));
        }
        if (SystemClock.uptimeMillis() - Slider.START_TIME > 50) {
            if ((sbn.getPackageName().equals("android")) && sbn.getTag() == null) {
                snoozeNotification(sbn.getKey(), TimeUnit.DAYS.toMillis(1000));
            }
        }
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        checkNotification(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}
