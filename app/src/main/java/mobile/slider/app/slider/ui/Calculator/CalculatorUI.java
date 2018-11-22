package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatTextView;
import android.util.LayoutDirection;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
import mobile.slider.app.slider.model.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class CalculatorUI extends UIClass {
    public final float TEXT_SIZE = 0.75f;
    public static final int operatorRGB = Color.rgb(66, 134, 244);
    public Context c;
    public int calcHeight;
    public SView mainLayout, calcLayout,numberText, answerText, dummyAnswerText, answerLayout, numberLayout, operatorsLayout, textLayout;
    public ArrayList<SView> operators;
    public float operatorsTotalHeight, operatorHeight, operatorWidth;
    public CalcHandler calcHandler;

    public enum ID {
        CLEAR(R.drawable.calculator_clear,""),  BRACKET(R.drawable.calculator_bracket,"("), ANSWER(R.drawable.calculator_ans, "?"), DELETE(R.drawable.calculator_delete,""), SQROOT(R.drawable.calculator_sqroot,"√"), POW(R.drawable.calculator_pow,"^"), PERCENT(R.drawable.calculator_percent, "%"), PI(R.drawable.calculator_pi,"π"), SEVEN(R.drawable.calculator_seven,7), EIGHT(R.drawable.calculator_eight,8), NINE(R.drawable.calculator_nine,9), DIVIDE(R.drawable.calculator_divide,"÷"), FOUR(R.drawable.calculator_four,4), FIVE(R.drawable.calculator_five,5), SIX(R.drawable.calculator_six,6), MULT(R.drawable.calculator_mult,"x"), ONE(R.drawable.calculator_one,1), TWO(R.drawable.calculator_two,2), THREE(R.drawable.calculator_three,3), SUB(R.drawable.calculator_sub,"-")
        , NEGATE(R.drawable.calculator_negate,""), ZERO(R.drawable.calculator_zero,0), DEC(R.drawable.calculator_dec,"."), ADD(R.drawable.calculator_add,"+"), EQUAL(R.drawable.calculator_equal,""), EULER(R.drawable.calculator_euler, "*");


        public int drawableRes;
        public String numValue;
        public static final String NUM_VALUES = "[0123456789/]", OPERATOR_VALUES = "[÷^x+-]", VARIABLE_VALUES = "[" + PI.numValue + EULER.numValue + "]";

        ID(int drawableRes, String numValue) {
            this.drawableRes = drawableRes;
            this.numValue = numValue;
        }
        ID(int drawableRes, int numValue) {
            this(drawableRes, numValue + "");
        }
        public boolean isNumber() {
            return numValue.matches(NUM_VALUES);
        }
        public boolean isBasicOperator() {
            return numValue.matches(OPERATOR_VALUES);
        }
        public boolean isVariable() {
            return numValue.matches(VARIABLE_VALUES);
        }
    }

    public CalculatorUI(Context c) {
        this.c = c;
    }
    public String getID() {
        return UserInterface.CALCULATOR_WINDOW;
    }
    public void setup() {
        calcHandler = new CalcHandler(this);
        UserInterface.UI.resize(Util.displayWidth() / 2);
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.view.setBackgroundColor(Color.WHITE);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        calcHeight = Util.screenHeight() - Util.getStatusBarHeight();

        calcLayout = new SView(new RelativeLayout(c), mainLayout.view);
        calcLayout.view.setBackgroundColor(Color.GRAY);
        calcLayout.plot();
        calcLayout.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(calcHeight)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                .save();

        new CalcSetup().setup();
    }
    public class CalcSetup {
        public void setup() {
            operators = new ArrayList<>();
            operatorsTotalHeight = hUnit(70);
            operatorHeight = operatorsTotalHeight / 7f;
            operatorWidth = calcLayout.width() / 4f;
            setupMainOperators();
            setupTextLayout();

        }
        public void setupMainOperators() {
            operatorsLayout = new SView(new RelativeLayout(c), calcLayout.view);
            operatorsLayout.plot();
            operatorsLayout.view.setBackgroundColor(Color.WHITE);
            operatorsLayout.openRLayout()
                    .setWidth(operatorWidth * 4)
                    .setHeight(operatorsTotalHeight)
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    .save();

            SView equalButton = new SView(new ImageView(c), operatorsLayout.view);
            ImageUtil.setImageDrawable(equalButton.view, ID.EQUAL.drawableRes);
            equalButton.view.setBackgroundColor(operatorRGB);
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

            for (int y = 0; y < 6; y++) {
                int topMargin = (int)operatorHeight * y;
                for (int x = 0; x < 4; x++) {
                    int leftMargin = (int)operatorWidth * x;
                    final ID id = ids.next();

                    SView operator = new SView(new ImageView(c), operatorsLayout.view);
                    ((ImageView)operator.view).setImageBitmap(calcOperatorBG(id, id.equals(ID.DELETE) ? 0.75f : 0.5f));
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
            float totalHeight = hUnit(100) - operatorsTotalHeight - 4;
            float totalWidth = operatorWidth * 4 - 4;
            textLayout = new SView(new RelativeLayout(c), calcLayout.view);
            textLayout.view.setBackgroundColor(Color.WHITE);
            textLayout.plot();
            textLayout.openRLayout()
                    .setTopM(2)
                    .setWidth(totalWidth)
                    .setHeight(totalHeight)
                    .addRule(RelativeLayout.CENTER_HORIZONTAL)
                    .save();

//            SView deleteButton = new SView(new ImageView(c), textLayout.view);
//            ImageUtil.setImageDrawable(deleteButton.view, ID.DELETE.drawableRes);
//            operators.add(deleteButton);
//            deleteButton.plot();
//            deleteButton.openRLayout()
//                    .setHeight(totalHeight / 4)
//                    .setWidth(totalWidth / 4)
//                    .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
//                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//                    .save();
//            deleteButton.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    calcHandler.handle(ID.DELETE);
//                }
//            });

            answerLayout = new SView(new UIView.MHScrollView(c,false), textLayout.view);
            ((UIView.MHScrollView) answerLayout.view).setHorizontalScrollBarEnabled(false);
            answerLayout.plot();
            answerLayout.openRLayout()
                    .setHeight(totalHeight / 4)
                    .setWidth(textLayout.width() * 2)
                    .setTopM(totalHeight / 2)
                    .save();
            answerLayout.post(new Runnable() {
                @Override
                public void run() {
                    ((UIView.MHScrollView) answerLayout.view).fullScroll(View.FOCUS_RIGHT);
                }
            });

            SView answerLayoutContainer = new SView(new RelativeLayout(c), answerLayout.view);
            answerLayoutContainer.plot();
            ((UIView.MHScrollView.LayoutParams) answerLayoutContainer.params).gravity = Gravity.RIGHT;
            answerLayoutContainer.openLayout()
                    .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT)
                    .setWidth(answerLayout.width())
                    .save();

            SView innerContainer = new SView(new RelativeLayout(c), answerLayoutContainer.view);
            innerContainer.plot(answerLayoutContainer.width(), RelativeLayout.LayoutParams.MATCH_PARENT);

            answerText = new SView(new TextView(c), innerContainer.view);
            ((TextView)answerText.view).setTextColor(operatorRGB);
            answerText.plot(RelativeLayout.LayoutParams.WRAP_CONTENT, answerLayout.height());
            answerText.openRLayout()
                    .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    .save();
            ((TextView) answerText.view).setMaxLines(1);
            ((TextView) answerText.view).setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(answerText.height() * TEXT_SIZE));


            dummyAnswerText = new SView(new TextView(c), innerContainer.view);
            ((TextView)dummyAnswerText.view).setTextColor(operatorRGB);
            dummyAnswerText.plot(RelativeLayout.LayoutParams.WRAP_CONTENT, answerLayout.height());
            dummyAnswerText.openRLayout()
                    .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    .save();
            ((TextView) dummyAnswerText.view).setMaxLines(1);
            ((TextView) dummyAnswerText.view).setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(dummyAnswerText.height() * TEXT_SIZE));



            numberLayout = new SView(new UIView.MHScrollView(c), textLayout.view);
//            ((UIView.MHScrollView) numberLayout.view).setHorizontalScrollBarEnabled(false);
            numberLayout.plot();
            numberLayout.openRLayout()
                    .setHeight(totalHeight / 2)
                    .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    .setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT)
                    .save();

            numberText = new SView(new TextView(c), numberLayout.view);
            numberText.plot(ScrollView.LayoutParams.WRAP_CONTENT, textLayout.width() / 3 < numberLayout.height() ? textLayout.width() / 3 : (int) (numberLayout.height()));
            ((UIView.MHScrollView.LayoutParams)numberText.params).gravity = Gravity.CENTER_VERTICAL;
            numberText.openLayout().save();
            ((TextView) numberText.view).setMaxLines(1);
            ((TextView) numberText.view).setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(numberText.height() * TEXT_SIZE));
        }


        public int hUnit(float perc) {
            return (int) ((perc / 100f) * calcHeight);
        }

        public Bitmap calcOperatorBG(ID id, float scale) {
            Bitmap calc = BitmapFactory.decodeResource(c.getResources(), id.drawableRes);
            Bitmap b = Bitmap.createBitmap((int)operatorWidth, (int)operatorHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            Paint p = new Paint();
            p.setColor(Color.GRAY);
            canvas.drawRect(0,0,operatorWidth,operatorHeight,p);
            p.setColor(Color.WHITE);
            canvas.drawRect(2,2,operatorWidth - 1,operatorHeight - 1,p);


            int destSize = (int) (operatorWidth * scale);
            Rect dest = new Rect((int) operatorWidth / 2 - destSize / 2, (int) operatorHeight / 2 - destSize / 2, (int) operatorWidth / 2 + destSize / 2, (int) operatorHeight / 2 + destSize / 2);
            canvas.drawBitmap(calc, new Rect(0, 0, calc.getWidth(), calc.getHeight()), dest, p);

            return b;
        }
    }

    public void remove() { }
    public void backPressed() {
        UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
    }
}
