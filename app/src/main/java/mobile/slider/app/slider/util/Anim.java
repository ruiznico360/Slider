package mobile.slider.app.slider.util;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import java.util.ArrayList;

import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.ui.MainUI;
import mobile.slider.app.slider.ui.UserInterface;

public class Anim {
    public static final String FADE_IN = "IN";
    public static final String FADE_OUT = "OUT";
    public static final String OVERRIDE = "OVERRIDE";
    private static ArrayList<Anim> currentAnims = new ArrayList<>();

    public SView view;
    public Context c;
    public ArrayList<AnimTag> tags;
    public float speed;
    public int counter = 0;
    public long init;
    public int duration;
    public int delay;
    private Runnable onStart, onEnd;
    private Translate translate;
    private Alpha alpha;
    private boolean cancelled = false;

    public Anim(Context c, SView view, int duration) {
        this.c = c;
        this.view = view;
        this.duration = duration;
        this.tags = new ArrayList<>();
        this.delay = 0;
    }

    public static boolean isInAnim(View v) {
        for (int i = 0; i < currentAnims.size(); i++) {
            if (currentAnims.get(i).view.view == v) {
                return true;
            }
        }
        return false;
    }
    public void addTag(String key, Object value) {
        tags.add(new AnimTag(key,value));
    }

    public boolean hasTag(String key) {
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).key.equals(key)) {
                return true;
            }
        }
        return false;
    }
    public Object getTag(String key) {
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).key.equals(key)) {
                return tags.get(i).value;
            }
        }
        return null;
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
        Util.log("cancelled");
    }
    public void start() {
        speed = (float) duration / (1000f / 42f);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (UserInterface.UI != null && view == UserInterface.UI.inner.view) {
//                    Util.log(translate.initX + " " + translate.xOffset);
//                }
                if (cancelled) return;
                if (counter < speed) {
                    if (tags.size() != 0 && hasTag(OVERRIDE)) {
                        for (int i = 0; i < currentAnims.size(); i++) {
                            if (currentAnims.get(i).view == getTag(OVERRIDE)) {
                                cancel();
                            }
                        }
                    }
                    if (counter == 0) {
                        view.currentAnim = Anim.this;
                        if (onStart != null) {
                            onStart.run();
                        }
                        init = SystemClock.uptimeMillis();
                        if (translate != null) {
                            view.view.setTranslationX(translate.initX);
                            view.view.setTranslationY(translate.initY);
                        }
                        currentAnims.add(Anim.this);
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
                    finishAnim();
                }
            }
        },delay);
    }

    public void finishAnim() {
        view.currentAnim = null;
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
        currentAnims.remove(Anim.this);
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
