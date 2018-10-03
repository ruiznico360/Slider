package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.ui.UIClass;
import mobile.slider.app.slider.ui.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class CalculatorUI extends UIClass {
    public Context c;
    public int calcHeight;
    public SView mainLayout, calcLayout;

    public CalculatorUI(Context c) {
        this.c = c;
    }
    public String getID() {
        return UserInterface.CALCULATOR_WINDOW;
    }
    public void setup() {
        UserInterface.UI.resize(Util.displayWidth() / 2);
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        calcHeight = Util.displayWidth() - (Util.displayWidth() / 5);

        calcLayout = new SView(new RelativeLayout(c), mainLayout.view);
        Util.generateViewId(calcLayout.view);
        calcLayout.view.setBackgroundColor(Color.RED);
        calcLayout.plot(wUnit(100), calcHeight);
    }
    public void remove() { }
    public void backPressed() {
        UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
    }
}
