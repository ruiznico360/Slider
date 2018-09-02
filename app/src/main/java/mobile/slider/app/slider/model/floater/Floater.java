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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.SView.SWindowLayout;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.resources.FloaterIcon;
import mobile.slider.app.slider.settings.resources.FloaterUpdate;
import mobile.slider.app.slider.settings.resources.WindowGravity;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Anim;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.ToastMessage;
import mobile.slider.app.slider.util.Util;

public class Floater extends SView {
    public FloaterController floaterMovement;
    public int currentOrientation, currentVisibility;
    public Runnable deviceStateRunnable;

    public Floater(ImageView overlayFloater, RelativeLayout container, int visibility) {
        super(overlayFloater, new SWindowLayout(container));
        currentVisibility = visibility;
        container.setVisibility(visibility);

        this.floaterMovement = new FloaterController(SystemOverlay.service);
        currentOrientation = SystemOverlay.service.getResources().getConfiguration().orientation;

        final boolean phoneStatus = Util.isLocked(SystemOverlay.service);
        deviceStateRunnable = new Runnable() {
            @Override
            public void run() {
                if (phoneStatus != Util.isLocked(SystemOverlay.service.getApplicationContext())) {
                    createFloater(SystemOverlay.floater.getVisibility());
                    if (floaterMovement.garbage != null) {
                        floaterMovement.garbage.container.remove();
                    }
                }
            }
        };
        SystemOverlay.deviceStateListener.tasks.add(deviceStateRunnable);
    }
    public int floaterPosX(int width) {
        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            return 0;
        }else{
            return Util.screenWidth() - width;
        }
    }

    public int floaterPosY() {
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
            if (SystemOverlay.deviceStateListener.tasks.contains(SystemOverlay.floater.deviceStateRunnable)) {
                SystemOverlay.deviceStateListener.tasks.remove(SystemOverlay.floater.deviceStateRunnable);
            }
        }
        Util.log("checkpoint 1");

        final ImageView floater = new AppCompatImageView(SystemOverlay.service);
        final RelativeLayout container = new RelativeLayout(SystemOverlay.service);
        SystemOverlay.floater = new Floater(floater, container, visibility);

        int floaterType;
        if (Util.isLocked(SystemOverlay.service)) {
            floaterType = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }else{
            floaterType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        Util.log("checkpoint 2");
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                SettingsUtil.getFloaterSize(), floaterType,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

        int innerG = 0;
        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
            innerG = RelativeLayout.ALIGN_PARENT_RIGHT;
        }else if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            innerG = RelativeLayout.ALIGN_PARENT_LEFT;
        }
        int width = 0;
        if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.DOTS)) {
            width = ((SettingsUtil.getFloaterSize() / (200 / 50)));
            params.width =  (SettingsUtil.getFloaterSize()) / (100 / 50);
            ImageUtil.setImageDrawable(floater, R.drawable.floater_dots);
            floater.setAlpha(0.85f);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.TRANSLUCENT)) {
            width = ((SettingsUtil.getFloaterSize() / (515 / 50)));
            ImageUtil.setImageDrawable(floater, R.drawable.floater_translucent);
            floater.setAlpha(0.7f);
        }else if (SettingsUtil.getFloaterIcon().equals(FloaterIcon.INVISIBLE)) {
            width = (SettingsUtil.getFloaterSize() / 5);
        }
        Util.log("checkpoint 3");
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.y = SystemOverlay.floater.floaterPosY();
        params.x = SystemOverlay.floater.floaterPosX(params.width);
        Util.log("checkpoint 3a");

        SystemOverlay.floater.container.plot(params);
        Util.log("checkpoint 3b");
        SystemOverlay.floater.plot();
        Util.log("checkpoint 3c");

        SView.Layout fEdit = SystemOverlay.floater.openLayout();
        fEdit.setWidth(width);
        fEdit.setHeight(SettingsUtil.getFloaterSize() - (SettingsUtil.getFloaterSize() / 5));
        fEdit.addRule(RelativeLayout.CENTER_VERTICAL);
        fEdit.addRule(innerG);
        fEdit.save();
        Util.log("checkpoint 4");

        if (visibility == View.VISIBLE) {
            SystemOverlay.floater.showFloater();
        }else{
            SystemOverlay.floater.hideFloater();
        }
        Util.log("checkpoint 5");

    }

    private void updateVisibility() {
        container.layout.setVisibility(currentVisibility);
    }

    private void setVisibility(int visibility) {
        currentVisibility = visibility;
    }
    public int getVisibility() {
        return currentVisibility;
    }
    public class FloaterController {
        public Handler longPressListener;
        public Runnable longPressRunnable;
        public boolean floaterRelocate = false, currentlyInTouch = false, touchEnabled = false;
        public float initialX,initialTouchX, initialTouchY, yOffset = 0, originalY;
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
                    yOffset = event.getRawY() - container.y();
                    initialX = container.x();
                    originalGravity = SettingsUtil.getFloaterGravity();
                    originalY = SettingsUtil.getFloaterPos();
                    originalLastFloaterUpdate = SettingsUtil.getLastFloaterUpdate();

                    garbage = new Garbage(new RelativeLayout(c), new RelativeLayout(c), new ImageView(c));
                    garbage.plot();

                    SView.Layout fEdit = openLayout();
                    if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        fEdit.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    }else {
                        fEdit.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    }
                    fEdit.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    fEdit.save();
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
                    int w = container.width() / 2;
                    int h = container.height();

                    if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        if (fX - initialTouchX >= w && Math.abs(fY - initialTouchY) <= h) {
                            UserInterface.launchUI();
                        }
                    }else{
                        if (initialTouchX - fX >= w && Math.abs(fY - initialTouchY) <= h) {
                            UserInterface.launchUI();
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
                if (rawY + container.height() > (height - (border))) {
                    rawY = ((height - (border)) - container.height());
                }
                else if (rawY < border) {
                    rawY = ((border));
                }
                editor.setX((initialX + (int) (event.getRawX() - initialTouchX)));
                editor.setY(rawY);
                SettingsUtil.setFloaterPos((rawY) / (height));

                if (c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.LANDSCAPE);
                } else {
                    SettingsUtil.setLastFloaterUpdate(FloaterUpdate.PORTRAIT);
                }
                if (event.getRawX() < width) {
                    SView.Layout fEdit = openLayout();
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
                        SettingsUtil.setFloaterGravity(WindowGravity.LEFT);

                    }
                } else if (event.getRawX() >= width) {
                    SView.Layout fEdit = openLayout();
                    if (!SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
                        SettingsUtil.setFloaterGravity(WindowGravity.RIGHT);
                    }
                }
                editor.save();

                Rect cRect = new Rect(container.x(), container.y(), container.x() + container.width(), container.y() + container.height());
                if (cRect.intersects(garbage.trash.x() + (SettingsUtil.getFloaterSize() / 2), garbage.trash.y(), garbage.trash.x() + garbage.trash.width() - (SettingsUtil.getFloaterSize() / 2), garbage.trash.y() + garbage.trash.height())) {
                    if (garbage.trash.height() != SettingsUtil.getFloaterSize()) {
                        SView.Layout tEdit = garbage.trash.openLayout();
                        tEdit.setHeight(SettingsUtil.getFloaterSize());
                        tEdit.setWidth(SettingsUtil.getFloaterSize());
                        tEdit.save();
                    }
                } else {
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
                int x = container.x();
                int y = container.y();
                int w = container.width();
                int h = container.height();
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
        setVisibility(View.INVISIBLE);

        Anim anim = new Anim(SystemOverlay.service, view, 100);
        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            anim.addTranslate(-width(),0);
        }else{
            anim.addTranslate(width(),0);
        }
        anim.addAlpha(Anim.FADE_OUT);
        anim.setEnd(new Runnable() {
            @Override
            public void run() {
                updateVisibility();
            }
        });
        anim.start();
    }
    public void showFloater() {
        setVisibility(View.VISIBLE);
        updateVisibility();

        Anim anim = new Anim(SystemOverlay.service, view, 100);
        if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
            anim.addTranslate(-width(),width(),0,0);
        }else{
            anim.addTranslate(width(),-width(),0,0);
        }
        anim.addAlpha(Anim.FADE_IN);
        anim.setEnd(new Runnable() {
            @Override
            public void run() {
                floaterMovement.enableTouch(true);
            }
        });
        anim.start();
    }

    public void updateFloater() {
        SWindowLayout.Layout editor = container.openLayout();
        editor.setY(floaterPosY());
        editor.setX(floaterPosX(container.width()));
        editor.save();
    }
    public class Garbage extends SView{
        public SView trash;
        public Garbage(RelativeLayout background, RelativeLayout gradient, ImageView trash) {
            super(gradient, new SWindowLayout(background));
            this.trash = new SView(trash, new SWindowLayout(gradient));
        }
        public void plot() {
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(Util.screenWidth(),
                    SettingsUtil.getFloaterSize() * 2, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.BOTTOM;
            container.plot(params);
            super.plot();
            trash.plot();
            ShapeDrawable d = new ShapeDrawable(new RectShape());
            d.getPaint().setShader(new LinearGradient(0,0,0,params.height, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP));
            ((ImageView)trash.view).setScaleType(ImageView.ScaleType.FIT_XY);
            ((ImageView)trash.view).setAdjustViewBounds(true);
            ImageUtil.setBackground(view, d);
            ImageUtil.setImageDrawable(((ImageView)trash.view), R.drawable.garbage);

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

            Anim anim = new Anim(SystemOverlay.service, view, 300);
            anim.addAlpha(Anim.FADE_IN);
            anim.start();
        }
    }
}
