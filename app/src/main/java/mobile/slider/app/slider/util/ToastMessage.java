package mobile.slider.app.slider.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;

public class ToastMessage {
    public static final String HIDING_FLOATER = "hiding_floater";
    public static final String HOWDY = "howdy";
    public static RelativeLayout currentToast = null;
    public static Runnable remove;

    public static void toast(final Context c, String toastMsg) {
        if (ToastMessage.currentToast != null) {
            ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).removeView(ToastMessage.currentToast);
            ToastMessage.currentToast.removeCallbacks(ToastMessage.remove);
            ToastMessage.currentToast = null;

        }

        final RelativeLayout toast = new RelativeLayout(c);
        final ImageView text = new ImageView(c);

        if (toastMsg.equals(ToastMessage.HIDING_FLOATER)) {
            ImageUtil.setImageDrawable(text, R.drawable.toast_hiding_floater);
        }else if (toastMsg.equals(ToastMessage.HOWDY)) {
            ImageUtil.setImageDrawable(text, R.drawable.toast_howdy);
        }

        int size;
        if (Util.screenHeight() > Util.screenWidth()) {
            size = Util.screenWidth() / 9;
        }else{
            size = Util.screenHeight() / 9;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams((Util.screenWidth() / 8) * 7,
                size, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN + WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);

        params.y = Util.screenHeight() - (size * 3);
        params.gravity = Gravity.TOP;

        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(toast, params);
        toast.addView(text);

        RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams) text.getLayoutParams();
        textParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        textParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        toast.updateViewLayout(text, textParams);

        text.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fade_in));
        remove = new Runnable() {
            @Override
            public void run() {
                Animation a = AnimationUtils.loadAnimation(c, R.anim.fade_out);
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).removeView(toast);
                        currentToast = null;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                text.startAnimation(a);
            }
        };
        toast.postDelayed(remove,5000);
        currentToast = toast;
    }
}
