package mobile.slider.app.slider.services;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

import java.util.concurrent.TimeUnit;

import mobile.slider.app.slider.util.Util;

@RequiresApi(26)
public class NotificationListener extends NotificationListenerService {

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onListenerDisconnected() {
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
            snoozeNotification(sbn.getKey(), (10000));
        }
        if ((sbn.getPackageName().equals("android"))) {
            if (sbn.getNotification().extras != null && sbn.getNotification().extras.get("android.title") != null && (sbn.getNotification().extras.get("android.title").toString().equals("Slider is displaying over other apps") || sbn.getNotification().extras.get("android.title").toString().equals("Slider is running in the background"))) {
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
