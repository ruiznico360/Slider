package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

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

    public enum ID {
        BRACKET(R.drawable.calculator_bracket), SQROOT(R.drawable.calculator_sqroot), POW(R.drawable.calculator_pow), CLEAR(R.drawable.calculator_clear), SEVEN(R.drawable.calculator_seven), EIGHT(R.drawable.calculator_eight), NINE(R.drawable.calculator_nine), DIVIDE(R.drawable.calculator_divide), FOUR(R.drawable.calculator_four), FIVE(R.drawable.calculator_five), SIX(R.drawable.calculator_six), MULT(R.drawable.calculator_mult), ONE(R.drawable.calculator_one), TWO(R.drawable.calculator_two), THREE(R.drawable.calculator_three), SUB(R.drawable.calculator_sub)
        , NEGATE(R.drawable.calculator_negate), ZERO(R.drawable.calculator_zero), DEC(R.drawable.calculator_dec), ADD(R.drawable.calculator_add), EQUAL(R.drawable.calculator_equal), DELETE(R.drawable.calculator_delete);

        public int drawableRes;

        ID(int drawableRes) {
            this.drawableRes = drawableRes;
        }
    }

    public CalculatorUI(Context c) {
        this.c = c;
    }
    public String getID() {
        return UserInterface.CALCULATOR_WINDOW;
    }
    public void setup() {
        UserInterface.UI.resize(Util.displayWidth() / 2);
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.view.setBackgroundColor(Color.WHITE);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        calcHeight = Util.screenHeight() - Util.getStatusBarHeight();

        calcLayout = new SView(new RelativeLayout(c), mainLayout.view);
        calcLayout.plot();
        calcLayout.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(calcHeight)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                .save();
        new CalcSetup().setup();
    }
    public class CalcSetup {
        public SView numberText, answerText,answerLayout, numberLayout, operatorsLayout, textLayout;
        public ArrayList<SView> operators;
        public float operatorsTotalHeight, operatorHeight, operatorWidth;
        public CalcHandler calcHandler;

        public void setup() {
            calcHandler = new CalcHandler();
            operators = new ArrayList<>();
            operatorsTotalHeight = hUnit(70);
            operatorHeight = operatorsTotalHeight / 6f;
            operatorWidth = calcLayout.width() / 4f;
            setupMainOperators();
            setupTextLayout();

        }
        public void setupMainOperators() {
            operatorsLayout = new SView(new RelativeLayout(c), calcLayout.view);
            operatorsLayout.plot();
            operatorsLayout.openRLayout()
                    .setWidth(operatorWidth * 4)
                    .setHeight(operatorsTotalHeight)
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    .save();

            SView equalButton = new SView(new ImageView(c), operatorsLayout.view);
            ImageUtil.setImageDrawable(equalButton.view, ID.EQUAL.drawableRes);
            equalButton.view.setBackgroundColor(Color.CYAN);
            operators.add(equalButton);
            equalButton.plot();
            equalButton.openRLayout()
                    .setHeight(operatorHeight)
                    .setWidth(operatorWidth * 4)
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    .save();
            equalButton.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calcHandler.handle(ID.EQUAL);
                }
            });

            Iterator <ID> ids = Arrays.asList(ID.values()).iterator();

            for (int y = 0; y < 5; y++) {
                int topMargin = (int)operatorHeight * y;
                for (int x = 0; x < 4; x++) {
                    int leftMargin = (int)operatorWidth * x;
                    final ID id = ids.next();

                    SView operator = new SView(new ImageView(c), operatorsLayout.view);
                    ((ImageView)operator.view).setImageBitmap(calcOperatorBG(id));
                    operator.plot();
                    operator.openRLayout()
                            .setWidth(operatorWidth)
                            .setHeight(operatorHeight)
                            .setTopM(topMargin)
                            .setLeftM(leftMargin)
                            .save();

                    operator.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            calcHandler.handle(id);
                        }
                    });
                }
            }
        }
        public void setupTextLayout() {
            float totalHeight = hUnit(100) - operatorsTotalHeight;
            float totalWidth = operatorWidth * 4;
            textLayout = new SView(new RelativeLayout(c), calcLayout.view);
            textLayout.plot(totalWidth, totalHeight);

            SView deleteButton = new SView(new ImageView(c), textLayout.view);
            ImageUtil.setImageDrawable(deleteButton.view, ID.DELETE.drawableRes);
            operators.add(deleteButton);
            deleteButton.plot();
            deleteButton.openRLayout()
                    .setHeight(totalHeight / 4)
                    .setWidth(totalWidth / 4)
                    .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    .save();
            deleteButton.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calcHandler.handle(ID.DELETE);
                }
            });

            answerLayout = new SView(new UIView.MHScrollView(c), textLayout.view);
            ((UIView.MHScrollView) answerLayout.view).setHorizontalScrollBarEnabled(false);
            answerLayout.plot();
            answerLayout.openRLayout()
                    .setHeight(totalHeight / 4)
                    .setWidth(totalWidth)
                    .setTopM(totalHeight / 2)
                    .save();

            answerText = new SView(new TextView(c), answerLayout.view);
            answerText.plot(ScrollView.LayoutParams.WRAP_CONTENT, answerLayout.height());
            ((UIView.MHScrollView.LayoutParams)answerText.params).gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            answerText.openLayout().save();
            ((TextView) answerText.view).setMaxLines(1);
            ((TextView) answerText.view).setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(answerText.height() * .75));

            numberLayout = new SView(new UIView.MHScrollView(c), textLayout.view);
            numberLayout.view.setBackgroundColor(Color.GREEN);
            ((UIView.MHScrollView) numberLayout.view).setHorizontalScrollBarEnabled(false);
            numberLayout.plot();
            numberLayout.openRLayout()
                    .setHeight(totalHeight / 2)
                    .setWidth(totalWidth)
                    .save();

            numberText = new SView(new TextView(c), numberLayout.view);
            numberText.plot(ScrollView.LayoutParams.WRAP_CONTENT, numberLayout.width() / 2 < numberLayout.height() ? numberLayout.width() / 2 : (int) (numberLayout.height()));
            ((UIView.MHScrollView.LayoutParams)numberText.params).gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            numberText.openLayout().save();
            ((TextView) numberText.view).setMaxLines(1);
            ((TextView) numberText.view).setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(numberText.height() * .75));



        }
        public int hUnit(float perc) {
            return (int) ((perc / 100f) * calcHeight);
        }

        public Bitmap calcOperatorBG(ID id) {
            int destSize = (int)operatorWidth / 2;
            Bitmap calc = BitmapFactory.decodeResource(c.getResources(), id.drawableRes);
            Bitmap b = Bitmap.createBitmap((int)operatorWidth, (int)operatorHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            Paint p = new Paint();
            p.setColor(Color.GRAY);
            canvas.drawRect(0,0,operatorWidth,operatorHeight,p);
            p.setColor(Color.WHITE);
            canvas.drawRect(2,2,operatorWidth - 1,operatorHeight - 1,p);

            Rect dest = new Rect((int)operatorWidth / 2 - destSize / 2,(int)operatorHeight / 2 - destSize / 2,(int)operatorWidth / 2 + destSize / 2,(int)operatorHeight / 2 + destSize / 2);
            canvas.drawBitmap(calc, new Rect(0,0,calc.getWidth(),calc.getHeight()), dest, p);
            return b;
        }
        public class CalcHandler {
            public String calculation = "";
            int i = 0;
            public void handle(ID id) {
                ((UIView.MHScrollView)numberLayout.view).smoothScrollTo(numberText.width() - numberLayout.width(),0);
                ((UIView.MHScrollView)answerLayout.view).smoothScrollTo(answerText.width() - answerLayout.width(),0);

                i++;
                ((TextView) numberText.view).setText(((TextView) numberText.view).getText().toString() + i);
                ((TextView) answerText.view).setText(((TextView) answerText.view).getText().toString() + i);
            }
        }
    }

    public void remove() { }
    public void backPressed() {
        UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
    }
}
