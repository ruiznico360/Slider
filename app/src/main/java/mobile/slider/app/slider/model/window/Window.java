package mobile.slider.app.slider.model.window;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.util.ImageUtil;
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
        ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).removeView(windowContainer.minimizedIcon);

        openWindows.remove(this);
    }

    public class WindowContainer {
        public RelativeLayout window, innerWindow,content,statusBar, resizeAreaInnerContainer;
        public ImageView exitButton, minimizeButton, resizeButton, resizeArea, minimizedIcon;
        public int windowSize, defaultSize;
        public float minimizedX, minimizedY;
        public boolean touchEnabled = true, minimizeIconTouchEnabled = false;
        public Listener listener;

        public WindowContainer() {
            if (Util.screenHeight() > Util.screenWidth()) {
                windowSize =  (Util.screenWidth() / 10) * 7;
                defaultSize =  (Util.screenWidth() / 10) * 7;

            }else{
                defaultSize =  (Util.screenHeight() / 10) * 7;
                windowSize = (Util.screenHeight() / 10) * 7;
            }
        }

        public void configurationChange() {
            float oldHeight = Util.screenWidth();
            float oldWidth = Util.screenHeight();

            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();

            float statusBarHeight = statusBar.getHeight();
            float horPosition;
            float verPosition;
            if (minimizedIcon.getVisibility() == View.VISIBLE) {
                horPosition = minimizedX + (window.getWidth() / 2);
                verPosition = minimizedY + (window.getHeight() / 2);
            }else {
                horPosition = windowParams.x + (window.getWidth() / 2);
                verPosition = windowParams.y + (window.getHeight() / 2);
            }
            float newX;
            float newY;
            if (horPosition <= 0) {
                newX = -window.getWidth() + statusBarHeight;
            }else if (horPosition > oldWidth)  {
                newX = Util.screenWidth() - statusBarHeight;
            }else{
                newX = ((horPosition / oldWidth) * Util.screenWidth()) - (window.getWidth() / 2);
            }
            if (verPosition > oldHeight)  {
                newY = Util.screenHeight() - statusBarHeight;
            }else{
                newY = ((verPosition / oldHeight) * Util.screenHeight()) - (window.getHeight() / 2);
                if (newY < statusBarHeight) {
                    newY = statusBarHeight;
                }
            }
            minimizedX = (int) newX;
            minimizedY = (int) newY;
            windowParams.x = (int) newX;
            windowParams.y = (int) newY;
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);

            WindowManager.LayoutParams resizeParams = (WindowManager.LayoutParams) resizeButton.getLayoutParams();
            resizeParams.y = windowParams.y - resizeParams.height;
            resizeParams.x = windowParams.x + windowSize;
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(resizeButton, resizeParams);

            WindowManager.LayoutParams minimizeParams = (WindowManager.LayoutParams) minimizedIcon.getLayoutParams();
            float newIconX = ((minimizeParams.x / oldWidth) * Util.screenWidth());
            float newIconY = ((minimizeParams.y / oldHeight) * Util.screenHeight());

            if (newIconX < 0) {
                newIconX = 0;
            }else if (newIconX + minimizedIcon.getWidth() > Util.screenWidth()) {
                newIconX = Util.screenWidth() - minimizedIcon.getWidth();
            }

            if (newIconY < 0) {
                newIconY = 0;
            }else if (newIconY + minimizedIcon.getHeight() > Util.screenHeight()) {
                newIconY = Util.screenHeight() - minimizedIcon.getHeight();
            }
            minimizeParams.x = (int)newIconX;
            minimizeParams.y = (int)newIconY;
            ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(minimizedIcon, minimizeParams);
        }

        public void configure() {
            configure((Util.screenWidth() / 2) - (windowSize / 2),(Util.screenHeight() / 2) - (windowSize / 2));
        }

        public void setMinimized(boolean minimized) {
            if (minimized) {
                touchEnabled = false;
                WindowManager.LayoutParams minimizedIconParams = (WindowManager.LayoutParams) minimizedIcon.getLayoutParams();
                final WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
                if (minimizedIconParams.x == 0 && minimizedIconParams.y == 0) {
                    float newIconX = windowParams.x + window.getWidth() - minimizedIconParams.width;
                    float newIconY = windowParams.y;

                    if (newIconX < 0) {
                        newIconX = 0;
                    }else if (newIconX + minimizedIcon.getWidth() > Util.screenWidth()) {
                        newIconX = Util.screenWidth() - minimizedIcon.getWidth();
                    }

                    if (newIconY < 0) {
                        newIconY = 0;
                    }else if (newIconY + minimizedIcon.getHeight() > Util.screenHeight()) {
                        newIconY = Util.screenHeight() - minimizedIcon.getHeight();
                    }
                    minimizedIconParams.x = (int)newIconX;
                    minimizedIconParams.y = (int)newIconY;
                    ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(minimizedIcon, minimizedIconParams);
                }
                resizeButton.setVisibility(View.INVISIBLE);

                final float distanceX;
                final float distanceY;
                final float shrinkSizeX;
                final float shrinkSizeY;
                final float speed = 5;

                AnimationSet a = new AnimationSet(true);
//                a.addAnimation(AnimationUtils.loadAnimation(c, R.anim.shrink));
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        window.setVisibility(View.INVISIBLE);
                        minimizedIcon.setVisibility(View.VISIBLE);
                        minimizeIconTouchEnabled = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                innerWindow.startAnimation(a);
                minimizedX = windowParams.x;
                minimizedY = windowParams.y;

//                if (minimizedIconParams.x - windowParams.x == 0) {
//                    distanceX = 1;
//                }else{
//                    distanceX = (minimizedIconParams.x - windowParams.x) / speed;
//                }
//
//                if (minimizedIconParams.y - windowParams.y == 0) {
//                    distanceY = 1;
//                }else{
//                    distanceY = (minimizedIconParams.y - windowParams.y) / speed;
//                }

//
//                final Handler shrink = new Handler();
//                shrink.postDelayed(new Runnable() {
//                    int count = 0;
//                    @Override
//                    public void run() {
//                        count++;
//                        RelativeLayout.LayoutParams innerWindowParams = (RelativeLayout.LayoutParams) innerWindow.getLayoutParams();
//                        windowParams.x = (int)(windowParams.x + distanceX);
//                        windowParams.y = (int)(windowParams.y + distanceY);
//                        window.setAlpha(window.getAlpha() - (1 / speed));
//
//                        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
//                        window.updateViewLayout(innerWindow, innerWindowParams);
//
//                        if (count == speed) {
//                            window.setVisibility(View.INVISIBLE);
//                            minimizedIcon.setVisibility(View.VISIBLE);
//                            minimizeIconTouchEnabled = true;
//                        }else{
//                            shrink.postDelayed(this, 1);
//                        }
//                    }
//                },1);
            }else{
                minimizeIconTouchEnabled = false;
                window.setVisibility(View.VISIBLE);
                minimizedIcon.setVisibility(View.INVISIBLE);

                final WindowManager.LayoutParams minimizedIconParams = (WindowManager.LayoutParams) minimizedIcon.getLayoutParams();
                final WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();

                final float distanceX;
                final float distanceY;
                final float growSizeX;
                final float growSizeY;
                final float speed = 5;

                windowParams.x = (int) (minimizedX);
                windowParams.y = (int)(minimizedY);
                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);

                AnimationSet a = new AnimationSet(true);
//                a.addAnimation(AnimationUtils.loadAnimation(c, R.anim.grow));
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        touchEnabled = true;
                        resizeButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });



                innerWindow.startAnimation(a);

