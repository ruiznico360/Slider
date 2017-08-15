package mobile.slider.app.slider.model.window;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.util.ToastMessage;
import mobile.slider.app.slider.util.Util;

public class Window {
    public Context c;
    public WindowContainer windowContainer;
    public static ArrayList<Window> openWindows = new ArrayList<>();

    public Window(Context c) {
        this.c = c;
        windowContainer = new WindowContainer();

    }
    public static boolean hasOpenWindows() {
        return openWindows.size() > 0;
    }
    public void create() {
        windowContainer.configure();
        openWindows.add(this);
    }
    public void remove() {
        ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).removeView(windowContainer.window);
        ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).removeView(windowContainer.resizeButton);
        ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).removeView(windowContainer.icon);

        openWindows.remove(this);
    }
    public void configurationChange() {
//        WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) windowContainer.window.getLayoutParams();
//        float scaleX = (windowParams.x / Util.screenHeight()) * Util.screenWidth();
//        float scaleY = (windowParams.y / Util.screenWidth()) * Util.screenHeight();
//
//
//        if (scaleY > windowContainer.statusBar.getHeight() && scaleY < Util.screenHeight() - windowContainer.statusBar.getHeight()) {
//            windowParams.y = (int) (scaleY);
//        }
//        if (scaleX + windowParams.width > windowContainer.statusBar.getHeight() && scaleX < Util.screenWidth() - windowContainer.statusBar.getHeight()) {
//            windowParams.x = (int) (scaleX);
//        }
//        Util.log(windowParams.y + " " + windowParams.x);
//        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(windowContainer.window, windowParams);
//        WindowManager.LayoutParams resizeParams = (WindowManager.LayoutParams) windowContainer.resizeButton.getLayoutParams();
//        resizeParams.y = windowParams.y - resizeParams.height;
//        resizeParams.x = windowParams.x + windowContainer.size;
//        windowContainer.moveResizeArea();

    }
    public class WindowContainer {
        public RelativeLayout window, innerWindow,content,statusBar, resizeAreaContainer;
        public ImageView exitButton, minimizeButton, resizeButton, resizeArea, icon;
        public int size, defaultSize;
        public float minimizedWidth, minimizedHeight, minimizedX, minimizedY;
        public boolean touchEnabled = true, iconTouchEnabled = false;

        public WindowContainer() {
            if (Util.screenHeight() > Util.screenWidth()) {
                size =  (Util.screenWidth() / 10) * 7;
                defaultSize =  (Util.screenWidth() / 10) * 7;

            }else{
                defaultSize =  (Util.screenHeight() / 10) * 7;
                size = (Util.screenHeight() / 10) * 7;
            }
            icon = new ImageView(c);
            Util.setImageDrawable(icon, R.drawable.window_icon);
            final WindowManager.LayoutParams iconParams = new WindowManager.LayoutParams(SettingsUtil.getFloaterSize(),
                    SettingsUtil.getFloaterSize(), WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            int[] l = new int[2];
            SystemOverlay.floater.container.getLocationOnScreen(l);
            iconParams.gravity = Gravity.TOP | Gravity.LEFT;
            iconParams.x = 0;
            iconParams.y = 0;
            ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).addView(icon, iconParams);
            icon.setVisibility(View.INVISIBLE);
            icon.setOnTouchListener(new View.OnTouchListener() {
                boolean moveActivated = false;
                float yOffset, xOffset;
                Rect hitbox;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (iconTouchEnabled) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            int[] l = new int[2];
                            v.getLocationOnScreen(l);
                            hitbox = new Rect(l[0] + (v.getWidth() / 4), l[1] + (v.getHeight() / 4), l[0] + ((v.getWidth() / 4) * 3), l[1] + ((v.getHeight() / 4) * 3));
                        }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            if (moveActivated) {
                                WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
                                WindowManager.LayoutParams iconParams = (WindowManager.LayoutParams) icon.getLayoutParams();
                                iconParams.y = (int) (event.getRawY() - yOffset);
                                iconParams.x = (int) (event.getRawX() - xOffset);
                                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(icon, iconParams);
                                windowParams.x = iconParams.x;
                                windowParams.y = iconParams.y;
                                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);

                            }else {
                                int[] l = new int[2];
                                v.getLocationOnScreen(l);
                                if (!hitbox.contains((int) event.getRawX(), (int) event.getRawY())) {
                                    moveActivated = true;
                                    yOffset = event.getRawY() - l[1];
                                    xOffset = event.getRawX() - l[0];
                                }
                            }
                        }else if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (!moveActivated) {
                                int[] l = new int[2];
                                v.getLocationOnScreen(l);
                                if (hitbox.contains((int) event.getRawX(), (int) event.getRawY())) {
                                    setMinimized(false);
                                }
                            }
                            moveActivated = false;
                        }else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                            moveActivated = false;
                        }
                    }
                    return true;
                }
            });
        }

        public void configure() {
            configure((Util.screenWidth() / 2) - (size / 2),(Util.screenHeight() / 2) - (size / 2));
        }

        public void setMinimized(boolean minimized) {
            if (minimized) {
                touchEnabled = false;
                WindowManager.LayoutParams iconParams = (WindowManager.LayoutParams) icon.getLayoutParams();
                final WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
                if (iconParams.x == 0 && iconParams.y == 0) {
                    iconParams.y = windowParams.y;
                    iconParams.x = windowParams.x + window.getWidth() - iconParams.width;
                    ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(icon, iconParams);
                }
                resizeButton.setVisibility(View.INVISIBLE);

                final float distanceX;
                final float distanceY;
                final float shrinkSizeX;
                final float shrinkSizeY;

                if (iconParams.x - windowParams.x == 0) {
                    distanceX = 1;
                }else{
                    distanceX = (iconParams.x - windowParams.x) / 10;
                }

                if (iconParams.y - windowParams.y == 0) {
                    distanceY = 1;
                }else{
                    distanceY = (iconParams.y - windowParams.y) / 10;
                }
                if (windowParams.width - iconParams.width == 0) {
                    shrinkSizeX = 1;
                }else{
                    shrinkSizeX = (windowParams.width - iconParams.width) / 10;
                }
                if (windowParams.height - iconParams.height == 0) {
                    shrinkSizeY = 1;
                }else{
                    shrinkSizeY = (windowParams.height - iconParams.height) / 10;
                }

                minimizedWidth = windowParams.width;
                minimizedHeight = windowParams.height;
                minimizedX = windowParams.x;
                minimizedY = windowParams.y;


                final Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        count++;
                        window.setAlpha(window.getAlpha() - 0.1f);
                        RelativeLayout.LayoutParams innerWindowParams = (RelativeLayout.LayoutParams) innerWindow.getLayoutParams();
                        innerWindowParams.width = (int)(innerWindowParams.width - shrinkSizeX);
                        innerWindowParams.height = (int)(innerWindowParams.height - shrinkSizeY);
                        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
                        windowParams.x = (int)(windowParams.x + distanceX);
                        windowParams.y = (int)(windowParams.y + distanceY);
                        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
                        window.updateViewLayout(innerWindow, innerWindowParams);

                        if (count == 10) {
                            window.setVisibility(View.INVISIBLE);
                            icon.setVisibility(View.VISIBLE);
                            iconTouchEnabled = true;
                        }else{
                            h.postDelayed(this, 5);
                        }
                    }
                },5);
            }else{
                iconTouchEnabled = false;
                window.setVisibility(View.VISIBLE);
                resizeButton.setVisibility(View.VISIBLE);
                icon.setVisibility(View.INVISIBLE);

                final WindowManager.LayoutParams iconParams = (WindowManager.LayoutParams) icon.getLayoutParams();
                final WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();

                final float distanceX;
                final float distanceY;
                final float shrinkSizeX;
                final float shrinkSizeY;

                if (windowParams.x - minimizedX == 0) {
                    distanceX = 1;
                }else{
                    distanceX = (windowParams.x - minimizedX) / 10;
                }

                if (windowParams.y - minimizedY == 0) {
                    distanceY = 1;
                }else{
                    distanceY = (windowParams.y - minimizedY) / 10;
                }
                if (minimizedWidth - iconParams.width == 0) {
                    shrinkSizeX = 1;
                }else{
                    shrinkSizeX = (minimizedWidth - iconParams.width) / 10;
                }
                if (minimizedHeight - iconParams.height == 0) {
                    shrinkSizeY = 1;
                }else{
                    shrinkSizeY = (minimizedHeight - iconParams.height) / 10;
                }

                final Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        count++;
                        window.setAlpha(window.getAlpha() + 0.1f);
                        RelativeLayout.LayoutParams innerWindowParams = (RelativeLayout.LayoutParams) innerWindow.getLayoutParams();
                        innerWindowParams.width = (int)(innerWindowParams.width + shrinkSizeX);
                        innerWindowParams.height = (int)(innerWindowParams.height + shrinkSizeY);
                        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
                        windowParams.x = (int)(windowParams.x - distanceX);
                        windowParams.y = (int)(windowParams.y - distanceY);
                        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
                        window.updateViewLayout(innerWindow, innerWindowParams);

                        if (count == 10) {
                            touchEnabled = true;
                        }else{
                            h.postDelayed(this, 5);
                        }
                    }
                },5);
            }
        }

        public void configure(float x, float y) {
            window = new RelativeLayout(c);
            innerWindow = new RelativeLayout(c);
            statusBar = new RelativeLayout(c);
            content = new RelativeLayout(c);
            exitButton = new ImageView(c);
            minimizeButton = new ImageView(c);
            resizeButton = new ImageView(c);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(size,
                    size, WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            params.y = (int)y;
            params.x = (int)x;
            params.gravity = Gravity.TOP | Gravity.LEFT;

            window.addView(innerWindow);
            innerWindow.addView(statusBar);
            innerWindow.addView(content);
            statusBar.addView(exitButton);
            statusBar.addView(minimizeButton);
            innerWindow.setPadding(5,5,5,5);
            innerWindow.setBackgroundColor(Color.GRAY);
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(window, params);
            RelativeLayout.LayoutParams innerWindowParams = (RelativeLayout.LayoutParams) innerWindow.getLayoutParams();
            innerWindowParams.width = params.width;
            innerWindowParams.height = params.height;
            window.updateViewLayout(innerWindow, innerWindowParams);

            configureStatusBar();
            configureResizeButton();
            RelativeLayout.LayoutParams contentParams = (RelativeLayout.LayoutParams) content.getLayoutParams();
            contentParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            contentParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            contentParams.addRule(RelativeLayout.BELOW, statusBar.getId());
            content.setBackgroundColor(Color.LTGRAY);
            innerWindow.updateViewLayout(content, contentParams);
            innerWindow.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fade_in));
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastMessage.toast(c, ToastMessage.HOWDY);
                }
            });
        }
        public void configureResizeButton() {
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(defaultSize / 10,
                    defaultSize / 10, WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.y = windowParams.y - params.height;
            params.x = windowParams.x + size;
            Util.setImageDrawable(resizeButton, R.drawable.window_resize_icon);
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(resizeButton, params);

            resizeArea = new ImageView(c);
            resizeAreaContainer = new RelativeLayout(c);
            final WindowManager.LayoutParams containerParams = new WindowManager.LayoutParams(Util.screenWidth(),
                    Util.screenHeight(), WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
            containerParams.y = windowParams.y + size - containerParams.height;
            containerParams.x = windowParams.x;
            containerParams.gravity = Gravity.TOP | Gravity.LEFT;
            resizeAreaContainer.addView(resizeArea);
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(resizeAreaContainer, containerParams);
            RelativeLayout.LayoutParams areaParams = (RelativeLayout.LayoutParams) resizeArea.getLayoutParams();
            areaParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            areaParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            areaParams.width = size;
            areaParams.height = size;
            resizeAreaContainer.updateViewLayout(resizeArea, areaParams);
            Util.setImageDrawable(resizeArea, R.drawable.window_resize_area_icon);
            resizeAreaContainer.setVisibility(View.INVISIBLE);

            resizeButton.setOnTouchListener(resize());
        }
        public void configureStatusBar() {
            final RelativeLayout.LayoutParams statusBarParams = (RelativeLayout.LayoutParams) statusBar.getLayoutParams();
            statusBarParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            statusBarParams.height = size / 8;
            statusBarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            statusBar.setPadding(5,5,5,5);
            innerWindow.updateViewLayout(statusBar, statusBarParams);
            statusBar.setBackgroundColor(Color.WHITE);
            Util.generateViewId(statusBar);
            statusBar.setOnTouchListener(new View.OnTouchListener() {
                float yOffset, xOffset;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (touchEnabled) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            int[] l = new int[2];
                            statusBar.getLocationOnScreen(l);
                            yOffset = event.getRawY() - l[1];
                            xOffset = event.getRawX() - l[0];
                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
                            if (event.getRawY() - yOffset > statusBarParams.height && event.getRawY() - yOffset < Util.screenHeight() - statusBarParams.height) {
                                params.y = (int) (event.getRawY() - yOffset);
                            }
                            if (event.getRawX() - xOffset + params.width > statusBarParams.height && event.getRawX() - xOffset < Util.screenWidth() - statusBarParams.height) {
                                params.x = (int) (event.getRawX() - xOffset);
                            }
                            Util.log(params.y + " " + params.x);
                            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, params);
                            WindowManager.LayoutParams resizeParams = (WindowManager.LayoutParams) resizeButton.getLayoutParams();
                            resizeParams.y = params.y - resizeParams.height;
                            resizeParams.x = params.x + size;
                            moveResizeArea();
                            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(resizeButton, resizeParams);
                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            moveResizeArea();
                        }
                    }
                    return true;
                }
            });

            RelativeLayout.LayoutParams exitButtonParams = (RelativeLayout.LayoutParams) exitButton.getLayoutParams();
            exitButtonParams.width = statusBarParams.height;
            exitButtonParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            exitButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            Util.setImageDrawable(exitButton, R.drawable.window_close_icon);
            statusBar.updateViewLayout(exitButton, exitButtonParams);
            exitButton.setOnTouchListener(windowButton(new Runnable() {
                @Override
                public void run() {
                    remove();
                }
            }));
            Util.generateViewId(exitButton);

            RelativeLayout.LayoutParams minimizeButtonParams = (RelativeLayout.LayoutParams) minimizeButton.getLayoutParams();
            minimizeButtonParams.width = statusBarParams.height;
            minimizeButtonParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            minimizeButtonParams.addRule(RelativeLayout.LEFT_OF, exitButton.getId());
            Util.setImageDrawable(minimizeButton, R.drawable.window_minimize_icon);
            statusBar.updateViewLayout(minimizeButton, minimizeButtonParams);
            minimizeButton.setOnTouchListener(windowButton(new Runnable() {
                @Override
                public void run() {
                    setMinimized(true);
                }
            }));

        }
        public View.OnTouchListener resize() {

            return new View.OnTouchListener() {
                int y;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (touchEnabled) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            resizeAreaContainer.setVisibility(View.VISIBLE);
                            int[] l = new int[2];
                            resizeArea.getLocationOnScreen(l);
                            y = l[1] + resizeArea.getHeight();

                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            WindowManager.LayoutParams containerParams = (WindowManager.LayoutParams) resizeAreaContainer.getLayoutParams();
                            RelativeLayout.LayoutParams areaParams = (RelativeLayout.LayoutParams) resizeArea.getLayoutParams();
                            float y = event.getRawY();

                            int[] l = new int[2];
                            resizeArea.getLocationOnScreen(l);

                            if (this.y - y <= defaultSize * 1.25 && this.y - y >= defaultSize / 2) {
                                areaParams.height = (int) (this.y - y);
                                areaParams.width = areaParams.height;
                                resizeAreaContainer.updateViewLayout(resizeArea, areaParams);
                            } else {
                                if (this.y - y > defaultSize * 1.25) {
                                    areaParams.height = (int) (defaultSize * 1.25);
                                    areaParams.width = areaParams.height;
                                    resizeAreaContainer.updateViewLayout(resizeArea, areaParams);
                                } else if (this.y - y < defaultSize / 2) {
                                    areaParams.height = (int) (defaultSize / 2);
                                    areaParams.width = areaParams.height;
                                    resizeAreaContainer.updateViewLayout(resizeArea, areaParams);
                                }
                            }

                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            size = resizeArea.getWidth();


                            final View removeWindow = windowContainer.window;

                            Animation a = AnimationUtils.loadAnimation(c, R.anim.fade_out);
                            a.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).removeView(removeWindow);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            innerWindow.startAnimation(a);
                            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).removeView(windowContainer.resizeButton);
                            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).removeView(resizeAreaContainer);

                            int[] l = new int[2];
                            resizeArea.getLocationOnScreen(l);
                            float x = l[0];
                            float y = l[1];

                            if (l[0] + resizeArea.getWidth() < statusBar.getLayoutParams().height) {
                                x = statusBar.getLayoutParams().height - resizeArea.getWidth();
                            }
                            if (l[1] > Util.screenHeight() - statusBar.getLayoutParams().height) {
                                y = Util.screenHeight() - statusBar.getLayoutParams().height;
                            }
                            configure(x, y);
                        }
                    }
                    return true;
                }
            };
        }
        public void moveResizeArea() {
            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
            WindowManager.LayoutParams containerParams = (WindowManager.LayoutParams) resizeAreaContainer.getLayoutParams();
            containerParams.y = windowParams.y + window.getHeight() - containerParams.height;
            containerParams.x = windowParams.x;
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(resizeAreaContainer, containerParams);
        }
        public View.OnTouchListener windowButton(final Runnable r) {
            return new View.OnTouchListener() {
                boolean statusBarActivated = false;
                boolean timesUp = false;
                float yOffset, xOffset;
                Runnable timer = new Runnable() {
                    @Override
                    public void run() {
                        statusBarActivated = false;
                        timesUp = true;
                    }
                };

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (touchEnabled) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            v.postDelayed(timer, 1000);
                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            if (statusBarActivated) {
                                WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
                                if (event.getRawY() - yOffset > statusBar.getLayoutParams().height && event.getRawY() - yOffset < Util.screenHeight() - statusBar.getLayoutParams().height) {
                                    params.y = (int) (event.getRawY() - yOffset);
                                }
                                if (event.getRawX() - xOffset + params.width > statusBar.getLayoutParams().height && event.getRawX() - xOffset < Util.screenWidth() - statusBar.getLayoutParams().height) {
                                    params.x = (int) (event.getRawX() - xOffset);
                                }
                                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, params);
                                WindowManager.LayoutParams resizeParams = (WindowManager.LayoutParams) resizeButton.getLayoutParams();
                                resizeParams.y = params.y - resizeParams.height;
                                resizeParams.x = params.x + size;
                                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(resizeButton, resizeParams);

                            } else if (!timesUp) {
                                int[] l = new int[2];
                                v.getLocationOnScreen(l);
                                Rect viewBounds = new Rect(l[0], l[1], l[0] + v.getWidth(), l[1] + v.getHeight());
                                if (!viewBounds.contains((int) event.getRawX(), (int) event.getRawY())) {
                                    statusBarActivated = true;
                                    v.removeCallbacks(timer);
                                    int[] loc = new int[2];
                                    statusBar.getLocationOnScreen(loc);
                                    yOffset = event.getRawY() - loc[1];
                                    xOffset = event.getRawX() - loc[0];
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (statusBarActivated) {
                                moveResizeArea();
                            } else {
                                int[] l = new int[2];
                                v.getLocationOnScreen(l);
                                Rect viewBounds = new Rect(l[0], l[1], l[0] + v.getWidth(), l[1] + v.getHeight());
                                if (viewBounds.contains((int) event.getRawX(), (int) event.getRawY())) {
                                    r.run();
                                }
                            }
                            statusBarActivated = false;
                            timesUp = false;
                            v.removeCallbacks(timer);
                        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                            if (statusBarActivated) {
                                moveResizeArea();
                            }
                            statusBarActivated = false;
                            timesUp = false;
                            v.removeCallbacks(timer);
                        }
                    }
                    return true;
                }
            };
        }
    }
}
