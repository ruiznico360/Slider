package mobile.slider.app.slider.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
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

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.FloaterIcon;
import mobile.slider.app.slider.settings.resources.FloaterUpdate;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.ui.Slider;
import mobile.slider.app.slider.ui.UI;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

public class SystemOverlay extends Service {
    public static SystemOverlay service;
    public static Floater floater;
    public int innn = 0;


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
            processIntent(intent);
            startJob();
        }else{
            stopSelf();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent();
                i.setAction("howdy");
                sendBroadcast(i);
                new Handler().postDelayed(this,500);
            }
        },500);
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
            updateFloater();
        }
        floater.currentOrientation = newConfig.orientation;
    }

    public void processIntent(Intent intent) {
        if (Util.screenHeight() > Util.screenWidth()) {
            FloaterController.BORDER = Util.screenWidth() / 10;
        }else{
            FloaterController.BORDER = Util.screenHeight() / 10;
        }

        if (!SettingsWriter.running) {
            SettingsWriter.init(getApplicationContext());
        }
        if (intent != null) {
            if (intent.getExtras() != null) {
                if (intent.getExtras().containsKey(IntentExtra.FROM_UI)) {
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from UI");
                    createFloater(View.INVISIBLE);
                    launchUI();
                }else if (intent.getExtras().containsKey(IntentExtra.SAFE_REBOOT_SERVICE)) {
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from Reboot");
                    createFloater(View.VISIBLE);
                }else{
                    Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from intent with no extras");
                    createFloater(View.VISIBLE);
                }
            }else {
                Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from intent with null extras");
                createFloater(View.VISIBLE);
            }
        }else{
            Util.sendNotification(getApplicationContext(), "SystemOverlay", "Created from null intent");
            createFloater(View.VISIBLE);
        }
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
            Intent i = new Intent(getApplicationContext(), Restarter.class);
            PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30000, pintent);
