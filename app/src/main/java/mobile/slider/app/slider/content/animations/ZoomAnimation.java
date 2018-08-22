package mobile.slider.app.slider.content.animations;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import mobile.slider.app.slider.util.Util;

public class ZoomAnimation implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.95f;
    private static final float MIN_ALPHA = 0.75f;

    public void transformPage(View view, float position) {
        Util.logM(view.getWidth(), view.getHeight() + " OG");
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (pageWidth == 0) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            pageWidth = params.width;
            pageHeight = params.height;
            Util.logM(pageWidth, pageHeight + " UPDATED");
        }

        if (position < -1) {
            view.setAlpha(0);

        } else if (position <= 1) {
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            view.setAlpha(MIN_ALPHA +
                    (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else {
            view.setAlpha(0);
        }
    }
}
