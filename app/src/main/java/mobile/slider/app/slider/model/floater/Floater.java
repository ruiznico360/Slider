package mobile.slider.app.slider.model.floater;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.content.SView.SView;
import mobile.slider.app.slider.content.SView.SWindowLayout;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.FloaterIcon;
import mobile.slider.app.slider.settings.resources.FloaterUpdate;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.ui.UI;
import mobile.slider.app.slider.util.ToastMessage;
import mobile.slider.app.slider.util.Util;

public class Floater extends SView {
    public FloaterController floaterMovement;
    public int currentOrientation;

    public Floater(ImageView overlayFloater, RelativeLayout container, int visibility) {
        super(overlayFloater, new SWindowLayout(container));
        container.setVisibility(visibility);

        this.floaterMovement = new FloaterController(SystemOverlay.service);
        currentOrientation = SystemOverlay.service.getResources().getConfiguration().orientation;
    }

    public static int floaterPos() {
        double floaterPos;
        double height = Util.screenHeight();
        int border = SystemOverlay.getOverlayBorder();
        floaterPos = SettingsUtil.getFloaterPos() * height;

        if (floaterPos < border) {
            floaterPos = border;
        }else if ((floaterPos + SettingsUtil.getFloaterSize()) > height - border) {
            floaterPos = height - border - (SettingsUtil.getFloaterSize());
        }
        return (int)floaterPos;
    }

