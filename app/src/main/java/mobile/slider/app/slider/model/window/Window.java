package mobile.slider.app.slider.model.window;


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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.util.ToastMessage;
import mobile.slider.app.slider.util.Util;

public class Window {
    public Context c;
    public WindowContainer windowContainer;

    public Window(Context c) {
        this.c = c;
        windowContainer = new WindowContainer();

    }
    public void create() {
        windowContainer.configure();
    }
    public void remove() {
        ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).removeView(windowContainer.window);
    }
    public class WindowContainer {
        public RelativeLayout window;
        public RelativeLayout statusBar;
        public RelativeLayout content;
        public ImageView exitButton;
        public int size;

        public WindowContainer() {
            window = new RelativeLayout(c);
            statusBar = new RelativeLayout(c);
            content = new RelativeLayout(c);
            exitButton = new ImageView(c);
        }

        public void configure() {
            if (Util.screenHeight() > Util.screenWidth()) {
                size =  (Util.screenWidth() / 10) * 7;
            }else{
                size = (Util.screenHeight() / 10) * 7;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(size,
                    size, WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            params.y = (Util.screenHeight() / 2) - (size / 2);
            params.x = (Util.screenWidth() / 2) - (size / 2);
            params.gravity = Gravity.TOP | Gravity.LEFT;

            window.addView(statusBar);
            window.addView(content);
            statusBar.addView(exitButton);
            window.setPadding(5,5,5,5);
            window.setBackgroundColor(Color.GRAY);
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(window, params);

            configureStatusBar();
            RelativeLayout.LayoutParams contentParams = (RelativeLayout.LayoutParams) content.getLayoutParams();
            contentParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            contentParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            contentParams.addRule(RelativeLayout.BELOW, statusBar.getId());
            content.setBackgroundColor(Color.WHITE);
            window.updateViewLayout(content, contentParams);
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastMessage.toast(c, ToastMessage.HOWDY);
                }
            });
        }
        public void configureStatusBar() {
            final RelativeLayout.LayoutParams statusBarParams = (RelativeLayout.LayoutParams) statusBar.getLayoutParams();
            statusBarParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            statusBarParams.height = size / 8;
            statusBarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            statusBar.setPadding(5,5,5,5);
            window.updateViewLayout(statusBar, statusBarParams);
            statusBar.setBackgroundColor(Color.GRAY);
            Util.generateViewId(statusBar);
            statusBar.setOnTouchListener(new View.OnTouchListener() {
                float yOffset, xOffset;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        int[] l = new int[2];
                        statusBar.getLocationOnScreen(l);
                        yOffset = event.getRawY() - l[1];
                        xOffset = event.getRawX() - l[0];
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
                        if (event.getRawY() - yOffset > statusBarParams.height && event.getRawY() - yOffset < Util.screenHeight() - statusBarParams.height) {
                            params.y = (int) (event.getRawY() - yOffset);
                        }
                        if (event.getRawX() - xOffset + params.width > statusBarParams.height && event.getRawX() - xOffset < Util.screenWidth() - statusBarParams.height) {
                            params.x = (int)(event.getRawX() - xOffset);
                        }
                        ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, params);
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
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.postDelayed(timer, 1000);
                    }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (statusBarActivated) {
                            WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
                            if (event.getRawY() - yOffset > statusBar.getLayoutParams().height && event.getRawY() - yOffset < Util.screenHeight() - statusBar.getLayoutParams().height) {
                                params.y = (int) (event.getRawY() - yOffset);
                            }
                            if (event.getRawX() - xOffset + params.width > statusBar.getLayoutParams().height && event.getRawX() - xOffset < Util.screenWidth() - statusBar.getLayoutParams().height) {
                                params.x = (int) (event.getRawX() - xOffset);
                            }
                            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(window, params);
                        }else if (!timesUp){
                            int[] l = new int[2];
                            v.getLocationOnScreen(l);
                            Rect viewBounds = new Rect(l[0], l[1], l[0] + v.getWidth(), l[1] + v.getHeight());
                            if (!viewBounds.contains((int)event.getRawX(), (int)event.getRawY())) {
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
                            if (viewBounds.contains((int)event.getRawX(), (int)event.getRawY())) {
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
                    return true;
                }
            };
        }
    }
}
