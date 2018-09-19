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
import mobile.slider.app.slider.services.SystemOverlay;

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

        final WindowManager.LayoutParams params = SystemOverlay.newWindow(false);
        params.width = (Util.screenWidth() / 8) * 7;
        params.height = size;
        params.y = Util.screenHeight() - (size * 3);
        params.gravity = Gravity.TOP;

        ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).addView(toast, params);
        toast.addView(text);

        RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams) text.getLayoutParams();
        textParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        textParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        toast.updateViewLayout(text, textParams);

        toast.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).removeView(toast);
                currentToast = null;
            }
        }, 5000);
        currentToast = toast;
    }
}
