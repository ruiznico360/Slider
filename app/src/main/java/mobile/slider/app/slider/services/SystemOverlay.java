package mobile.slider.app.slider.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.SView.SWindowLayout;
import mobile.slider.app.slider.model.floater.Floater;
import mobile.slider.app.slider.model.window.Window;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.FloaterIcon;
import mobile.slider.app.slider.settings.resources.FloaterUpdate;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.ui.Slider;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.ToastMessage;
import mobile.slider.app.slider.util.Util;

public class SystemOverlay extends Service {
    public static SystemOverlay service;
    public static Floater floater;
    public static DeviceStateListener deviceStateListener;

    public static void start(Context c, String intent) {
        Intent i = new Intent(c,SystemOverlay.class);
        if (intent != null) {
            i.putExtra(intent, true);
        }
        new ContextWrapper(c).startService(i);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Slider.canUseOverlay(this)) {
            super.onCreate();
            processIntent(intent);
            startInForeground();
//            startJob();
        }else{
            stopSelf();
        }
        return START_STICKY;
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
            floater.updateFloater();
        }
        floater.currentOrientation = newConfig.orientation;
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

    public void processIntent(Intent intent) {
        if (!SettingsWriter.running) {
            SettingsWriter.init(getApplicationContext());
        }
        deviceStateListener = new DeviceStateListener();
        deviceStateListener.start();
        if (intent != null) {
            if (intent.getExtras() != null) {
                if (intent.getExtras().containsKey(IntentExtra.FROM_UI)) {
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from UI");
                    Floater.createFloater(View.INVISIBLE);
                    UserInterface.launchUI();
                }else if (intent.getExtras().containsKey(IntentExtra.SAFE_REBOOT_SERVICE)) {
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from Reboot");
                    Floater.createFloater(View.VISIBLE);
                }else{
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from intent with no extras");
                    Floater.createFloater(View.VISIBLE);
                }
            }else {
                Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from intent with null extras");
                Floater.createFloater(View.VISIBLE);
            }
        }else{
            Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from null intent");
            Floater.createFloater(View.VISIBLE);
        }

    }
    public void startInForeground() {
        Intent pi = new Intent();
        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            pi.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            pi.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        }else if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            pi.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            pi.putExtra("app_package", getPackageName());
            pi.putExtra("app_uid", getApplicationInfo().uid);
        }else {
            pi.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            pi.addCategory(Intent.CATEGORY_DEFAULT);
            pi.setData(Uri.parse("package:" + getPackageName()));
        }
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                pi, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon_status)
                .setContent(contentView)
                .setContentIntent(pendingIntent).build();

        this.startForeground(1234567, notification);
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
    public class DeviceStateListener {
        public Handler handler = new Handler();
        public ArrayList<Runnable> tasks = new ArrayList<>();

        public void start() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < tasks.size(); i++) {
                        tasks.get(i).run();
                    }
                    handler.postDelayed(this,500);
                }
            },500);
        }
    }
}
