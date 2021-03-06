package mobile.slider.app.slider.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.contact.Contact;
import mobile.slider.app.slider.model.floater.Floater;
import mobile.slider.app.slider.model.window.Window;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.ui.activity.Setup;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ToastMessage;
import mobile.slider.app.slider.util.Util;

import static android.service.notification.NotificationListenerService.requestRebind;

public class SystemOverlay extends Service {
    public static final int SERV_ID = 1234567;
    public static final String CHANNEL_NAME = "Slider Notification Channel";
    public static final String CHANNEL_ID = "SliderNotfChannel";
    public static final String CHANNEL_DESC= "Notification Channel for Slider Notifications.";

    public static SystemOverlay service;
    public static Floater floater;
    public static PeriodicRunnableHandler periodicRunnableHandler;

    public static void start(Context c, String intent) {
        Intent i = new Intent(c,SystemOverlay.class);
        if (intent != null) {
            i.putExtra(intent, true);
        }
        new ContextWrapper(c).startService(i);
    }

    public static void checkForServiceEnabled(String extra, Context c) {
        if (service == null) {
            if (extra.equals(IntentExtra.SAFE_REBOOT_SERVICE)) {
                start(c, IntentExtra.SAFE_REBOOT_SERVICE);
            }else if (extra.equals(IntentExtra.FROM_UI)){
                start(c, IntentExtra.FROM_UI);
            }
            if (Build.VERSION.SDK_INT >= 26) {
                requestRebind(new ComponentName(c, NotificationListener.class));
            }
        }else {
            floater.hideFloater();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Util.log("starting service");
        if (Setup.hasAllReqPermissions(this)) {
            super.onCreate();
            processIntent(intent);
            periodicRunnableHandler.tasks.add(new Runnable() {
                @Override
                public void run() {
                    if (!Setup.hasAllReqPermissions(SystemOverlay.service)) {
                        SystemOverlay.service.stopSelf();
                    }
                }
            });
            startInForeground();
//            startJob();
        }else{
            stopSelf();
        }
        return START_NOT_STICKY;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);

    }
    @Override
    public void onCreate() {
        service = this;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != floater.currentOrientation) {
            floater.updateFloater(newConfig.orientation);
        }
        if (ToastMessage.currentToast != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(ToastMessage.currentToast);
            ToastMessage.currentToast.removeCallbacks(ToastMessage.remove);
            ToastMessage.currentToast = null;

        }
        if (Window.hasOpenWindows()) {
            for (Window w : Window.openWindows) {
                w.windowContainer.configurationChange();
            }
        }
    }
    public static WindowManager.LayoutParams newWindow(boolean watchOutsideTouch) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT < 26) {

            if (watchOutsideTouch) {
                params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            }else {
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            }

            if (Util.isLocked(SystemOverlay.service.getApplicationContext())) {
                if (params.type != WindowManager.LayoutParams.TYPE_SYSTEM_ERROR) {
                    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                }
            }else{
                if (params.type != WindowManager.LayoutParams.TYPE_PHONE) {
                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
                }
            }
        }else{
            if (watchOutsideTouch) {
                params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            }else {
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            }
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return params;
    }
    public void processIntent(Intent intent) {
        if (!SettingsWriter.running) {
            SettingsWriter.init(getApplicationContext());
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                Contact.contacts = Contact.retrieveContacts();
//                Contact.retrieveContactInfo();
            }
        };
        thread.start();

        periodicRunnableHandler = new PeriodicRunnableHandler();
        periodicRunnableHandler.start();

        Floater.createFloater(View.INVISIBLE);
//        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(IntentExtra.FROM_UI)) {
//
//        }else{
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Util.log("showing");
//                    floater.showFloater(Floater.SHOW_DELAY);
//                }
//            },10000);
//        }
    }
    public void startInForeground() {

        Intent notificationIntent = new Intent();
        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            notificationIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            notificationIntent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        }else if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            notificationIntent.putExtra("app_package", getPackageName());
            notificationIntent.putExtra("app_uid", getApplicationInfo().uid);
        }else {
            notificationIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
            notificationIntent.setData(Uri.parse("package:" + getPackageName()));
        }

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        Bundle extras = new Bundle();
        extras.putInt(IntentExtra.SLIDER_NOTIFICATION_SETUP, -1);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon_status)
                .setContent(contentView)
                .addExtras(extras)
                .setContentIntent(pendingIntent).build();
        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(SERV_ID, notification);

    }

    public void startJob() {
//        if (Build.VERSION.SDK_INT >= 21) {
//            ComponentName mServiceComponent = new ComponentName(this, RestarterJobService.class);
//            JobInfo.Builder builder = new JobInfo.Builder(0, mServiceComponent);
//            builder.setMinimumLatency(30000);
//            builder.setOverrideDeadline((long) (30000 * 1.05));
//            builder.setRequiresDeviceIdle(false);
//            builder.setPersisted(true);
//            builder.setRequiresCharging(false); // we don't care if the device is charging or not
//            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//            jobScheduler.schedule(builder.build());
//        }else {
//            Intent i = new Intent(getApplicationContext(), Restarter.class);
//            PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
//            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30000, pintent);
//        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public static int getOverlayBorder() {
        if (Util.screenHeight() > Util.screenWidth()) {
            return Util.screenWidth() / 10;
        }else{
            return Util.screenHeight() / 10;
        }
    }
    public class PeriodicRunnableHandler {
        public Handler handler = new Handler();
        public ArrayList<Runnable> tasks = new ArrayList<>();
        public Runnable sysTask = new Runnable() {
            @Override
            public void run() {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        if (!UserInterface.running()) {
                            if (Contact.loadedContactIds) {
                                Contact.contacts = Contact.retrieveContacts();
//                                Contact.retrieveContactInfo();
                            }
                        }
                    }
                };
                thread.start();
            }
        };

        public void start() {
            //add window hider
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < tasks.size(); i++) {
                        tasks.get(i).run();
                    }
                    handler.postDelayed(this,500);
                }
            },500);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sysTask.run();
                    handler.postDelayed(this,5000);
                }
            }, 10000);
        }
    }

    public static class IntentExtra {
        public static final String FROM_SETTINGS = "FROM_SETTINGS";
        public static final String FROM_UI = "FROM_UI";
        public static final String SLIDER_NOTIFICATION_SETUP = "SLIDER_NOTIFICATION_SETUP";
        public static final String CON_FIN = "CON_FIN";
        public static final String SAFE_REBOOT_SERVICE = "SAFE_REBOOT_SERVICE";
    }
}
