package mobile.slider.app.slider.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class RoundedImageView extends AppCompatImageView {
    private Paint objPaint = new Paint();
    public Bitmap bMap;
    public boolean conserveMemory;

    public RoundedImageView(Context context, boolean conserveMemory) {
        super(context);
        this.conserveMemory = conserveMemory;
    }

    public RoundedImageView(Context context) {
        super(context);
        this.conserveMemory = false;
    }

    @Override
    public void setImageBitmap(Bitmap b) {
        bMap = b;
        invalidate();
    }
    @Override
    public void setImageDrawable(Drawable d) {
        bMap = ((BitmapDrawable)d).getBitmap();
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
//        Drawable drawable = getDrawable();
//
//        if (drawable == null) {
//            return;
//        }

        if (bMap == null) return;

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap bitmap  = bMap;

        int w = getWidth();
        Bitmap roundBitmap = getCroppedBitmap(bitmap, w);
        objPaint.setAntiAlias(true);
        objPaint.setDither(true);
        canvas.drawBitmap(roundBitmap, 0, 0, objPaint);

        if (conserveMemory) {
            roundBitmap.recycle();
            bMap.recycle();
            bMap = null;
        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        }else {
            sbmp = bmp;
        }

        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f, sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }
}
