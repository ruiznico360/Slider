package mobile.slider.app.slider.model;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import java.util.ArrayList;

import mobile.slider.app.slider.model.SView.SView;
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
    private Runnable onStart, onEnd, condition;
    private Translate translate;
    private Alpha alpha;
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
                    }
                    if (translate != null) {
                        float incrementX = (float)translate.xOffset / speed;
                        float incrementY = (float)translate.yOffset / speed;
                        view.view.setTranslationX(translate.initX + incrementX * (float)counter);
                        view.view.setTranslationY(translate.initY + incrementY * (float)counter);
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

        if (onEnd != null) {
            onEnd.run();
        }
        if (translate != null) {
            view.view.setTranslationX(0);
            view.view.setTranslationY(0);
        }
        if (alpha != null) {
            view.view.setAlpha(1);
        }
    }
    public void setStart(Runnable r) {
        this.onStart = r;
    }

    public void setEnd(Runnable r) {
        this.onEnd = r;
    }

    private class Translate {
        public int initX, xOffset, initY, yOffset;
    }
    private class Alpha {
        public String type;
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