//                if (windowParams.x - minimizedX == 0) {
//                    distanceX = 1;
//                }else{
//                    distanceX = (windowParams.x - minimizedX) / speed;
//                }
//
//                if (windowParams.y - minimizedY == 0) {
//                    distanceY = 1;
//                }else{
//                    distanceY = (windowParams.y - minimizedY) / speed;
//                }
//
//                final Handler grow = new Handler();
//                grow.postDelayed(new Runnable() {
//                    int count = 0;
//                    @Override
//                    public void run() {
//                        count++;
//                        RelativeLayout.LayoutParams innerWindowParams = (RelativeLayout.LayoutParams) innerWindow.getLayoutParams();
//                        windowParams.x = (int)(windowParams.x - distanceX);
//                        windowParams.y = (int)(windowParams.y - distanceY);
//
//                        window.setAlpha(window.getAlpha() + (1 / speed));
//                        window.updateViewLayout(innerWindow, innerWindowParams);
//                        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
//
//                        if (count == speed) {
//                            touchEnabled = true;
//                            resizeButton.setVisibility(View.VISIBLE);
//                        }else{
//                            grow.postDelayed(this, 1);
//                        }
//                    }
//                },1);
            }
        }

        public void configure(float x, float y) {
            listener = new Listener();

            minimizedIcon = new ImageView(c);
            window = new RelativeLayout(c);
            innerWindow = new RelativeLayout(c);
            statusBar = new RelativeLayout(c);
            content = new RelativeLayout(c);
            exitButton = new ImageView(c);
            minimizeButton = new ImageView(c);
            resizeButton = new ImageView(c);
            resizeArea = new ImageView(c);

            ImageUtil.setImageDrawable(minimizedIcon, R.drawable.window_icon);
            final WindowManager.LayoutParams minimizedIconParams = new WindowManager.LayoutParams(SettingsUtil.getFloaterSize(),
                    SettingsUtil.getFloaterSize(), WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            minimizedIconParams.gravity = Gravity.TOP | Gravity.LEFT;
            minimizedIconParams.x = 0;
            minimizedIconParams.y = 0;
            minimizedIcon.setVisibility(View.INVISIBLE);
            minimizedIcon.setOnTouchListener(listener.minimizedIconListener());
            ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).addView(minimizedIcon, minimizedIconParams);

            final WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(windowSize,
                    windowSize, WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            windowParams.y = (int)y;
            windowParams.x = (int)x;
            windowParams.gravity = Gravity.TOP | Gravity.LEFT;
            window.addView(innerWindow);
            RelativeLayout.LayoutParams innerWindowParams = (RelativeLayout.LayoutParams) innerWindow.getLayoutParams();
            innerWindowParams.width = windowParams.width;
            innerWindowParams.height = windowParams.height;
            window.updateViewLayout(innerWindow, innerWindowParams);

            innerWindow.addView(statusBar);
            innerWindow.addView(content);
            statusBar.addView(exitButton);
            statusBar.addView(minimizeButton);
            innerWindow.setPadding(5,5,5,5);
            innerWindow.setBackgroundColor(Color.GRAY);
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(window, windowParams);

            configureStatusBar();
            configureResizer();

            content.setBackgroundColor(Color.LTGRAY);
            RelativeLayout.LayoutParams contentParams = (RelativeLayout.LayoutParams) content.getLayoutParams();
            contentParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            contentParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            contentParams.addRule(RelativeLayout.BELOW, statusBar.getId());

            innerWindow.updateViewLayout(content, contentParams);
            innerWindow.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fade_in));
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastMessage.toast(c, ToastMessage.HOWDY);
                }
            });
        }
        public void configureResizer() {
            final WindowManager.LayoutParams resizeButtonParams = new WindowManager.LayoutParams(defaultSize / 10,
                    defaultSize / 10, WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
            resizeButtonParams.gravity = Gravity.TOP | Gravity.LEFT;
            resizeButtonParams.y = windowParams.y - resizeButtonParams.height;
            resizeButtonParams.x = windowParams.x + windowParams.width;
            ImageUtil.setImageDrawable(resizeButton, R.drawable.window_resize_icon);
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(resizeButton, resizeButtonParams);

            ImageUtil.setImageDrawable(resizeArea, R.drawable.window_resize_area_icon);
            final WindowManager.LayoutParams resizeAreaParams = new WindowManager.LayoutParams(Util.screenWidth(),
                    Util.screenHeight(), WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            resizeAreaParams.y = windowParams.y + windowSize - resizeAreaParams.height;
            resizeAreaParams.x = windowParams.x;
            resizeAreaParams.gravity = Gravity.TOP | Gravity.LEFT;

            resizeAreaInnerContainer = new RelativeLayout(c);
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(resizeAreaInnerContainer, resizeAreaParams);


            resizeAreaInnerContainer.addView(resizeArea);
            RelativeLayout.LayoutParams areaParams = (RelativeLayout.LayoutParams) resizeArea.getLayoutParams();
            areaParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            areaParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            areaParams.width = windowSize;
            areaParams.height = windowSize;
            resizeAreaInnerContainer.updateViewLayout(resizeArea, areaParams);
            resizeAreaInnerContainer.setVisibility(View.INVISIBLE);

            resizeButton.setOnTouchListener(listener.resize());
        }
        public void configureStatusBar() {
            statusBar.setPadding(5,5,5,5);
            statusBar.setBackgroundColor(Color.WHITE);
            statusBar.setOnTouchListener(listener.statusBar());
            Util.generateViewId(statusBar);

            final RelativeLayout.LayoutParams statusBarParams = (RelativeLayout.LayoutParams) statusBar.getLayoutParams();
            statusBarParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            statusBarParams.height = windowSize / 8;
            statusBarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            innerWindow.updateViewLayout(statusBar, statusBarParams);

            ImageUtil.setImageDrawable(exitButton, R.drawable.window_close_icon);
            Util.generateViewId(exitButton);
            exitButton.setOnTouchListener(listener.windowButton(new Runnable() {
                @Override
                public void run() {
                    remove();
                }
            }));
            RelativeLayout.LayoutParams exitButtonParams = (RelativeLayout.LayoutParams) exitButton.getLayoutParams();
            exitButtonParams.width = statusBarParams.height;
            exitButtonParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            exitButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            statusBar.updateViewLayout(exitButton, exitButtonParams);

            ImageUtil.setImageDrawable(minimizeButton, R.drawable.window_minimize_icon);
            minimizeButton.setOnTouchListener(listener.windowButton(new Runnable() {
                @Override
                public void run() {
                    setMinimized(true);
                }
            }));
            RelativeLayout.LayoutParams minimizeButtonParams = (RelativeLayout.LayoutParams) minimizeButton.getLayoutParams();
            minimizeButtonParams.width = statusBarParams.height;
            minimizeButtonParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            minimizeButtonParams.addRule(RelativeLayout.LEFT_OF, exitButton.getId());
            statusBar.updateViewLayout(minimizeButton, minimizeButtonParams);
        }
        public class Listener {
            public View.OnTouchListener statusBar(){
                return new View.OnTouchListener() {
                    float yOffset, xOffset;
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (touchEnabled) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                int[] l = new int[2];
                                statusBar.getLocationOnScreen(l);
                                yOffset = event.getRawY() - l[1];
                                xOffset = event.getRawX() - l[0];
                            }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
                                if (event.getRawY() - yOffset > statusBar.getHeight() && event.getRawY() - yOffset < Util.screenHeight() - statusBar.getHeight()) {
                                    windowParams.y = (int) (event.getRawY() - yOffset);
                                }
                                if (event.getRawX() - xOffset + windowParams.width > statusBar.getHeight() && event.getRawX() - xOffset < Util.screenWidth() - statusBar.getHeight()) {
                                    windowParams.x = (int) (event.getRawX() - xOffset);
                                }

                                WindowManager.LayoutParams resizeParams = (WindowManager.LayoutParams) resizeButton.getLayoutParams();
                                resizeParams.y = windowParams.y - resizeParams.height;
                                resizeParams.x = windowParams.x + windowSize;

                                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
                                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(resizeButton, resizeParams);
                            }
                        }
                        return true;
                    }
                };
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
                            }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                if (statusBarActivated) {
                                    WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
                                    if (event.getRawY() - yOffset > statusBar.getLayoutParams().height && event.getRawY() - yOffset < Util.screenHeight() - statusBar.getLayoutParams().height) {
                                        windowParams.y = (int) (event.getRawY() - yOffset);
                                    }
                                    if (event.getRawX() - xOffset + windowParams.width > statusBar.getLayoutParams().height && event.getRawX() - xOffset < Util.screenWidth() - statusBar.getLayoutParams().height) {
                                        windowParams.x = (int) (event.getRawX() - xOffset);
                                    }
                                    WindowManager.LayoutParams resizeParams = (WindowManager.LayoutParams) resizeButton.getLayoutParams();
                                    resizeParams.y = windowParams.y - resizeParams.height;
                                    resizeParams.x = windowParams.x + windowSize;

                                    ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);
                                    ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(resizeButton, resizeParams);

                                }else if (!timesUp) {
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
                            }else if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (!statusBarActivated) {
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
                            }else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                                statusBarActivated = false;
                                timesUp = false;
                                v.removeCallbacks(timer);
                            }
                        }
                        return true;
                    }
                };
            }
            public View.OnTouchListener minimizedIconListener() {
                return new View.OnTouchListener() {
                   boolean moveActivated = false;
                   float yOffset, xOffset;
                   Rect hitbox;
                   @Override
                   public boolean onTouch(View v, MotionEvent event) {
                       if (minimizeIconTouchEnabled) {
                           if (event.getAction() == MotionEvent.ACTION_DOWN) {
                               WindowManager.LayoutParams minimizedIconParams = (WindowManager.LayoutParams) minimizedIcon.getLayoutParams();
                               hitbox = new Rect(minimizedIconParams.x + (minimizedIcon.getWidth() / 4), minimizedIconParams.y + (minimizedIcon.getHeight() / 4),minimizedIconParams.x + minimizedIcon.getWidth() - (minimizedIcon.getWidth() / 4), minimizedIconParams.y + minimizedIcon.getHeight() - (minimizedIcon.getHeight() / 4));
                           }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                               if (moveActivated) {
                                   WindowManager.LayoutParams minimizedIconParams = (WindowManager.LayoutParams) minimizedIcon.getLayoutParams();
                                   WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();

                                   float newIconY = (int) (event.getRawY() - yOffset);
                                   float newIconX = (int) (event.getRawX() - xOffset);

                                   if (newIconX < 0) {
                                       newIconX = 0;
                                   }else if (newIconX + minimizedIcon.getWidth() > Util.screenWidth()) {
                                       newIconX = Util.screenWidth() - minimizedIcon.getWidth();
                                   }

                                   if (newIconY < 0) {
                                       newIconY = 0;
                                   }else if (newIconY + minimizedIcon.getHeight() > Util.screenHeight()) {
                                       newIconY = Util.screenHeight() - minimizedIcon.getHeight();
                                   }
                                   minimizedIconParams.x = (int)newIconX;
                                   minimizedIconParams.y = (int)newIconY;
                                   windowParams.x = minimizedIconParams.x;
                                   windowParams.y = minimizedIconParams.y;

                                   ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(minimizedIcon, minimizedIconParams);
                                   ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, windowParams);

                               }else{
                                   if (!hitbox.contains((int) event.getRawX(), (int) event.getRawY())) {
                                       WindowManager.LayoutParams minimizedIconParams = (WindowManager.LayoutParams) minimizedIcon.getLayoutParams();
                                       moveActivated = true;
                                       yOffset = event.getRawY() - minimizedIconParams.y;
                                       xOffset = event.getRawX() - minimizedIconParams.x;
                                   }
                               }
                           }else if (event.getAction() == MotionEvent.ACTION_UP) {
                               if (!moveActivated) {
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
               };
           }
           public View.OnTouchListener resize() {
               return new View.OnTouchListener() {
                   int y;
                   @Override
                   public boolean onTouch(View v, MotionEvent event) {
                       if (touchEnabled) {
                           if (event.getAction() == MotionEvent.ACTION_DOWN) {
                               WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) window.getLayoutParams();
                               WindowManager.LayoutParams resizeAreaInnerContainerParams = (WindowManager.LayoutParams) resizeAreaInnerContainer.getLayoutParams();
                               resizeAreaInnerContainerParams.y = windowParams.y + window.getHeight() - resizeAreaInnerContainerParams.height;
                               resizeAreaInnerContainerParams.x = windowParams.x;
                               ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(resizeAreaInnerContainer, resizeAreaInnerContainerParams);

                               resizeAreaInnerContainer.setVisibility(View.VISIBLE);
                               y = resizeAreaInnerContainerParams.y + resizeAreaInnerContainer.getHeight();

                               Util.log(y + "");
                           }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                               RelativeLayout.LayoutParams resizeAreaParams = (RelativeLayout.LayoutParams) resizeArea.getLayoutParams();
                               float rawY = event.getRawY();

                               if (this.y - rawY <= defaultSize * 1.25 && this.y - rawY >= defaultSize / 2) {
                                   resizeAreaParams.height = (int) (this.y - rawY);
                                   resizeAreaParams.width = resizeAreaParams.height;
                                   resizeAreaInnerContainer.updateViewLayout(resizeArea, resizeAreaParams);
                               }else{
                                   if (this.y - rawY > defaultSize * 1.25) {
                                       resizeAreaParams.height = (int) (defaultSize * 1.25);
                                       resizeAreaParams.width = resizeAreaParams.height;
                                       resizeAreaInnerContainer.updateViewLayout(resizeArea, resizeAreaParams);
                                   }else if (this.y - rawY < defaultSize / 2) {
                                       resizeAreaParams.height = (int) (defaultSize / 2);
                                       resizeAreaParams.width = resizeAreaParams.height;
                                       resizeAreaInnerContainer.updateViewLayout(resizeArea, resizeAreaParams);
                                   }
                               }
                           }else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                               windowSize = resizeArea.getWidth();

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
                               ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).removeView(resizeAreaInnerContainer);

                               int[] l = new int[2];
                               resizeArea.getLocationOnScreen(l);
                               float x = l[0];
                               float y = l[1];

                               if (x + resizeArea.getWidth() < statusBar.getLayoutParams().height) {
                                   x = statusBar.getLayoutParams().height - resizeArea.getWidth();
                               }
                               if (y > Util.screenHeight() - statusBar.getLayoutParams().height) {
                                   y = Util.screenHeight() - statusBar.getLayoutParams().height;
                               }else if (y < statusBar.getLayoutParams().height) {
                                   y = statusBar.getLayoutParams().height;
                               }
                               configure(x, y);
                           }
                       }
                       return true;
                   }
               };
           }
       }
    }
}