    public static void createFloater(int visibility) {
        if (SystemOverlay.floater != null) {
            SystemOverlay.floater.container.remove();
        }

        final ImageView floater = new AppCompatImageView(SystemOverlay.service);
        final RelativeLayout container = new RelativeLayout(SystemOverlay.service);
        SystemOverlay.floater = new Floater(floater, container, visibility);

        int floaterPos = floaterPos();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                SettingsUtil.getFloaterSize(), WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

        params.y = floaterPos;

        int innerG = 0;
        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
            params.gravity = Gravity.RIGHT | Gravity.TOP;
            innerG = RelativeLayout.ALIGN_PARENT_RIGHT;
        }else if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            params.gravity = Gravity.LEFT | Gravity.TOP;
            innerG = RelativeLayout.ALIGN_PARENT_LEFT;
            floater.setScaleX(-1);
        }
        int width = 0;
        if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.DOTS)) {
            width = ((SettingsUtil.getFloaterSize() / (200 / 50)));
            params.width =  (SettingsUtil.getFloaterSize()) / (100 / 50);
            Util.setImageDrawable(floater, R.drawable.floater_dots);
            floater.setAlpha(0.85f);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.TRANSLUCENT)) {
            width = ((SettingsUtil.getFloaterSize() / (515 / 50)));
            Util.setImageDrawable(floater, R.drawable.floater_translucent);
            floater.setAlpha(0.7f);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.INVISIBLE)) {
            width = (SettingsUtil.getFloaterSize() / 5);
        }

        SystemOverlay.floater.container.plot(params);
        SystemOverlay.floater.plot();

        SView.Layout fEdit = SystemOverlay.floater.openLayout();
        fEdit.setWidth(width);
        fEdit.setHeight(SettingsUtil.getFloaterSize() - (SettingsUtil.getFloaterSize() / 5));
        fEdit.addRule(RelativeLayout.CENTER_VERTICAL);
        fEdit.addRule(innerG);
        fEdit.save();

        if (visibility == View.VISIBLE) {
            SystemOverlay.floater.showFloater();
        }else{
            SystemOverlay.floater.hideFloater();
        }

    }

    public void setVisibility(int visibility) {
        container.layout.setVisibility(visibility);
    }
    public int getVisibility() {
        return container.layout.getVisibility();
    }
    public class FloaterController {
        public Handler longPressListener;
        public Runnable longPressRunnable;
        public boolean floaterRelocate = false, currentlyInTouch = false, touchEnabled = true;
        public float initialX,initialTouchX, initialTouchY, yOffset = 0, originalY;
        public int xValueMultiplier;
        public Context c;
        public View.OnTouchListener touchListener;
        public Garbage garbage;
        public String originalLastFloaterUpdate, originalGravity;

        public FloaterController(Context c) {
            this.c = c;

            touchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, final MotionEvent event) {
                    if (touchEnabled) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            down(event);
                        } else if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && currentlyInTouch) {
                            up(event, false);
                        } else if ((event.getAction() == MotionEvent.ACTION_MOVE) && currentlyInTouch) {
                            move(event);
                        }
                    }
                    return true;
                }
            };
            container.layout.setOnTouchListener(touchListener);
        }
        public void enableTouch(boolean enable) {
            this.touchEnabled = enable;
        }
        public void down(final MotionEvent event) {
            currentlyInTouch = true;
            if (floaterRelocate) {
                floaterRelocate = false;
            }
            longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    Vibrator vib = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(25);
                    floaterRelocate = true;
                    yOffset = event.getRawY() - container.y;
                    initialX = container.x;
                    if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        xValueMultiplier = -1;
                    }else {
                        xValueMultiplier = 1;
                    }
                    originalGravity = SettingsUtil.getFloaterGravity();
                    originalY = SettingsUtil.getFloaterPos();
                    originalLastFloaterUpdate = SettingsUtil.getLastFloaterUpdate();

                    garbage = new Garbage(new RelativeLayout(c), new RelativeLayout(c), new ImageView(c));
                    garbage.plot();
                }
            };
            initialTouchY = event.getRawY();
            initialTouchX = event.getRawX();

            longPressListener = new Handler();
            longPressListener.postDelayed(longPressRunnable, 500);
            Vibrator vib = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(25);
        }
        public void forceUp() {
            up(null, true);
        }
        public void up(final MotionEvent event, boolean force) {
            currentlyInTouch = false;
            longPressListener.removeCallbacks(longPressRunnable);
            if (floaterRelocate) {
                if (garbage.trash.height() == SettingsUtil.getFloaterSize()) {
                    hideFloater();
                    SettingsUtil.setLastFloaterUpdate(originalLastFloaterUpdate);
                    SettingsUtil.setFloaterPos(originalY);
                    SettingsUtil.setFloaterGravity(originalGravity);
                    Floater.createFloater(View.INVISIBLE);
                    ToastMessage.toast(c, ToastMessage.HIDING_FLOATER);
                }else{
                    Floater.createFloater(Floater.this.getVisibility());
                }
                garbage.container.remove();
            }else{
                if (!force) {
                    float fX = event.getRawX();
                    float fY = event.getRawY();
                    int w = container.width / 2;
                    int h = container.height / 2;

                    if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        if (fX - initialTouchX >= w && Math.abs(fY - initialTouchY) <= h) {
                            UI.launchUI();
                        }
                    }else{
                        if (initialTouchX - fX >= w && Math.abs(fY - initialTouchY) <= h) {
                            UI.launchUI();
                        }
                    }
                }
            }
            floaterRelocate = false;
        }

        public void move(final MotionEvent event) {
            if (floaterRelocate) {
                double width = Util.screenWidth() / 2;
                int height = Util.screenHeight();
                int border = SystemOverlay.getOverlayBorder();
                SWindowLayout.Layout editor = container.openLayout();

                float rawY = event.getRawY() - yOffset;
                if (rawY + container.height > (height - (border))) {
                    rawY = ((height - (border)) - container.height);
                }
                else if (rawY < border) {
                    rawY = ((border));
                }
                editor.setX(xValueMultiplier * (initialX + (int) (event.getRawX() - initialTouchX)));
                editor.setY(rawY);
                SettingsUtil.setFloaterPos((rawY) / (height));

                if (c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.LANDSCAPE);
                } else {
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.PORTRAIT);
                }
                if (event.getRawX() < width) {
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        Floater.this.view.setScaleX(-1);
                        SettingsUtil.setFloaterGravity(WindowGravity.LEFT);
                    }
                } else if (event.getRawX() >= width) {
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        Floater.this.view.setScaleX(1);
                        SettingsUtil.setFloaterGravity(WindowGravity.RIGHT);
                    }
                }
                editor.save();

                Rect cRect = new Rect(Floater.this.x(), Floater.this.y(), Floater.this.x() + Floater.this.width(), Floater.this.y() + Floater.this.height());

                if (cRect.intersects(garbage.trash.x(), garbage.trash.y(), garbage.trash.x() + garbage.trash.width(), garbage.trash.y() + garbage.trash.height())) {
                    if (garbage.trash.height() != SettingsUtil.getFloaterSize()) {
                        SView.Layout tEdit = garbage.trash.openLayout();
                        tEdit.setHeight(SettingsUtil.getFloaterSize());
                        tEdit.setWidth(SettingsUtil.getFloaterSize());
                        tEdit.save();
                    }
                }else{
                    if (garbage.trash.height() != SettingsUtil.getFloaterSize() / 1.3) {
                        SView.Layout tEdit = garbage.trash.openLayout();
                        tEdit.setHeight(SettingsUtil.getFloaterSize() / 1.3f);
                        tEdit.setWidth(SettingsUtil.getFloaterSize() / 1.3f);
                        tEdit.save();
                    }
                }
            }else{
                int rx = (int)event.getRawX();
                int ry = (int)event.getRawY();
                int x = container.x;
                int y = container.y;
                int w = container.width * 2;
                int h = container.height;
                if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                   x = Util.screenWidth() - w;
                   w = Util.screenWidth();
                }
                if (rx < x || rx > x + w || ry < y || ry > y + h) {
                    longPressListener.removeCallbacks(longPressRunnable);
                }
            }
        }
    }
    public void hideFloater() {
        floaterMovement.enableTouch(false);
        Animation a = AnimationUtils.loadAnimation(SystemOverlay.service.getApplicationContext(), R.anim.fade_out);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(a);
    }
    public void showFloater() {
        setVisibility(View.VISIBLE);
        Animation a = AnimationUtils.loadAnimation(SystemOverlay.service.getApplicationContext(), R.anim.fade_in);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                floaterMovement.enableTouch(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(a);
    }
    public void updateFloater() {
        SWindowLayout.Layout editor = container.openLayout();
        editor.setY(floaterPos());
        editor.save();
    }
    public class Garbage extends SView{
        public SView trash;
        public Garbage(RelativeLayout background, RelativeLayout gradient, ImageView trash) {
            super(gradient, new SWindowLayout(background));
            this.trash = new SView(trash, new SWindowLayout(gradient));
        }
        public void plot() {
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                    SettingsUtil.getFloaterSize() * 2, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.BOTTOM;
            container.plot(params);
            super.plot();
            trash.plot();

            ShapeDrawable d = new ShapeDrawable(new RectShape());
            d.getPaint().setShader(new LinearGradient(0,0,0,params.height, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.REPEAT));
            ((ImageView)trash.view).setScaleType(ImageView.ScaleType.FIT_XY);
            ((ImageView)trash.view).setAdjustViewBounds(true);
            Util.setBackground(view, d);
            Util.setImageDrawable(((ImageView)trash.view), R.drawable.garbage);

            Layout editor = openLayout();
            editor.setWidth(params.width);
            editor.setHeight(params.height);
            editor.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            editor.save();

            editor = trash.openLayout();
            editor.setHeight(SettingsUtil.getFloaterSize() / 1.3f);
            editor.setWidth(SettingsUtil.getFloaterSize() / 1.3f);
            editor.addRule(RelativeLayout.CENTER_IN_PARENT);
            editor.save();

            view.startAnimation(AnimationUtils.loadAnimation(SystemOverlay.service, R.anim.fade_in));
        }
    }
}
