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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
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
import mobile.slider.app.slider.ui.UILayout;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

public class SystemOverlay extends Service {
    public static SystemOverlay service;
    public PowerReceiver powerReceiver;
    public static RelativeLayout container;
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
        screenStateFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        powerReceiver = new PowerReceiver();
        registerReceiver(powerReceiver, screenStateFilter);
        if (Build.VERSION.SDK_INT >= 21) {
            ComponentName mServiceComponent = new ComponentName(this, RestarterJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, mServiceComponent);
            builder.setMinimumLatency(2000);
            builder.setOverrideDeadline((long) (2000 * 1.05));
            builder.setRequiresDeviceIdle(false);
            builder.setRequiresCharging(false); // we don't care if the device is charging or not
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
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
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(container);
        service = null;
        container = null;
        overlayFloater = null;
        backgroundFloater = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
    }
    public int floaterPos() {
        double floaterPos;
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        double width = dm.widthPixels;
        double height = dm.heightPixels;
        int orientation = getResources().getConfiguration().orientation;
//        if (SettingsUtil.getLastFloaterUpdate().equals(FloaterUpdate.LANDSCAPE)) {
//            if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
//                double ratio = height / width;
//                double newFloaterPos = floaterPos * ratio;
//                floaterPos = (int)(newFloaterPos);
//            }
//        }else if (SettingsUtil.getLastFloaterUpdate().equals(FloaterUpdate.PORTRAIT)) {
//            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
//                double ratio = width / height;
//                double newFloaterPos = floaterPos / ratio;
//                floaterPos = (int)(newFloaterPos);
//            }
//        }
        floaterPos = SettingsUtil.getFloaterPos() * height;
        if (floaterPos < FloaterController.BORDER) {
            floaterPos = FloaterController.BORDER;
        }else if (floaterPos + (SettingsUtil.getFloaterSize() * 1.2) > height - FloaterController.BORDER) {
            floaterPos = height - FloaterController.BORDER - (SettingsUtil.getFloaterSize() * 1.2);
        }

        return (int)floaterPos;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (UserInterface.running) {
            UserInterface.remove(getApplicationContext());
        }
        createFloater(overlayFloater.getVisibility());
        super.onConfigurationChanged(newConfig);
    }
    public void createFloater(int visibility) {
        super.onCreate();
        final ImageView floater = new ImageView(getApplicationContext());
        final ImageView background = new ImageView(getApplicationContext());
        final RelativeLayout layout = new RelativeLayout(getApplicationContext());
        layout.setGravity(RelativeLayout.CENTER_VERTICAL);
        int floaterPos = floaterPos();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.height = (int)(SettingsUtil.getFloaterSize() * 1.2);
        params.y = floaterPos;
        int gravity = 0;
        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
            params.gravity = Gravity.RIGHT | Gravity.TOP;
            gravity = RelativeLayout.ALIGN_PARENT_RIGHT;
        }else if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            params.gravity = Gravity.LEFT | Gravity.TOP;
            gravity = RelativeLayout.ALIGN_PARENT_LEFT;
            floater.setScaleX(-1);
            background.setScaleX(-1);
        }
        int width = 0;
        if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.DOTS)) {
            width = (int)((SettingsUtil.getFloaterSize() / (200 / 50)) * 1.5);
            floater.setAlpha(0.85f);
            Util.setImageDrawable(floater, R.drawable.floater_dots);
            invisibleIcon = false;
            params.width =  (int)((SettingsUtil.getFloaterSize() * 1.2) / (100 / 50));
            Util.setBackground(background, R.drawable.floater_background);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.TRANSLUCENT)) {
            width = (int)((SettingsUtil.getFloaterSize() / (515 / 50)) * 2.2);
            floater.setAlpha(0.7f);
            Util.setImageDrawable(floater, R.drawable.floater_translucent);
            invisibleIcon = false;
            Util.setImageDrawable(background, R.drawable.floater_background);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.INVISIBLE)) {
            width = (SettingsUtil.getFloaterSize() / 5);
            invisibleIcon = true;
        }
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(layout, params);

        floater.setScaleType(ImageView.ScaleType.FIT_END);
        background.setAlpha(0.8f);
        background.setAlpha(0f);
        if (container != null) {
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(container);
        }
        layout.addView(background);
        layout.addView(floater);
        RelativeLayout.LayoutParams floaterParams = (RelativeLayout.LayoutParams) floater.getLayoutParams();
        floaterParams.width = width;
        floaterParams.addRule(RelativeLayout.CENTER_VERTICAL);
        floaterParams.addRule(gravity);
        floaterParams.height = SettingsUtil.getFloaterSize();
        layout.updateViewLayout(floater, floaterParams);
        container = layout;
        floaterMovement = new FloaterController(container, floater, background, params, getApplicationContext());
        overlayFloater = floater;
        backgroundFloater = background;
        if (visibility == View.VISIBLE) {
            showFloater();
        }else{
            hideFloater();
        }

    }
    public static void hideFloater() {
        floaterMovement.enableTouch(false);
        Animation a = AnimationUtils.loadAnimation(service.getApplicationContext(), R.anim.fade_out);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                overlayFloater.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        overlayFloater.startAnimation(a);
    }
    public static void showFloater() {
        overlayFloater.setVisibility(View.VISIBLE);
        floaterMovement.enableTouch(true);
        overlayFloater.startAnimation(AnimationUtils.loadAnimation(service.getApplicationContext(), R.anim.fade_in));
    }

    public void launchUI() {
        if (Util.isLocked(getApplicationContext())) {
            if (SystemOverlay.floaterMovement.inTouch) {
                SystemOverlay.floaterMovement.forceUp();
            }
            SystemOverlay.hideFloater();
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
            RelativeLayout ui = new UILayout.LockedActivityView(getApplicationContext());
            View inner = UILayout.init(getApplicationContext());
            ui.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        int rx = (int)event.getRawX();
                        int ry = (int)event.getRawY();
                        int[] l = new int[2];
                        v.getLocationOnScreen(l);
                        int x = l[0];
                        int y = l[1];
                        int w = v.getWidth();
                        int h = v.getHeight();
                        if (rx < x || rx > x + w || ry < y || ry > y + h) {
                            if (UserInterface.running) {
                                UserInterface.remove(getApplicationContext());
                            }
                        }
                    }
                    return true;
                }
            });
            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;

            params.width = (width / 5);
            params.height = 2560;

            if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
                params.gravity = Gravity.RIGHT | Gravity.TOP;
            }else if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
                params.gravity = Gravity.LEFT | Gravity.TOP;
            }
            ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).addView(ui, params);
            ui.addView(inner);
            inner.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            UserInterface.ui = ui;
            Animation a;
            if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
                a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_right_to_middle);
            }else {
                a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_left_to_middle);
            }
            inner.startAnimation(a);
            UserInterface.running = true;
        }else {
            if (SystemOverlay.floaterMovement.inTouch) {
                SystemOverlay.floaterMovement.forceUp();
            }
            SystemOverlay.hideFloater();
            Intent intent = new Intent(this, UserInterface.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, 0);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    public class FloaterController {
        public static final int BORDER = 200;
        Handler longPress;
        Runnable startLongPress;
        boolean startSliding = false;
        int initialY, initialX;
        float initialTouchY, initialTouchX;
        float x1 = 0.0f;
        float y1 = 0.0f;
        int multiplier;
        public RelativeLayout container;
        public ImageView overlayFloater,background;
        public WindowManager.LayoutParams params;
        public Context c;
        public View.OnTouchListener touchListener;
        public boolean inTouch = false;
        public float yOffset = 0;

        public FloaterController(RelativeLayout container, ImageView of, ImageView bg, WindowManager.LayoutParams params, Context con) {
            this.overlayFloater = of;
            this.container = container;
            this.background = bg;
            this.params = params;
            this.c = con;

            touchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        down(event);
                    }else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        up(event, false);
                    }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        move(event);
                    }
                    return true;
                }
            };
            overlayFloater.setOnTouchListener(touchListener);
        }
        public void enableTouch(boolean enable) {
            if (enable) {
                overlayFloater.setOnTouchListener(touchListener);
            }else{
                overlayFloater.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
            }
        }
        public void down(final MotionEvent event) {
            inTouch = true;
            if (startSliding) {
                startSliding = false;
            }
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
                    int[] l = new int[2];
                    container.getLocationOnScreen(l);
                    yOffset = event.getRawY() - l[1];
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
                createFloater(overlayFloater.getVisibility());
            }else if (!force){
                float x2 = event.getX();

                float y2 = event.getY();
                float dx = x2 - x1;
                float dy = y2 - y1;
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (!(dx > 0) && SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        launchUI();
                    } else if ((dx > 0) && SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        launchUI();
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
                DisplayMetrics dm = new DisplayMetrics();
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
                double width = dm.widthPixels / 2;
                float rawY = event.getRawY() - yOffset;
                if (rawY + container.getHeight() > (dm.heightPixels - (BORDER))) {
                    rawY = ((dm.heightPixels - (BORDER)) - container.getHeight());
                }
                else if (rawY < BORDER) {
                    rawY = ((BORDER));
                }
                params.x = multiplier * (initialX + (int) (event.getRawX() - initialTouchX));
                params.y = (int) (rawY);

                SettingsUtil.setFloaterPos((rawY) / (dm.heightPixels));

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.LANDSCAPE);
                } else {
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.PORTRAIT);
                }
                if (event.getRawX() < width) {
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        overlayFloater.setScaleX(-1);
                        SettingsUtil.setFloaterGravity(WindowGravity.LEFT);
                    }
                } else if (event.getRawX() >= width) {
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        overlayFloater.setScaleX(1);
                        SettingsUtil.setFloaterGravity(WindowGravity.RIGHT);
                    }
                }
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(container, params);
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
            }
        }
    }
}
