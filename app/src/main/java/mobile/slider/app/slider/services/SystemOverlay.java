package mobile.slider.app.slider.services;

import android.animation.Animator;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.AnimatorRes;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.FloaterIcon;
import mobile.slider.app.slider.settings.resources.FloaterUpdate;
import mobile.slider.app.slider.settings.resources.SettingType;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

public class SystemOverlay extends Service {
    public static SystemOverlay service;
    public PowerReceiver powerReceiver;
    public static ImageView overlayFloater;
    public static FloaterController floaterMovement;
    private ImageView backgroundFloater;
    private boolean invisibleIcon = false;

    public static void start(Context c, String intent) {
        Intent i = new Intent(c,SystemOverlay.class);
        if (intent != null) {
            i.putExtra(intent, true);
        }
        c.startService(i);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getExtras() != null) {
                if (intent.getExtras().containsKey(IntentExtra.FROM_UI)) {
                    createFloater(View.INVISIBLE);
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "View created invisible from UI");
                }else if (intent.getExtras().containsKey(IntentExtra.SAFE_REBOOT_SERVICE)) {
                    SettingsWriter.init(getApplicationContext());
                    Util.sendNotification(getApplicationContext(), "Restarter", "Created visible");
                    createFloater(View.VISIBLE);
                }else{
                    createFloater(View.VISIBLE);
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created visible from intent with no matching extras");
                }
            }else {
                createFloater(View.VISIBLE);
                Util.sendNotification(getApplicationContext(), "SystemOverlay", "View created visible from intent with no extras");
            }
        }else{
                SettingsWriter.init(getApplicationContext());
                Util.sendNotification(getApplicationContext(), "SystemOverlay", "View created visible from null intent");
                createFloater(View.VISIBLE);
        }
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        powerReceiver = new PowerReceiver();
        registerReceiver(powerReceiver, screenStateFilter);
        if (Build.VERSION.SDK_INT >= 21) {
            ComponentName mServiceComponent = new ComponentName(this, RestarterJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, mServiceComponent);
            builder.setRequiresDeviceIdle(false); // device should be idle
            builder.setPeriodic(2000);
            builder.setRequiresCharging(false); // we don't care if the device is charging or not
            JobScheduler jobScheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }else {
            Intent i = new Intent(getApplicationContext(), Restarter.class);
            PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 2000, pintent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(overlayFloater);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(backgroundFloater);
        service = null;
        overlayFloater = null;
        backgroundFloater = null;
        unregisterReceiver(powerReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
    }
    public int floaterPos() {
        int floaterPos = SettingsUtil.getFloaterPos();
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        double width = dm.widthPixels;
        double height = dm.heightPixels;
        int orientation = getResources().getConfiguration().orientation;
        if (SettingsUtil.getLastFloaterUpdate().equals(FloaterUpdate.LANDSCAPE)) {
            if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
                double ratio = height / width;
                double newFloaterPos = floaterPos * ratio;
                floaterPos = (int)(newFloaterPos);
            }
        }else if (SettingsUtil.getLastFloaterUpdate().equals(FloaterUpdate.PORTRAIT)) {
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                double ratio = width / height;
                double newFloaterPos = floaterPos / ratio;
                floaterPos = (int)(newFloaterPos);
            }
        }
        return floaterPos;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        createFloater(overlayFloater.getVisibility());
    }
    public void createFloater(int visibility) {
        super.onCreate();
        final ImageView floater = new ImageView(getApplicationContext());
        final ImageView background = new ImageView(getApplicationContext());

        int floaterPos = floaterPos();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.height = SettingsUtil.getFloaterSize();
        params.y = floaterPos;

        final WindowManager.LayoutParams backgroundParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        backgroundParams.height = (int)(SettingsUtil.getFloaterSize() * 1.2);
        backgroundParams.y = floaterPos;

        if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.DOTS)) {
            params.width = (int)((params.height / (200 / 50)) * 1.5);
            floater.setAlpha(0.85f);
            Util.setImageDrawable(floater, R.drawable.floater_dots);
            invisibleIcon = false;
            backgroundParams.width =  (int)((backgroundParams.height / (100 / 50)));
            Util.setImageDrawable(background, R.drawable.floater_background);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.TRANSLUCENT)) {
            params.width = (int)((params.height / (515 / 50)) * 2.2);
            floater.setAlpha(0.7f);
            Util.setImageDrawable(floater, R.drawable.floater_translucent);
            invisibleIcon = false;
            backgroundParams.width = 0;
            Util.setImageDrawable(background, R.drawable.floater_background);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.INVISIBLE)) {
            params.width = (params.height / 5);
            invisibleIcon = true;
        }
        floater.setScaleType(ImageView.ScaleType.FIT_END);
        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
            params.gravity = Gravity.RIGHT;
            backgroundParams.gravity = Gravity.RIGHT;
        }else if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            params.gravity = Gravity.LEFT;
            backgroundParams.gravity = Gravity.LEFT;
            floater.setScaleX(-1);
            background.setScaleX(-1);
        }
        background.setAlpha(0.8f);
        floater.setLayoutParams(params);
        background.setLayoutParams(backgroundParams);
        background.setAlpha(0f);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(background, backgroundParams);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(floater, params);
        if (overlayFloater != null) {
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(overlayFloater);
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(backgroundFloater);
        }
        floaterMovement = new FloaterController(floater, background, params, backgroundParams, getApplicationContext());
        overlayFloater = floater;
        backgroundFloater = background;
        floater.setVisibility(visibility);
    }
    public static void hideFloater() {
        overlayFloater.setVisibility(View.INVISIBLE);
    }
    public static void showFloater() {
        overlayFloater.setVisibility(View.VISIBLE);
    }
    public class FloaterController {
        Handler longPress;
        Runnable startLongPress;
        boolean startSliding = false;
        int initialY, initialX;
        float initialTouchY, initialTouchX;
        float x1 = 0.0f;
        float y1 = 0.0f;
        int multiplier;
        public ImageView overlayFloater,background;
        public WindowManager.LayoutParams params, backgroundParams;
        public Context c;
        public View.OnTouchListener touchListener;
        public boolean inTouch = false;

        public FloaterController(ImageView of, ImageView bg, WindowManager.LayoutParams pm, WindowManager.LayoutParams bp, Context con) {
            this.overlayFloater = of;
            this.background = bg;
            this.params = pm;
            this.backgroundParams = bp;
            this.c = con;

            touchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        down(event);
                    }else if (event.getAction() == MotionEvent.ACTION_UP) {
                        up(event, false);
                    }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        move(event);
                    }
                    return true;
                }
            };
            overlayFloater.setOnTouchListener(touchListener);
        }
        public void down(final MotionEvent event) {
            inTouch = true;
            startLongPress = new Runnable() {
                @Override
                public void run() {
                    Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(50);
                    startSliding = true;
                    initialY = params.y;
                    initialX = params.x;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    background.setAlpha(0f);
                    if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        multiplier = -1;
                    }else {
                        multiplier = 1;
                    }
                }
            };
            longPress = new Handler();
            longPress.postDelayed(startLongPress, 500);
            x1 = event.getX();
            y1 = event.getY();
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(50);
            if (invisibleIcon) {
                overlayFloater.setBackgroundColor(Color.parseColor("#50000000"));
            } else {
                background.setAlpha(0.8f);
            }
        }
        public void forceUp() {
            up(null, true);
        }
        public void up(final MotionEvent event, boolean force) {
            inTouch = false;
            longPress.removeCallbacks(startLongPress);
            if (startSliding) {
                if (invisibleIcon) {
                    overlayFloater.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    background.setAlpha(0f);
                }
                params.x = 0;
                backgroundParams.x = 0;
                if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                    params.gravity = Gravity.RIGHT;
                    backgroundParams.gravity = Gravity.RIGHT;
                }else if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                    params.gravity = Gravity.LEFT;
                    backgroundParams.gravity = Gravity.LEFT;
                }
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(overlayFloater, params);
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(background, backgroundParams);
            }else if (!force){
                float x2 = event.getX();
                float y2 = event.getY();
                float dx = x2 - x1;
                float dy = y2 - y1;
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (!(dx > 0) && SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        Intent in = new Intent(getApplicationContext(), UserInterface.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getApplication().startActivity(in);
                    } else if ((dx > 0) && SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        Intent in = new Intent(getApplicationContext(), UserInterface.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getApplication().startActivity(in);
                    }
                }
                if (invisibleIcon) {
                    overlayFloater.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    background.setAlpha(0f);
                }
            }
            startSliding = false;
        }

        public void move(final MotionEvent event) {
            if (startSliding) {
                if (invisibleIcon) {
                    overlayFloater.setBackgroundColor(Color.parseColor("#50000000"));
                } else {
                    background.setAlpha(0f);
                }
                params.x = multiplier * (initialX + (int) (event.getRawX() - initialTouchX));
                backgroundParams.x = multiplier * (initialX + (int) (event.getRawX() - initialTouchX));
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                backgroundParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                SettingsUtil.setFloaterPos(params.y);
                DisplayMetrics dm = new DisplayMetrics();
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
                double width = dm.widthPixels / 2;

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.LANDSCAPE);
                }else{
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.PORTRAIT);
                }
                if (event.getRawX() < width) {
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        overlayFloater.setScaleX(-1);
                        background.setScaleX(-1);
                        SettingsUtil.setFloaterGravity(WindowGravity.LEFT);
                    }
                }else  if (event.getRawX() >= width) {
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        overlayFloater.setScaleX(1);
                        background.setScaleX(1);
                        SettingsUtil.setFloaterGravity(WindowGravity.RIGHT);
                    }
                }
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(overlayFloater, params);
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(background, backgroundParams);
            }else{
                int rx = (int)event.getRawX();
                int ry = (int)event.getRawY();
                int[] l = new int[2];
                background.getLocationOnScreen(l);
                int x = l[0];
                int y = l[1];
                int w = background.getWidth() * 2;
                int h = background.getHeight();
                if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                    w = x;
                    x = 0;
                }else{
                    x -= background.getWidth();
                }
                if (rx < x || rx > x + w || ry < y || ry > y + h) {
                    longPress.removeCallbacks(startLongPress);
                }
                Util.log(x + " " + y + " " + " " + (x + w) + " " + (y + h) + " " + w + " " + h + " " + rx + " " + ry);
            }
        }
    }
}
