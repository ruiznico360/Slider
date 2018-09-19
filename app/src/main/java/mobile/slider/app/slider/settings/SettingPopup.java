package mobile.slider.app.slider.settings;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.ui.activity.SettingsActivity;
import mobile.slider.app.slider.util.Util;

public class SettingPopup {
//    public SettingsActivity act;
//    public RelativeLayout settingPopup,innerLayout;
//    public ImageView windowBackground;
//    public TextView title;
//    public Button neutral,cancel,ok;
//    private boolean isVisible;
//    private View lastInflated;
//    private int alignment;
//    private int gravity;
//
//    public SettingPopup(SettingsActivity act, String titleName) {
//        this.act = act;
//        settingPopup = (RelativeLayout) act.findViewById(R.id.settings_popup_outer_layout);
//        innerLayout = (RelativeLayout) act.findViewById(R.id.settings_popup_inner_layout);
//        windowBackground = (ImageView) act.findViewById(R.id.setting_popup_window);
//        title = (TextView) act.findViewById(R.id.settings_popup_title);
//        neutral = (Button) act.findViewById(R.id.settings_popup_neutral_button);
//        cancel = (Button) act.findViewById(R.id.settings_popup_cancel_button);
//        ok = (Button) act.findViewById(R.id.settings_popup_ok_button);
//
//        innerLayout.setBackgroundColor(Color.TRANSPARENT);
//        neutral.setVisibility(View.INVISIBLE);
//        neutral.setOnTouchListener(null);
//        cancel.setOnTouchListener(Util.darkenAsPressed(new Runnable() {
//            @Override
//            public void run() {
//                remove();
//            }
//        }));
//        title.setText(titleName);
//        alignment = RelativeLayout.BELOW;
//        gravity = RelativeLayout.CENTER_HORIZONTAL;
//
//    }
//    public void enableNeutralButton(String neutral, Runnable onTouch) {
//        this.neutral.setVisibility(View.VISIBLE);
//        this.neutral.setText(neutral);
//        this.neutral.setOnTouchListener(Util.darkenAsPressed(onTouch));
//    }
//    public boolean isVisible() {
//        return isVisible;
//    }
//    public void setHorizontal() {
//        alignment = RelativeLayout.RIGHT_OF;
//        gravity = RelativeLayout.ALIGN_PARENT_LEFT;
//    }
//    public void addView(View v) {
//        if (lastInflated != null) {
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.addRule(alignment, lastInflated.getId());
//            if (gravity == RelativeLayout.CENTER_HORIZONTAL) {
//                params.addRule(gravity);
//            }
//            v.setLayoutParams(params);
//            innerLayout.addView(v);
//            lastInflated = v;
//
//        }else {
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            if (gravity == RelativeLayout.CENTER_HORIZONTAL) {
//                params.addRule(gravity);
//            }
//            innerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
//            v.setLayoutParams(params);
//            innerLayout.addView(v);
//            lastInflated = v;
//        }
//        lastInflated.setId(ViewIdGenerator.generateViewId());
//    }
//    public void setOkListener(Runnable r) {
//        ok.setOnTouchListener(Util.darkenAsPressed(r));
//    }
//    public void setBackgroundColor(int color) {
//        innerLayout.setBackgroundColor(color);
//    }
//    public void show() {
//        isVisible = true;
//        settingPopup.setVisibility(View.VISIBLE);
//        innerLayout.setVisibility(View.GONE);
//        innerLayout.setVisibility(View.VISIBLE);
//        innerLayout.setGravity(RelativeLayout.CENTER_HORIZONTAL);
//        windowBackground.setVisibility(View.VISIBLE);
//
//        settingPopup.startAnimation(AnimationUtils.loadAnimation(act.getApplicationContext(), R.anim.slide_up));
//
//        settingPopup.setSoundEffectsEnabled(false);
//        innerLayout.setSoundEffectsEnabled(false);
//
//        settingPopup.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {}});
//        innerLayout.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {}});
//        windowBackground.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                remove();
//            }
//        });
//    }
//    public void remove() {
//        isVisible = false;
//        Animation a = AnimationUtils.loadAnimation(act.getApplicationContext(), R.anim.slide_down);;
//        a.setAnimationListener(new Animation.AnimationListener() {
//            @Override public void onAnimationStart(Animation animation) {}
//            @Override public void onAnimationRepeat(Animation animation) {}
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                settingPopup.setVisibility(View.INVISIBLE);
//                innerLayout.removeAllViews();
//            }
//        });
//        act.refreshActivity();
//        settingPopup.startAnimation(a);
//        windowBackground.setVisibility(View.INVISIBLE);
//    }
}
