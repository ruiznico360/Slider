package mobile.slider.app.slider.util;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.settings.SettingsUtil;

public class ImageUtil {
    public static void setImageDrawable(ImageView view, int id) {
        view.setImageDrawable(getDrawable(id));
    }

    public static void setBackground(View view, int id) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setBackground(getDrawable(id));
        }else{
            view.setBackgroundResource(id);
        }
    }

    public static void setBackground(View view, Drawable d) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setBackground(d);
        }else{
            view.setBackgroundDrawable(d);
        }
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
    public static int getRelativeHeight(Drawable d, int width) {
        float ratio = (1f * d.getIntrinsicHeight()) / ((1f) * d.getIntrinsicWidth());
        return (int)((1f * width) * ratio);
    }
    public static int getRelativeWidth(Drawable d, int height) {
        float ratio = (1f * d.getIntrinsicWidth()) / ((1f) * d.getIntrinsicHeight());
        return (int)((1f * height) * ratio);
    }
}
