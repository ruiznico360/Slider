package mobile.slider.app.slider.model;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import java.util.ArrayList;

import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class Anim {
    public static final String FADE_IN = "IN";
    public static final String FADE_OUT = "OUT";
    private static ArrayList<Anim> currentAnims = new ArrayList<>();

    public SView view;
    public Context c;
    public ArrayList<AnimTag> tags;
    public float speed;
    public int counter = 0;
    public long init;
    public int duration;
    public int delay;
    public Runnable onStart, onEnd, condition;
    public Translate translate;
    public Alpha alpha;
    public Scale scale;
    public boolean cancelled = false, hideAfter = false;

    public Anim(Context c, SView view, int duration) {
        this.c = c;
        this.view = view;
        this.duration = duration;
        this.tags = new ArrayList<>();
        this.delay = 0;
        this.condition = new Runnable() {
            @Override
            public void run() {

            }
        };
    }

    public static boolean isInAnim(View v) {
        for (int i = 0; i < currentAnims.size(); i++) {
            if (currentAnims.get(i).view.view == v) {
                return true;
            }
        }
        return false;
    }

    public void setHideAfter() {
        hideAfter = true;
    }
    public void addTag(String key, Object value) {
        tags.add(new AnimTag(key,value));
    }

    public void addTranslate(int xOffset, int yOffset) {
        translate = new Translate();
        translate.initX = 0;
        translate.xOffset = xOffset;
        translate.initY = 0;
        translate.yOffset = yOffset;
    }

    public void addTranslate(int initX, int xOffset, int initY, int yOffset) {
        translate = new Translate();
        translate.initX = initX;
        translate.xOffset = xOffset;
        translate.initY = initY;
        translate.yOffset = yOffset;
    }

    public void addScale(float initX, float xOffset, float initY, float yOffset) {
        scale = new Scale();
        scale.initX = initX;
        scale.xOffset = xOffset;
        scale.initY = initY;
        scale.yOffset = yOffset;
        scale.pivotX = 0;
    }
    public void addScale(float initX, float xOffset, float initY, float yOffset, int pivotX) {
        scale = new Scale();
        scale.initX = initX;
        scale.xOffset = xOffset;
        scale.initY = initY;
        scale.yOffset = yOffset;
        scale.pivotX = pivotX;
    }

    public void addAlpha(String type) {
        alpha = new Alpha();
        alpha.type = type;
    }

    public void cancel() {
        cancelled = true;
        finishAnim();
    }
    public void setCondition(Runnable condition) {
        this.condition = condition;
    }
    public void start() {
        speed = (float) duration / (1000f / 42f);
        view.currentAnim = Anim.this;
        currentAnims.add(Anim.this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (UserInterface.UI != null && view == UserInterface.UI.inner.view) {
//                    Util.log(translate.initX + " " + translate.xOffset);
//                }
                if (cancelled) return;
                if (counter < speed) {
                    condition.run();
                    if (counter == 0) {
                        if (onStart != null) {
                            onStart.run();
                        }
                        init = SystemClock.uptimeMillis();
                        if (translate != null) {
                            view.view.setTranslationX(translate.initX);
                            view.view.setTranslationY(translate.initY);
                        }
                        if (scale != null) {
                            view.view.setPivotY(0);
                            view.view.setPivotX(scale.pivotX);
                            view.view.setScaleX(scale.initX);
                            view.view.setScaleY(scale.initY);
                        }
                    }
                    if (translate != null) {
                        float incrementX = (float)translate.xOffset / speed;
                        float incrementY = (float)translate.yOffset / speed;
                        view.view.setTranslationX(translate.initX + incrementX * (float)counter);
                        view.view.setTranslationY(translate.initY + incrementY * (float)counter);
                    }
                    if (scale != null) {
                        float incrementX = (float)scale.xOffset / speed;
                        float incrementY = (float)scale.yOffset / speed;
                        view.view.setScaleX(scale.initX + incrementX * (float)counter);
                        view.view.setScaleY(scale.initY + incrementY * (float)counter);
                    }
                    if (alpha != null) {
                        float increment = 1f / speed;
                        if (alpha.type.equals(FADE_IN)) {
                            view.view.setAlpha(increment * (float) counter);
                        }else{
                            view.view.setAlpha(1f - increment * (float) counter);
                        }
                    }

                    counter++;
                    new Handler().postDelayed(this, 24);
                }else{
                    if (translate != null) {
                        view.view.setTranslationX(translate.initX + translate.xOffset);
                        view.view.setTranslationY(translate.initY + translate.yOffset);
                    }
                    if (scale != null) {
                        view.view.setScaleX(scale.initX + scale.xOffset);
                        view.view.setScaleY(scale.initY + scale.yOffset);
                    }
                    if (alpha != null) {
                        if (alpha.type.equals(FADE_IN)) {
                            view.view.setAlpha(1);
                        }else{
                            view.view.setAlpha(0);
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!cancelled) {
                                finishAnim();
                            }
                        }
                    },24);
                }
            }
        },delay);
    }

    public void finishAnim() {
        if (hideAfter) {
            view.view.setVisibility(View.INVISIBLE);
        }
        view.currentAnim = null;
        currentAnims.remove(Anim.this);

        if (translate != null) {
            view.view.setTranslationX(0);
            view.view.setTranslationY(0);
        }
        if (scale != null) {
            view.view.setPivotX(0);
            view.view.setScaleX(1);
            view.view.setScaleY(1);
        }
        if (alpha != null) {
            view.view.setAlpha(1);
        }

        if (onEnd != null) {
            onEnd.run();
        }
    }
    public void setStart(Runnable r) {
        this.onStart = r;
    }

    public void setEnd(Runnable r) {
        this.onEnd = r;
    }

    public void inFromRight() {
        addTranslate(view.width(), -view.width(),0,0);
    }
    public void inFromLeft() {
        addTranslate(-view.width(), +view.width(),0,0);

    }

    public class Translate {
        public int initX, xOffset, initY, yOffset;
    }
    private class Alpha {
        public String type;
    }
    private class Scale {
        public float initX, xOffset, initY, yOffset;
        public int pivotX;
    }
    public class AnimTag {
        public String key;
        public Object value;

        public AnimTag(String key, Object value)  {
            this.key = key;
            this.value = value;
        }
    }
}
