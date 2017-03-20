package mobile.slider.app.slider.util;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;

public class Util {
    public static View.OnTouchListener darkenAsPressed(final Runnable onClick) {
        return new View.OnTouchListener() {
            private boolean movable = true;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundColor(Color.parseColor("#50d3d3d3"));
                    movable = true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    movable = false;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (movable) {
                        onClick.run();
                        movable = true;
                    }
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                return true;
            }
        };
    }
    public static ShapeDrawable backgroundGradientTop(View container) {
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setShader(new LinearGradient(0,0,0,container.getLayoutParams().height + 10, Color.parseColor("#303F9F"), SettingsUtil.getBackgroundColor(), Shader.TileMode.REPEAT));
        return d;
    }
    public static ShapeDrawable backgroundGradientBottom(View container) {
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setShader(new LinearGradient(0,0,0,container.getLayoutParams().height + 10, SettingsUtil.getBackgroundColor(), Color.parseColor("#303F9F"), Shader.TileMode.REPEAT));
        return d;
    }
    public static Drawable getDrawable(int id) {
        if (Build.VERSION.SDK_INT >= 21) {
            return SystemOverlay.service.getDrawable(id);
        }else{
            return SystemOverlay.service.getResources().getDrawable(id);
        }
    }
}
