package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

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

        calcHeight = Util.screenHeight() - Util.getStatusBarHeight();

        calcLayout = new SView(new RelativeLayout(c), mainLayout.view);
        calcLayout.view.setBackgroundColor(Color.RED);
        calcLayout.plot();
        calcLayout.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(calcHeight)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                .save();
        new CalcSetup().setup();
    }
    public class CalcSetup {
        public SView numberText, answerText, operatorsLayout;
        public ArrayList<SView> operators;
        public int operatorsTotalHeight, operatorHeight, operatorWidth;

        public void setup() {
            operators = new ArrayList<>();
            operatorsTotalHeight = hUnit(60);
            operatorHeight = operatorsTotalHeight / 5;
            operatorWidth = (int)(calcLayout.width() / 4f);
            setupOperators();

        }
        public void setupOperators() {
            operatorsLayout = new SView(new RelativeLayout(c), calcLayout.view);
            operatorsLayout.view.setBackgroundColor(Color.GREEN);
            Util.generateViewId(operatorsLayout.view);
            operatorsLayout.plot();
            operatorsLayout.openRLayout()
                    .setWidth(operatorWidth * 4)
                    .setHeight(operatorsTotalHeight)
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    .save();

            SView equalButton = new SView(new ImageView(c), operatorsLayout.view);
            equalButton.view.setBackgroundColor(Color.RED);
            operators.add(equalButton);
            equalButton.plot();
            equalButton.openRLayout()
                    .setHeight(operatorHeight)
                    .setWidth(operatorWidth * 4)
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    .save();

            SView deleteButton = new SView(new ImageView(c), calcLayout.view);
            deleteButton.view.setBackgroundColor(Color.YELLOW);
            Util.generateViewId(deleteButton.view);
            operators.add(deleteButton);
            deleteButton.plot();
            deleteButton.openRLayout()
                    .setHeight(operatorHeight / 2)
                    .setWidth(operatorWidth / 2)
                    .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    .addRule(RelativeLayout.ABOVE, operatorsLayout.view.getId())
                    .save();

            answerText = new SView(new TextView(c), calcLayout.view);
            answerText.view.setBackgroundColor(Color.BLACK);
            Util.generateViewId(answerText.view);
            answerText.plot();
            answerText.openRLayout()
                    .setHeight(operatorHeight / 2)
                    .setWidth(operatorWidth * 4)
                    .addRule(RelativeLayout.ABOVE, deleteButton.view.getId())
                    .save();

            numberText = new SView(new TextView(c), calcLayout.view);
            numberText.view.setBackgroundColor(Color.BLUE);
            numberText.plot();
            numberText.openRLayout()
                    .setHeight(operatorHeight)
                    .setWidth(operatorWidth * 4)
                    .addRule(RelativeLayout.ABOVE, answerText.view.getId())
                    .save();
        }
        public int hUnit(float perc) {
            return (int) ((perc / 100f) * calcHeight);
        }
    }

    public class CalcHandler {
        public final String ONE = "ONE", TWO = "TWO", THREE = "THREE", FOUR = "FOUR", FIVE = "FIVE", SIX = "SIX", SEVEN = "SEVEN", EIGHT = "EIGHT", NINE = "NINE", TEN = "TEN", ZERO = "ZERO"
                , NEGATE = "NEGATE", DEC = "DECIMAL", ADD = "ADD", SUB = "SUBTRACT", MULT = "MULTIPLY", DIVIDE = "DIVIDE", BRACKET = "BRACKET", SQROOT = "SQUAREROOT", POW = "POW", CLEAR = "CLEAR";
        public final String EQUAL = "EQUAL", DEL = "DELETE";

        public void handle(String id) {

        }
    }
    public void remove() { }
    public void backPressed() {
        UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
    }
}
