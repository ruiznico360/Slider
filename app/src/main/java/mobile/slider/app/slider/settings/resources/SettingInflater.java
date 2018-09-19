package mobile.slider.app.slider.settings.resources;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.settings.Setting;
import mobile.slider.app.slider.settings.SettingPopup;
import mobile.slider.app.slider.settings.SettingsUtil;
import mobile.slider.app.slider.settings.SettingsWriter;
import mobile.slider.app.slider.settings.ViewIdGenerator;
import mobile.slider.app.slider.ui.activity.SettingsActivity;
import mobile.slider.app.slider.util.ImageUtil;

public class SettingInflater {
//    private SettingsActivity act;
//    private RelativeLayout settingView;
//    private HashMap<String,Setting> settings;
//    private View lastInflated;
//
//    public SettingInflater(SettingsActivity act) {
//        this.act = act;
//        settingView = act.settingView;
//        settings = act.getSettings();
//    }
//    public void inflateSettings() {
//        settings = act.getSettings();
//        inflateCategory("General");
//        inflateLanguageSetting();
//        inflateCategory("Colors");
////        inflateBackgroundSetting();
//        inflateThemeSetting();
//        inflateAutoSelectThemeSetting();
//        inflateCategory("Window");
//        inflateWindowSizeSetting();
//        inflateWindowGravitySetting();
//        inflateWindowShaderSetting();
//        inflateCategory("Floater");
//        inflateFloaterSizeSetting();
//        inflateFloaterGravitySetting();
//        inflateFloaterIconSetting();
//        inflateCategory("Advanced");
//        inflateResetSettings();
//
//    }
//    private void inflateCategory(String title) {
//        View v = LayoutInflater.from(act).inflate(R.layout.settings_list_row,null);
//        v.findViewById(R.id.setting_list_row_main_layout).setVisibility(View.GONE);
//        v.findViewById(R.id.setting_list_row_category_layout).setVisibility(View.VISIBLE);
//        ((TextView)v.findViewById(R.id.setting_list_row_category)).setText(title);
//        ((TextView) v.findViewById(R.id.setting_list_row_category)).setTextColor(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()));
//        int r = Color.red(SettingsUtil.getBackgroundColor());
//        int g = Color.green(SettingsUtil.getBackgroundColor());
//        int b = Color.blue(SettingsUtil.getBackgroundColor());
//        v.findViewById(R.id.setting_list_row_category_layout).setBackgroundColor(Color.rgb((int) (r * .8), (int) (g * .8), (int) (b * .8)));
//        v.setId(ViewIdGenerator.generateViewId());
//        addToSettingView(v);
//    }
//    private View inflateSetting(String titleName,String descriptionName,String setToName) {
//        View v = LayoutInflater.from(act).inflate(R.layout.settings_list_row,null);
//        TextView title = (TextView)v.findViewById(R.id.setting_list_row_title);
//        TextView description = (TextView) v.findViewById(R.id.setting_list_row_description);
//        TextView setTo = (TextView) v.findViewById(R.id.setting_list_row_setTo);
//        title.setText(titleName);
//        description.setText(descriptionName);
//        if (setToName != null) {
//            setTo.setText(setToName);
//        }else{
//            setTo.setVisibility(View.INVISIBLE);
//        }
//
//        if (SettingsUtil.getAppTheme().equals(AppTheme.DARK)) {
//            title.setTextColor(Color.BLACK);
//            description.setTextColor(Color.DKGRAY);
//            setTo.setTextColor(Color.BLUE);
//        }
//        v.setId(ViewIdGenerator.generateViewId());
//        return v;
//    }
//
//    private void addToSettingView(View v) {
//        if (lastInflated != null) {
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.addRule(RelativeLayout.BELOW, lastInflated.getId());
//            v.setLayoutParams(params);
//            settingView.addView(v);
//            lastInflated = v;
//
//        }else{
//            settingView.addView(v);
//            lastInflated = v;
//        }
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        params.addRule(RelativeLayout.BELOW, lastInflated.getId());
//        View divider = LayoutInflater.from(act).inflate(R.layout.divider, null);
//        divider.setLayoutParams(params);
//        divider.setId(ViewIdGenerator.generateViewId());
//        settingView.addView(divider);
//        lastInflated = divider;
//    }
//    public void clear() {
//        settingView.removeAllViews();
//    }
//
//    private void inflateLanguageSetting() {
//        Setting s = settings.get(SettingType.LANGUAGE);
//        View v = inflateSetting(s.title, s.description, s.setTo);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });
//        addToSettingView(v);
//    }
////    private void inflateBackgroundSetting() {
////        Setting s = settings.get(SettingType.BACKGROUND_COLOR);
////        View v = inflateSetting(s.title,s.description,null);
////        v.findViewById(R.id.setting_list_row_setTo_layout).setBackgroundColor(SettingsUtil.oppositeBackgroundShade(SettingsUtil.getBackgroundColor()));
////        ((TextView)v.findViewById(R.id.setting_list_row_setTo)).setTextColor(SettingsUtil.getBackgroundColor());
////        v.findViewById(R.id.setting_list_row_setTo).setBackgroundColor(SettingsUtil.getBackgroundColor());
////        v.findViewById(R.id.setting_list_row_setTo).setVisibility(View.VISIBLE);
////        v.findViewById(R.id.setting_list_row_setTo_layout).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                final SettingPopup p = makeNewSettingPopup(String.format("#%06X", (0xFFFFFF & SettingsUtil.getBackgroundColor())));
////                final ColorPicker picker = new ColorPicker(act);
////                p.enableNeutralButton("Default", new Runnable() {
////                    @Override
////                    public void run() {
////                        picker.setColor(SettingsUtil.defaultBackgroundColor());
////                    }
////                });
////                p.setOkListener(new Runnable() {
////                    @Override
////                    public void run() {
////                        SettingsWriter.setSetting(SettingType.BACKGROUND_COLOR, picker.getColor());
////                        if (SettingsUtil.getAutoSelectAppTheme()) {
////                            if (SettingsUtil.oppositeBackgroundShade(picker.getColor()) == Color.BLACK) {
////                                SettingsWriter.setSetting(SettingType.APP_THEME, AppTheme.DARK);
////                            } else if (SettingsUtil.oppositeBackgroundShade(picker.getColor()) == Color.WHITE) {
////                                SettingsWriter.setSetting(SettingType.APP_THEME, AppTheme.LIGHT);
////                            }
////                        }
////                        p.remove();
////                    }
////                });
////                picker.setColor(SettingsUtil.getBackgroundColor());
////                picker.setShowOldCenterColor(false);
////                picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
////                    @Override
////                    public void onColorChanged(int color) {
////                        p.title.setText(String.format("#%06X", (0xFFFFFF & picker.getColor())));
////                    }
////                });
////                p.addView(picker);
////                p.show();
////            }
////        });
////        addToSettingView(v);
////    }
//    private void inflateThemeSetting() {
//        final Setting s = settings.get(SettingType.APP_THEME);
//        View v = inflateSetting(s.title,s.description,s.setTo);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (s.setTo.equalsIgnoreCase(AppTheme.DARK)) {
//                    SettingsUtil.setAppTheme(AppTheme.LIGHT);
//                } else if (s.setTo.equalsIgnoreCase(AppTheme.LIGHT)) {
//                    SettingsUtil.setAppTheme(AppTheme.DARK);
//                }
//                act.refreshActivity();
//            }
//        });
//        addToSettingView(v);
//    }
//    private void inflateAutoSelectThemeSetting() {
//        final Setting s = settings.get(SettingType.APP_THEME_AUTO_SELECT);
//        View v = inflateSetting(s.title, s.description, null);
//        Switch sw = new Switch(act);
//        if (SettingsUtil.getAutoSelectAppTheme()) {
//            sw.setChecked(true);
//        }
//        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    if (SettingsUtil.oppositeBackgroundShade(SettingsUtil.getBackgroundColor()) == Color.BLACK) {
//                        SettingsUtil.setAppTheme(AppTheme.DARK);
//                    } else if (SettingsUtil.oppositeBackgroundShade(SettingsUtil.getBackgroundColor()) == Color.WHITE) {
//                        SettingsUtil.setAppTheme(AppTheme.LIGHT);
//                    }
//                    SettingsUtil.setAutoSelectAppTheme(true);
//                }else {
//                    SettingsUtil.setAutoSelectAppTheme(false);
//                }
//                act.refreshActivity();
//            }
//        });
//        ((RelativeLayout) v.findViewById(R.id.setting_list_row_setTo_layout)).addView(sw);
//        addToSettingView(v);
//    }
//    private void inflateWindowSizeSetting() {
//        final Setting s = settings.get(SettingType.WINDOW_SIZE);
//        View v = inflateSetting(s.title, s.description, s.setTo);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final SettingPopup p = makeNewSettingPopup("Select Window Size");
//                final SeekBar seekBar = new SeekBar(act);
//                final TextView percentage = new TextView(act);
//                percentage.setTextColor(Color.BLACK);
//                percentage.setTextSize(20);
//                percentage.setTypeface(Typeface.DEFAULT_BOLD);
//
//                p.addView(percentage);
//                p.addView(seekBar);
//
//                ((RelativeLayout.LayoutParams) percentage.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
//
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekBar.getLayoutParams();
//                params.width = 1000;
//                seekBar.setLayoutParams(params);
//                seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()), PorterDuff.Mode.SRC_IN));
//                seekBar.getBackground().setColorFilter(new PorterDuffColorFilter(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()), PorterDuff.Mode.MULTIPLY));
//                seekBar.setMax(50);
//                seekBar.setProgress(SettingsUtil.getWindowSize() - 50);
//                percentage.setText("Window windowSize: " + (seekBar.getProgress() + 50) + "%");
//                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        percentage.setText("Window windowSize: " + (progress + 50) + "%");
//                    }
//                });
//                p.enableNeutralButton("Default", new Runnable() {
//                    @Override
//                    public void run() {
//                        seekBar.setProgress(20);
//                    }
//                });
//                p.setOkListener(new Runnable() {
//                    @Override
//                    public void run() {
//                        SettingsUtil.setWindowSize(seekBar.getProgress() + 50);
//                        p.remove();
//                    }
//                });
//                p.show();
//            }
//        });
//        addToSettingView(v);
//    }
//    private void inflateWindowGravitySetting() {
//        Setting s = settings.get(SettingType.WINDOW_GRAVITY);
//        View v = inflateSetting(s.title,s.description,s.setTo);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SettingsUtil.getWindowGravity().equals(WindowGravity.RIGHT)) {
//                    SettingsUtil.setWindowGravity(WindowGravity.LEFT);
//                } else if (SettingsUtil.getWindowGravity().equals(WindowGravity.LEFT)) {
//                    SettingsUtil.setWindowGravity(WindowGravity.RIGHT);
//                }
//                act.refreshActivity();
//            }
//        });
//        addToSettingView(v);
//    }
//    private void inflateWindowShaderSetting() {
//        Setting s = settings.get(SettingType.WINDOW_SHADERS);
//        View v = inflateSetting(s.title,s.description,s.setTo);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final SettingPopup s = makeNewSettingPopup("Choose how shaders will appear on the main window");
//                s.setBackgroundColor(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()));
//
//                final RadioGroup group = new RadioGroup(act);
//                final RadioButton both = new RadioButton(act);
//                final RadioButton top = new RadioButton(act);
//                final RadioButton bottom = new RadioButton(act);
//                final RadioButton neither = new RadioButton(act);
//
//                both.setTypeface(Typeface.DEFAULT_BOLD);
//                top.setTypeface(Typeface.DEFAULT_BOLD);
//                bottom.setTypeface(Typeface.DEFAULT_BOLD);
//                neither.setTypeface(Typeface.DEFAULT_BOLD);
//
//                int color = SettingsUtil.oppositeBackgroundShade(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()));
//
//                both.setTextColor(color);
//                top.setTextColor(color);
//                bottom.setTextColor(color);
//                neither.setTextColor(color);
//
//                both.setText("Top and Bottom of screen");
//                top.setText("Top of screen");
//                bottom.setText("Bottom of screen");
//                neither.setText("No shaders on screen (not recommended)");
//
//                group.addView(both);
//                group.addView(top);
//                group.addView(bottom);
//                group.addView(neither);
//                group.setGravity(Gravity.LEFT);
//
//                if (SettingsUtil.getWindowShaders().equals(WindowShader.BOTH)) {
//                    group.check(both.getId());
//                } else if (SettingsUtil.getWindowShaders().equals(WindowShader.TOP)) {
//                    group.check(top.getId());
//                } else if (SettingsUtil.getWindowShaders().equals(WindowShader.BOTTOM)) {
//                    group.check(bottom.getId());
//                } else if (SettingsUtil.getWindowShaders().equals(WindowShader.NEITHER)) {
//                    group.check(neither.getId());
//                }
//
//                s.addView(group);
//                s.setOkListener(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (group.getCheckedRadioButtonId() == both.getId()) {
//                            SettingsUtil.setWindowShaders(WindowShader.BOTH);
//                        } else if (group.getCheckedRadioButtonId() == top.getId()) {
//                            SettingsUtil.setWindowShaders(WindowShader.TOP);
//                        } else if (group.getCheckedRadioButtonId() == bottom.getId()) {
//                            SettingsUtil.setWindowShaders(WindowShader.BOTTOM);
//                        } else if (group.getCheckedRadioButtonId() == neither.getId()) {
//                            SettingsUtil.setWindowShaders(WindowShader.NEITHER);
//                        }
//                        s.remove();
//                    }
//                });
//                s.show();
//            }
//        });
//        addToSettingView(v);
//    }
//    private void inflateFloaterSizeSetting() {
//        Setting s = settings.get(SettingType.FLOATER_SIZE);
//        View v = inflateSetting(s.title,s.description,s.setTo);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final SettingPopup p = makeNewSettingPopup("Select Floater Size");
//                final SeekBar seekBar = new SeekBar(act);
//                final TextView percentage = new TextView(act);
//                percentage.setTextColor(Color.BLACK);
//                percentage.setTextSize(20);
//                percentage.setTypeface(Typeface.DEFAULT_BOLD);
//
//                p.addView(percentage);
//                p.addView(seekBar);
//
//                ((RelativeLayout.LayoutParams) percentage.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
//
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekBar.getLayoutParams();
//                params.width = 1000;
//                seekBar.setLayoutParams(params);
//                seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()), PorterDuff.Mode.SRC_IN));
//                seekBar.getBackground().setColorFilter(new PorterDuffColorFilter(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()), PorterDuff.Mode.MULTIPLY));
//                seekBar.setMax(50);
//                seekBar.setProgress(SettingsUtil.getFloaterSize() / 10);
//                percentage.setText("Floater windowSize: " + (seekBar.getProgress() * 10) + " px");
//                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        percentage.setText("Floater windowSize: " + (progress * 10) + " px");
//                    }
//                });
//                p.enableNeutralButton("Default", new Runnable() {
//                    @Override
//                    public void run() {
//                        seekBar.setProgress(40);
//                    }
//                });
//                p.setOkListener(new Runnable() {
//                    @Override
//                    public void run() {
//                        SettingsUtil.setFloaterSize(seekBar.getProgress() * 10);
//                        p.remove();
//                    }
//                });
//                p.show();
//            }
//        });
//        addToSettingView(v);
//    }
//    private void inflateFloaterGravitySetting() {
//        Setting s = settings.get(SettingType.FLOATER_GRAVITY);
//        View v = inflateSetting(s.title,s.description,s.setTo);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SettingsUtil.getFloaterGravity().equals(WindowGravity.RIGHT)) {
//                    SettingsUtil.setFloaterGravity(WindowGravity.LEFT);
//                } else if (SettingsUtil.getFloaterGravity().equals(WindowGravity.LEFT)) {
//                    SettingsUtil.setFloaterGravity(WindowGravity.RIGHT);
//                }
//                act.refreshActivity();
//            }
//        });
//        addToSettingView(v);
//    }
//    private void inflateFloaterIconSetting() {
//        final Setting s = settings.get(SettingType.FLOATER_ICON);
//        View v = inflateSetting(s.title, s.description, null);
//        v.setOnClickListener(new View.OnClickListener() {
//            String selected;
//
//            @Override
//            public void onClick(View v) {
//                final SettingPopup p = makeNewSettingPopup("Select floater minimizedIcon");
//
//                final LinearLayout ly = new LinearLayout(act);
//                final ImageView dots = new ImageView(act);
//                final ImageView translucent = new ImageView(act);
//                final ImageView invisible = new ImageView(act);
//
//                ly.setOrientation(LinearLayout.HORIZONTAL);
//                ly.setGravity(Gravity.CENTER_VERTICAL);
//
//                dots.setImageDrawable(ImageUtil.getDrawable(R.drawable.floater_dots));
//                translucent.setImageDrawable(ImageUtil.getDrawable(R.drawable.floater_translucent_icon));
//                invisible.setImageDrawable(ImageUtil.getDrawable(R.drawable.floater_invisible));
//
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
//                params.setMargins(20,0,20,0);
//                dots.setLayoutParams(params);
//                translucent.setLayoutParams(params);
//                invisible.setLayoutParams(params);
//
//                p.addView(ly);
//                RelativeLayout.LayoutParams lyParams = (RelativeLayout.LayoutParams)ly.getLayoutParams();
//                lyParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
//                ly.setLayoutParams(lyParams);
//
//                ly.addView(dots);
//                ly.addView(translucent);
//                ly.addView(invisible);
//
//
//                if (s.setTo.equals(FloaterIcon.DOTS)) {
//                    dots.setBackgroundColor(Color.CYAN);
//                    selected = FloaterIcon.DOTS;
//                } else if (s.setTo.equals(FloaterIcon.TRANSLUCENT)) {
//                    translucent.setBackgroundColor(Color.CYAN);
//                    selected = FloaterIcon.TRANSLUCENT;
//                } else if (s.setTo.equals(FloaterIcon.INVISIBLE)) {
//                    invisible.setBackgroundColor(Color.CYAN);
//                    selected = FloaterIcon.INVISIBLE;
//                }
//
//                View.OnClickListener listener = new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (v == dots) {
//                            p.title.setText("Dots");
//                            selected = FloaterIcon.DOTS;
//                            dots.setBackgroundColor(Color.CYAN);
//                            translucent.setBackgroundColor(Color.TRANSPARENT);
//                            invisible.setBackgroundColor(Color.TRANSPARENT);
//                        } else if (v == translucent) {
//                            p.title.setText("Translucent");
//                            selected = FloaterIcon.TRANSLUCENT;
//                            dots.setBackgroundColor(Color.TRANSPARENT);
//                            translucent.setBackgroundColor(Color.CYAN);
//                            invisible.setBackgroundColor(Color.TRANSPARENT);
//                        } else if (v == invisible) {
//                            p.title.setText("Invisible");
//                            selected = FloaterIcon.INVISIBLE;
//                            dots.setBackgroundColor(Color.TRANSPARENT);
//                            translucent.setBackgroundColor(Color.TRANSPARENT);
//                            invisible.setBackgroundColor(Color.CYAN);
//                        }
//                    }
//                };
//                dots.setOnClickListener(listener);
//                translucent.setOnClickListener(listener);
//                invisible.setOnClickListener(listener);
//
//                p.setOkListener(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        SettingsUtil.setFloaterIcon(selected);
//                                        p.remove();
//                                    }
//                                }
//                );
//                p.show();
//            }
//        });
//        addToSettingView(v);
//    }
//    private void inflateResetSettings() {
//        final Setting s = settings.get(SettingType.RESET_SETTINGS);
//        View v = inflateSetting(s.title,s.description,null);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final SettingPopup p = makeNewSettingPopup("Reset Settings");
//                TextView t = new TextView(act);
//                t.setGravity(Gravity.CENTER);
//                t.setTextSize(20);
//                t.setTypeface(Typeface.DEFAULT_BOLD);
//                t.setText("Are you sure you want to reset your default settings?");
//                t.setTextColor(SettingsUtil.oppositeBackgroundShade(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor())));
//                p.addView(t);
//                p.setBackgroundColor(SettingsUtil.oppositeBackgroundColor(SettingsUtil.getBackgroundColor()));
//                p.setOkListener(new Runnable() {
//                    @Override
//                    public void run() {
//                        SettingsWriter.resetDefaultSettings();
//                        p.remove();
//                    }
//                });
//                p.show();
//            }
//        });
//        addToSettingView(v);
//    }
//    private SettingPopup makeNewSettingPopup(String title) {
//        if (act.currentPopup != null) {
//            act.currentPopup.remove();
//        }
//        SettingPopup p = new SettingPopup(act,title);
//        act.currentPopup = p;
//        return p;
//    }
}
