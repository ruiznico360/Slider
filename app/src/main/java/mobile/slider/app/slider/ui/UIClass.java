package mobile.slider.app.slider.ui;

import android.content.Context;

import mobile.slider.app.slider.util.Util;

public abstract class UIClass {
    public abstract String getID();
    public abstract void setup();
    public abstract void remove();
    public abstract void backPressed();

    public int wUnit(int percent) {
        return (int)(UserInterface.UI.container.width() / 100f * percent);
    }
    public int hUnit(int percent) {
        return (int)(Util.displayHeight() / 100f * percent);
    }
}
