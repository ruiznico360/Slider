package mobile.slider.app.slider.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.settings.SettingsHandler;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.FloaterIcon;
import mobile.slider.app.slider.settings.resources.SettingType;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.CustomToast;
import mobile.slider.app.slider.util.Util;

public class SystemOverlay extends Service {
    public static SystemOverlay service;
    public static ImageView overlayFloater;
    private ImageView backgroundFloater;
    private boolean startSliding = false;
    private boolean invisibleIcon = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            if (intent.getExtras().containsKey("FromUI")) {
                createFloater(View.INVISIBLE);
            }else{
                createFloater(View.VISIBLE);

            }
        }else{
            createFloater(View.VISIBLE);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
    }

    public void createFloater(int visibility) {
        super.onCreate();
        final ImageView floater = new ImageView(getApplicationContext());
        final ImageView background = new ImageView(getApplicationContext());

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.height = SettingsUtil.getFloaterSize();
        params.y = SettingsUtil.getFloaterPos();

        final WindowManager.LayoutParams backgroundParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        backgroundParams.height = SettingsUtil.getFloaterSize();
        backgroundParams.y = SettingsUtil.getFloaterPos();

        if (overlayFloater != null) {
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(overlayFloater);
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(backgroundFloater);
        }

        if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.DOTS)) {
            floater.setImageDrawable(Util.getDrawable(R.drawable.floater_dots));
            invisibleIcon = false;
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.TRANSLUCENT)) {
            floater.setImageDrawable(Util.getDrawable(R.drawable.floater_translucent));
            invisibleIcon = false;
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.INVISIBLE)) {
            params.width = SettingsUtil.getFloaterSize() / 5;
            invisibleIcon = true;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            background.setBackground(Util.getDrawable(R.drawable.floater_background));
        }else{
            background.setBackgroundDrawable(Util.getDrawable(R.drawable.floater_background));
        }

        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
            params.gravity = Gravity.RIGHT;
            backgroundParams.gravity = Gravity.RIGHT;
        }else if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            params.gravity = Gravity.LEFT;
            backgroundParams.gravity = Gravity.LEFT;
            floater.setScaleX(-1);
            background.setScaleX(-1);
        }

        backgroundParams.height = params.height;
        floater.setLayoutParams(params);
        background.setLayoutParams(backgroundParams);
        background.setVisibility(View.INVISIBLE);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(background, backgroundParams);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(floater, params);

        floater.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startSliding = true;
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);
                return false;
            }
        });
        floater.setOnTouchListener(new View.OnTouchListener() {
            int initialY;
            float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (startSliding) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        initialY = params.y;
                        initialTouchY = event.getRawY();
                        if (invisibleIcon) {
                            overlayFloater.setBackgroundColor(Color.parseColor("#50000000"));
                        } else {
                            background.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        startSliding = false;
                        if (invisibleIcon) {
                            overlayFloater.setBackgroundColor(Color.TRANSPARENT);
                        } else {
                            background.setVisibility(View.INVISIBLE);
                        }
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (invisibleIcon) {
                            overlayFloater.setBackgroundColor(Color.parseColor("#50000000"));
                        } else {
                            background.setVisibility(View.INVISIBLE);
                        }
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        backgroundParams.y = initialY + (int) (event.getRawY() - initialTouchY);

                        SettingsHandler.setSetting(SettingType.FLOATER_POS, initialY + (int) (event.getRawY() - initialTouchY));
                        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(floater, params);
                        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(background, backgroundParams);

                        return true;
                    }
                    return false;
                } else {
                    float x1 = 0.0f;
                    float y1 = 0.0f;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x1 = event.getX();
                            y1 = event.getY();
                            initialY = params.y;
                            initialTouchY = event.getRawY();
                            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(50);
                            if (invisibleIcon) {
                                overlayFloater.setBackgroundColor(Color.parseColor("#50000000"));
                            } else {
                                background.setVisibility(View.VISIBLE);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
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
                                background.setVisibility(View.INVISIBLE);
                            }
                            break;
                    }
                }
                return false;
            }
        });
        overlayFloater = floater;
        backgroundFloater = background;
        floater.setVisibility(visibility);
    }
}