//        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        CustomToast.makeToast("Low Mem");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CustomToast.makeToast("destroyed");

    }

    public int floaterPos() {
        double floaterPos;
        double height = Util.screenHeight();
        floaterPos = SettingsUtil.getFloaterPos() * height;
        if (floaterPos < FloaterController.BORDER) {
            floaterPos = FloaterController.BORDER;
        }else if (floaterPos + (SettingsUtil.getFloaterSize()) > height - FloaterController.BORDER) {
            floaterPos = height - FloaterController.BORDER - (SettingsUtil.getFloaterSize());
        }
        return (int)floaterPos;
    }

    public void updateFloater() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) floater.container.getLayoutParams();
        params.y = floaterPos();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(floater.container, params);

    }
    public void createFloater(int visibility) {
        super.onCreate();
        final ImageView floater = new AppCompatImageView(getApplicationContext());
        final ImageView background = new ImageView(getApplicationContext());
        final RelativeLayout container = new RelativeLayout(getApplicationContext());

        int floaterPos = floaterPos();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                SettingsUtil.getFloaterSize(), WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

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
            width = (int)((SettingsUtil.getFloaterSize() / (200 / 50)));
            params.width =  (SettingsUtil.getFloaterSize()) / (100 / 50);
            Util.setImageDrawable(floater, R.drawable.floater_dots);
            Util.setBackground(background, R.drawable.floater_background);
            floater.setAlpha(0.85f);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.TRANSLUCENT)) {
            width = (int)((SettingsUtil.getFloaterSize() / (515 / 50)));
            Util.setImageDrawable(background, R.drawable.floater_background);
            Util.setImageDrawable(floater, R.drawable.floater_translucent);
            floater.setAlpha(0.7f);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.INVISIBLE)) {
            width = (SettingsUtil.getFloaterSize() / 5);
        }
        container.setGravity(RelativeLayout.CENTER_VERTICAL);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(container, params);

        floater.setScaleType(ImageView.ScaleType.FIT_END);
        background.setAlpha(0.8f);
        background.setAlpha(0f);
        if (SystemOverlay.floater != null) {
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(SystemOverlay.floater.container);
        }
        container.addView(background);
        container.addView(floater);
        RelativeLayout.LayoutParams floaterParams = (RelativeLayout.LayoutParams) floater.getLayoutParams();
        floaterParams.width = width;
        floaterParams.addRule(RelativeLayout.CENTER_VERTICAL);
        floaterParams.addRule(gravity);
        floaterParams.height = SettingsUtil.getFloaterSize() - (SettingsUtil.getFloaterSize() / 5);
        container.updateViewLayout(floater, floaterParams);
        FloaterController floaterMovement = new FloaterController(container, floater, background, params, getApplicationContext());
        SystemOverlay.floater = new Floater(container, floater, floaterMovement, background, visibility);
        if (visibility == View.VISIBLE) {
            showFloater();
        }else{
            hideFloater();
        }
    }
    public static void disableFloater() {
        ((WindowManager)service.getSystemService(WINDOW_SERVICE)).removeView(floater.container);
        floater = null;
    }
    public static void hideFloater() {
        floater.floaterMovement.enableTouch(false);
        Animation a = AnimationUtils.loadAnimation(service.getApplicationContext(), R.anim.fade_out);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                floater.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        floater.overlayFloater.startAnimation(a);
    }
    public static void showFloater() {
        floater.setVisibility(View.VISIBLE);
        floater.floaterMovement.enableTouch(true);
        floater.overlayFloater.startAnimation(AnimationUtils.loadAnimation(service.getApplicationContext(), R.anim.fade_in));
    }

    public void launchUI() {
        int size;
        if (Util.screenHeight() > Util.screenWidth()) {
            size = Util.screenWidth() / 5;
        }else{
            size = Util.screenHeight() / 5;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = size;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD + WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        if (Util.isLocked(getApplicationContext())) {
            if (params.type != WindowManager.LayoutParams.TYPE_SYSTEM_ERROR) {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
        }else{
            if (params.type != WindowManager.LayoutParams.TYPE_PHONE) {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
        }
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            params.gravity = Gravity.RIGHT | Gravity.TOP;
        }else if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
            params.gravity = Gravity.LEFT | Gravity.TOP;
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (params.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        }else{
            if (params.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            }
        }


        UI.UILayout uiLayout = new UI.UILayout(this);
        View inner = UI.userInterface(getApplicationContext());
        uiLayout.setOnTouchListener(new View.OnTouchListener() {
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
                        if (UI.running) {
                            UI.remove(getApplicationContext());
                        }
                    }
                }
                return true;
            }
        });
        inner.findViewById(R.id.button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    String s = "";
                    CustomToast.makeToast(test.i + "");
                }
                return true;
            }
        });
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).addView(uiLayout, params);
        uiLayout.addView(inner);
        inner.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        UI.uiLayout = uiLayout;
        if (floater.floaterMovement.inTouch) {
            floater.floaterMovement.forceUp();
        }
        SystemOverlay.hideFloater();

        UI.uiLayout.setVisibility(View.VISIBLE);
        Animation a;
        if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
            a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_right_to_middle);
        }else {
            a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_left_to_middle);
        }
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                UI.running = true;
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        UI.uiLayout.getChildAt(0).startAnimation(a);
        final boolean phoneStatus = Util.isLocked(getApplicationContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (UI.running) {
                    if (phoneStatus != Util.isLocked(getApplicationContext())) {
                        UI.remove(getApplicationContext());
                    }else {
                        new Handler().postDelayed(this, 500);
                    }
                }
            }
        }, 500);
    }

    public class Floater {
        public RelativeLayout container;
        public ImageView overlayFloater;
        public FloaterController floaterMovement;
        public ImageView backgroundFloater;
        public int currentOrientation;
        private int visibility;

        public Floater(RelativeLayout container, ImageView overlayFloater, FloaterController floaterController,  ImageView backgroundFloater, int visibility) {
            this.container = container;
            this.overlayFloater = overlayFloater;
            this.floaterMovement = floaterController;
            this.backgroundFloater = backgroundFloater;
            this.visibility = visibility;
            container.setVisibility(visibility);
            currentOrientation = getResources().getConfiguration().orientation;
        }
        public void setVisibility(int visibility) {
            this.visibility = visibility;
            container.setVisibility(visibility);
        }
        public int getVisibility() {
            return visibility;
        }
    }

    public static class FloaterController {
        public static int BORDER;
        public Handler longPress;
        public Runnable startLongPress;
        public boolean startSliding = false, inTouch = false;
        public float initialX,initialTouchX,x1,y1,yOffset = 0, originalY;
        public int multiplier;
        public RelativeLayout container;
        public ImageView overlayFloater,background;
        public WindowManager.LayoutParams params;
        public Context c;
        public View.OnTouchListener touchListener;
        public Garbage garbage;
        public String originalLastFloaterUpdate, originalGravity;

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
            container.setOnTouchListener(touchListener);
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
                    Vibrator vib = (Vibrator) service.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(50);
                    startSliding = true;
                    int[] l = new int[2];
                    container.getLocationOnScreen(l);
                    yOffset = event.getRawY() - l[1];
                    background.setAlpha(0f);
                    initialX = params.x;
                    initialTouchX = event.getRawX();
                    if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        multiplier = -1;
                    }else {
                        multiplier = 1;
                    }
                    originalGravity = SettingsUtil.getFloaterGravity();
                    originalY = SettingsUtil.getFloaterPos();
                    originalLastFloaterUpdate = SettingsUtil.getLastFloaterUpdate();

                    RelativeLayout background = new RelativeLayout(c);
                    final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                            SettingsUtil.getFloaterSize() * 2, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, PixelFormat.TRANSLUCENT);
                    params.gravity = Gravity.BOTTOM;
                    ((WindowManager) c.getSystemService(WINDOW_SERVICE)).addView(background, params);
                    ShapeDrawable d = new ShapeDrawable(new RectShape());
                    d.getPaint().setShader(new LinearGradient(0,0,0,params.height, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.REPEAT));
                    RelativeLayout gradient = new RelativeLayout(c);
                    ImageView trash = new ImageView(c);
                    trash.setScaleType(ImageView.ScaleType.FIT_XY);
                    trash.setAdjustViewBounds(true);
                    Util.setBackground(gradient, d);
                    Util.setImageDrawable(trash, R.drawable.garbage);
                    background.addView(gradient);
                    gradient.addView(trash);

                    RelativeLayout.LayoutParams gradientParams = (RelativeLayout.LayoutParams) gradient.getLayoutParams();
                    gradientParams.width = params.width;
                    gradientParams.height = params.height;
                    gradientParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    background.updateViewLayout(gradient, gradientParams);

                    RelativeLayout.LayoutParams trashParams = (RelativeLayout.LayoutParams) trash.getLayoutParams();
                    trashParams.height = (int)(SettingsUtil.getFloaterSize() / 1.3);
                    trashParams.width = trashParams.height;
                    trashParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    gradient.updateViewLayout(trash, trashParams);

                    gradient.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fade_in));

                    garbage = new Garbage(background, gradient , trash);

                }
            };
            x1 = event.getX();
            y1 = event.getY();
            longPress = new Handler();
            longPress.postDelayed(startLongPress, 500);
            Vibrator vib = (Vibrator) service.getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(25);
            if (SettingsUtil.getFloaterIcon() == FloaterIcon.INVISIBLE) {
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
                if (SettingsUtil.getFloaterIcon() == FloaterIcon.INVISIBLE) {
                    overlayFloater.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    background.setAlpha(0f);
                }

                if (garbage.trash.getHeight() == SettingsUtil.getFloaterSize()) {
                    hideFloater();
                    SettingsUtil.setLastFloaterUpdate(originalLastFloaterUpdate);
                    SettingsUtil.setFloaterPos(originalY);
                    SettingsUtil.setFloaterGravity(originalGravity);
                    service.createFloater(View.INVISIBLE);
                    CustomToast.makeToast("Hiding Floater. Reopen Slider to activate");
                }else{
                    service.createFloater(overlayFloater.getVisibility());
                }

                ((WindowManager)c.getSystemService(WINDOW_SERVICE)).removeView(garbage.background);
            }else if (!force){
                float x2 = event.getX();

                float y2 = event.getY();
                float dx = x2 - x1;
                float dy = y2 - y1;
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (!(dx > 0) && SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        service.launchUI();
                    } else if ((dx < 0) && SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        service.launchUI();
                    }
                }
                if (SettingsUtil.getFloaterIcon() == FloaterIcon.INVISIBLE) {
                    overlayFloater.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    background.setAlpha(0f);
                }
            }
            startSliding = false;
        }

        public void move(final MotionEvent event) {
            if (startSliding) {
                if (SettingsUtil.getFloaterIcon() == FloaterIcon.INVISIBLE) {
                    overlayFloater.setBackgroundColor(Color.parseColor("#50000000"));
                } else {
                    background.setAlpha(0f);
                }
                double width = Util.screenWidth() / 2;
                int height = Util.screenHeight();
                float rawY = event.getRawY() - yOffset;
                if (rawY + container.getHeight() > (height - (BORDER))) {
                    rawY = ((height - (BORDER)) - container.getHeight());
                }
                else if (rawY < BORDER) {
                    rawY = ((BORDER));
                }
                params.x = (int)(multiplier * (initialX + (int) (event.getRawX() - initialTouchX)));
                params.y = (int) (rawY);

                SettingsUtil.setFloaterPos((rawY) / (height));

                if (service.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
                ((WindowManager) service.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(container, params);

                int[] floaterLoc = new int[2];
                int[] trashLoc = new int[2];
                overlayFloater.getLocationOnScreen(floaterLoc);
                garbage.trash.getLocationOnScreen(trashLoc);
                Rect cRect = new Rect(floaterLoc[0], floaterLoc[1], floaterLoc[0] + overlayFloater.getWidth(), floaterLoc[1] + overlayFloater.getHeight());
                Rect tRect = new Rect();

                if (cRect.intersects(trashLoc[0], trashLoc[1], trashLoc[0] + garbage.trash.getWidth(), trashLoc[1] + garbage.trash.getHeight())) {
                    if (garbage.trash.getHeight() != SettingsUtil.getFloaterSize()) {
                        RelativeLayout.LayoutParams trashParams = (RelativeLayout.LayoutParams) garbage.trash.getLayoutParams();
                        trashParams.height = (int) (SettingsUtil.getFloaterSize());
                        trashParams.width = trashParams.height;
                        garbage.gradient.updateViewLayout(garbage.trash, trashParams);
                    }
                }else{
                    if (garbage.trash.getHeight() != SettingsUtil.getFloaterSize() / 1.3) {
                        RelativeLayout.LayoutParams trashParams = (RelativeLayout.LayoutParams) garbage.trash.getLayoutParams();
                        trashParams.height = (int) (SettingsUtil.getFloaterSize() / 1.3);
                        trashParams.width = trashParams.height;
                        garbage.gradient.updateViewLayout(garbage.trash, trashParams);
                    }
                }


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
        public class Garbage {
            public RelativeLayout background, gradient;
            public ImageView trash;
            public Garbage(RelativeLayout background, RelativeLayout gradient, ImageView trash) {
                this.background = background;
                this.gradient = gradient;
                this.trash = trash;
            }
        }
    }

}
