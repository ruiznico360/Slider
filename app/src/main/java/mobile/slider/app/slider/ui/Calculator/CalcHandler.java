package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class CalcHandler {
    public static final String SCROLL_TO_CURRENTNUM = "SCROLL_TO_CURRENTNUM";
    public CalculatorUI calc;
    public Context c;
    public String calculation = "";
    public String currentNum = "";
    public static String ansValue;
    CalculatorUI.ID prevID = CalculatorUI.ID.CLEAR;

    public CalcHandler(CalculatorUI calculator) {
        ansValue = "1";
        this.calc = calculator;
        this.c = calc.c;
    }
    public void handle(CalculatorUI.ID id) {
        if (calc.answerText.currentAnim != null) return;
        if (calculation.length() > EquationHandler.MAX_LENGTH && !id.equals(CalculatorUI.ID.EQUAL) && !id.equals(CalculatorUI.ID.DELETE) && !id.equals(CalculatorUI.ID.CLEAR)) return;

        calc.answerLayout.view.setTranslationY(0);

        final TextView number = ((TextView)calc.numberText.view);
        final TextView answer = ((TextView)calc.answerText.view);
        boolean showAns = false;
        String scrollToStart = null;

        String numberValue;

        if (!id.isNumber()) {
            if (id == CalculatorUI.ID.EQUAL) {
                if (!EquationHandler.isNum(calculation)) {
                    final String answerValue = EquationHandler.answerValue(calculation);
                    numberValue = "";
                    showAns = true;

                    calc.answerText.post(new Runnable() {
                        @Override
                        public void run() {
                            ((UIView.MHScrollView)calc.numberLayout.view).scrollTo(0,0);

                            final Anim a = UserInterface.uiAnim(c, calc.answerLayout, 150);
                            a.addTranslate(0, (calc.numberText.y() + (int)(calc.numberText.height() * (1f - calc.TEXT_SIZE))) - calc.answerLayout.y());

                            a.setStart(new Runnable() {
                                @Override
                                public void run() {
                                    calc.numberLayout.view.setHorizontalScrollBarEnabled(false);

                                    calculation = EquationHandler.simplifyAns(answerValue);
                                    currentNum = calculation;

                                }
                            });
                            a.setEnd(new Runnable() {
                                @Override
                                public void run() {
                                    calc.answerLayout.view.setTranslationY(calc.numberText.y() - calc.answerLayout.y());
                                    if (EquationHandler.getError(calculation) != null) {
                                        number.setText(Html.fromHtml(EquationHandler.getError(calculation).equals(EquationHandler.ERROR) ?"<font color=red>" + EquationHandler.ERROR + "</font>" : "<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + formatCommas(calculation) + "</font>"), TextView.BufferType.SPANNABLE);
                                        calculation = "";
                                    }else{
                                        EQMath.Value v = EQMath.Operation.gen(answerValue);
                                        if (v.isRational()) {
                                            ansValue = EQMath.Operation.derationalize(v).getNumerator().toString();
//                                            Util.log("PREVANSVAL " + prevAnsValue + " " + v.getNumerator());
                                        }else{
                                            ansValue = answerValue;
                                        }
                                    }

                                    calc.numberLayout.view.setHorizontalScrollBarEnabled(true);
                                }
                            });
                            a.start();
                        }
                    });
                }else{
                    numberValue = calculation;
                }
            }else{
                if(prevID.equals(CalculatorUI.ID.EQUAL)) {
                    calculation = "";
                    currentNum = "";
                    prevID = CalculatorUI.ID.CLEAR;
                    handle(CalculatorUI.ID.ANSWER);
                }

                if (id == CalculatorUI.ID.DELETE) {
                    if (!calculation.equals("")) {
                        calculation = calculation.substring(0,calculation.length() - 1);

                        if (calculation.length() >= 1) {
                            String prev = calculation.substring(calculation.length() - 1, calculation.length());
                            if (prev.matches(CalculatorUI.ID.NUM_VALUES) || prev.equals(".") || prev.matches(CalculatorUI.ID.VARIABLE_VALUES) || prev.equals(CalculatorUI.ID.ANSWER.numValue)) {
                                int loc = 0;
                                for (int i = calculation.length() - 1; i >= 0; i--) {
                                    String s = calculation.substring(i,i + 1);
                                    if (!s.matches(CalculatorUI.ID.NUM_VALUES) && !s.equals(".") && !prev.matches(CalculatorUI.ID.VARIABLE_VALUES) && !prev.equals(CalculatorUI.ID.ANSWER.numValue)) {
                                        loc = i + 1;
                                        break;
                                    }
                                }
                                currentNum = calculation.substring(loc,calculation.length());
                            }else{
                                currentNum = "";
                            }
                        }else{
                            currentNum = "";
                        }
                    }
//                    Util.log("DELETED " + prevAnsDisplay + " " + prevAnsValue);
                }else if (id == CalculatorUI.ID.CLEAR) {
                    calculation = "";
                    currentNum = "";
                }else if (id == CalculatorUI.ID.DEC) {


                    if (currentNum.equals("") || currentNum.equals(CalculatorUI.ID.ANSWER.numValue) || currentNum.matches(CalculatorUI.ID.VARIABLE_VALUES)) {
                        handle(CalculatorUI.ID.ZERO);
                        handle(CalculatorUI.ID.DEC);
                        return;
                    }

                    if (currentNum.length() - (currentNum.contains(".") ? 1 : 0) < EquationHandler.MAX_DIGITS) {
                        if (currentNum.equals("")) {
                            handle(CalculatorUI.ID.ZERO);
                            handle(CalculatorUI.ID.DEC);
                            return;
                        } else if (!currentNum.contains(".")) {
                            currentNum += ".";
                            calculation += ".";
                        }
                    }
                }else if (id.isBasicOperator()) {
                    if (!calculation.equals("")) {
                        if (currentNum.equals("")) {
                            String prev = calculation.substring(calculation.length() - 1, calculation.length());
                            if (prev.matches(CalculatorUI.ID.OPERATOR_VALUES)) {
                                if (prev.equals(CalculatorUI.ID.SUB.numValue)) {
                                     if (!calculation.substring(calculation.length() - 2, calculation.length() - 1).matches("[" + CalculatorUI.ID.SQROOT.numValue + "(]")) {
                                        calculation = calculation.substring(0, calculation.length() - 1) + id.numValue;
                                    }
                                }else {
                                    calculation = calculation.substring(0, calculation.length() - 1) + id.numValue;
                                }
                            }else if (prev.equals("(")) {
                                if (id == CalculatorUI.ID.SUB) {
                                    calculation += id.numValue;
                                }
                            }else if (!prev.equals(CalculatorUI.ID.SQROOT.numValue) || id == CalculatorUI.ID.SUB){
                                calculation += id.numValue;
                            }
                        }else{
                            String prev = calculation.substring(calculation.length() - 1, calculation.length());
                            currentNum = "";
                            calculation += (prev.equals(".") ? "0" : "") + id.numValue;
                        }
                    }
                }else if (id == CalculatorUI.ID.BRACKET) {
                    if (calculation.equals("")) {
                        calculation += "(";
                    }else{
                        if (currentNum.equals("")) {
                            String prev = calculation.substring(calculation.length() - 1, calculation.length());
                            if (prev.matches(CalculatorUI.ID.OPERATOR_VALUES) || prev.equals("(") || prev.equals(CalculatorUI.ID.SQROOT.numValue)) {
                                calculation += "(";
                            }else{
                                if (EquationHandler.leftBrackets(calculation) > EquationHandler.rightBrackets(calculation)) {
                                    calculation += ")";
                                }else {
                                    calculation += CalculatorUI.ID.MULT.numValue + "(";
                                }
                            }
                        }else{
                            if (EquationHandler.leftBrackets(calculation) > EquationHandler.rightBrackets(calculation)) {
                                calculation += ")";
                            }else {
                                String prev = calculation.substring(calculation.length() - 1, calculation.length());
                                calculation += (prev.equals(".") ? "0" : "") + CalculatorUI.ID.MULT.numValue + "(";
                            }
                        }
                    }
                    currentNum = "";
                }else if (id == CalculatorUI.ID.SQROOT || id == CalculatorUI.ID.NEGATE) {
                    String operation;
                    if (id == CalculatorUI.ID.SQROOT) {
                        operation = id.numValue + "(";
                        scrollToStart = id.numValue;
                    }else{
                        operation = "(" + CalculatorUI.ID.SUB.numValue;
                        scrollToStart = "(" + CalculatorUI.ID.SUB.numValue;
                    }
                    if (calculation.equals("")) {
                        calculation += operation;
                    }else if (currentNum.equals("")) {
                        String prev = calculation.substring(calculation.length() - 1, calculation.length());
                        if (prev.equals(")") && id == CalculatorUI.ID.SQROOT) {
                            int leftBLoc = 0;
                            int rightBLoc = calculation.length() - 1;

                            int rightCounter = 1;
                            int leftCounter = 0;

                            for (int i = rightBLoc; i >= 0; i--) {
                                if (calculation.charAt(i) == '(') {
                                    leftCounter++;
                                    leftBLoc = i;
                                }else if (calculation.charAt(i) == ')') {
                                    rightCounter++;
                                }

                                if (leftCounter == rightCounter) break;
                            }

                            calculation = calculation.substring(0,leftBLoc) + CalculatorUI.ID.SQROOT.numValue + calculation.substring(leftBLoc, calculation.length());
                        }else if (!prev.matches(CalculatorUI.ID.OPERATOR_VALUES) && !prev.equals("(") && !prev.equals(CalculatorUI.ID.SQROOT.numValue)) {
                            calculation += CalculatorUI.ID.MULT.numValue + operation;
                        }else{
                            calculation += operation;
                        }
                    }else{
                        int loc = 0;
                        for (int i = calculation.length() - 1; i >= 0; i--) {
                            String s = calculation.substring(i , i + 1);
                            if (!s.matches(CalculatorUI.ID.NUM_VALUES) && !s.equals(".") && !s.matches(CalculatorUI.ID.VARIABLE_VALUES) && !s.equals(CalculatorUI.ID.ANSWER.numValue)) {
                                loc = i + 1;
                                break;
                            }
                        }
                        if (loc > 1 && id == CalculatorUI.ID.NEGATE) {
                            String prev = calculation.substring(loc - 2, loc);
                            if (prev.equals("(" + CalculatorUI.ID.SUB.numValue)) {
                                calculation = calculation.substring(0, loc - 2) + calculation.substring(loc, calculation.length());
                                scrollToStart = SCROLL_TO_CURRENTNUM;
                            }else if (prev.equals(CalculatorUI.ID.SQROOT.numValue + CalculatorUI.ID.SUB.numValue)) {
                                calculation = calculation.substring(0, loc - 1) + calculation.substring(loc, calculation.length());
                                scrollToStart = SCROLL_TO_CURRENTNUM;
                            }else{
                                calculation = calculation.substring(0, loc) + operation + calculation.substring(loc, calculation.length());
                            }
                        }else{
                            calculation = calculation.substring(0, loc) + operation + calculation.substring(loc, calculation.length());
                        }
                    }
                }else if (id.isVariable()) {
                    if (calculation.equals("")) {
                        calculation =  id.numValue;
                    }else{
                        String prev = calculation.substring(calculation.length() - 1, calculation.length());
                        if (prev.matches(CalculatorUI.ID.NUM_VALUES) || prev.equals(".") || prev.equals(")") || prev.matches(CalculatorUI.ID.VARIABLE_VALUES) || prev.equals(CalculatorUI.ID.PERCENT.numValue) || prev.equals(CalculatorUI.ID.ANSWER.numValue)) {
                            calculation += (prev.equals(".") ? "0" : "") + CalculatorUI.ID.MULT.numValue + id.numValue;
                        }else{
                            calculation += id.numValue;
                        }
                    }
                    currentNum = id.numValue;
                }else if (id.equals(CalculatorUI.ID.PERCENT)) {
                    if (!calculation.equals("")) {
                        String prev = calculation.substring(calculation.length() - 1, calculation.length());

                        if (prev.matches(CalculatorUI.ID.NUM_VALUES) || prev.equals(".") || prev.equals(")") || prev.matches(CalculatorUI.ID.VARIABLE_VALUES) || prev.equals(CalculatorUI.ID.ANSWER.numValue)) {
                            calculation += (prev.equals(".") ? "0" : "") + id.numValue;
                            currentNum = "";
                        }
                    }
                }else if (id.equals(CalculatorUI.ID.ANSWER)) {
                    if (calculation.equals("")) {
                        calculation += id.numValue;
                    }else{
                        String prev = calculation.substring(calculation.length() - 1, calculation.length());

                        if (prev.matches(CalculatorUI.ID.NUM_VALUES) || prev.equals(".") || prev.equals(")") || prev.matches(CalculatorUI.ID.VARIABLE_VALUES) || prev.equals(CalculatorUI.ID.ANSWER.numValue) || prev.equals(CalculatorUI.ID.PERCENT.numValue)) {
                            calculation += (prev.equals(".") ? "0" : "") + id.MULT.numValue + id.numValue;
                        }else {
                            calculation += id.numValue;
                        }
                    }
                    currentNum = id.numValue;
                }
                numberValue = calculation;
                showAns = !EquationHandler.isNum(calculation);
            }
        }else{
            if (prevID.equals(CalculatorUI.ID.EQUAL)) {
                calculation = id.numValue;
                currentNum = id.numValue;
            }else{
                String numVal = currentNum + id.numValue;
                String calcVal = calculation + id.numValue;

                if (currentNum.matches(CalculatorUI.ID.VARIABLE_VALUES) || currentNum.equals(CalculatorUI.ID.ANSWER.numValue)) {
                    numVal = id.numValue;
                    calcVal = calculation + CalculatorUI.ID.MULT.numValue + id.numValue;
                }else {
                    if (currentNum.length() - (currentNum.contains(".") ? 1 : 0) < EquationHandler.MAX_DIGITS) {
                        if (currentNum.equals("") && !calculation.equals("")) {
                            if (calculation.substring(calculation.length() - 1, calculation.length()).equals(")") || calculation.substring(calculation.length() - 1, calculation.length()).equals(CalculatorUI.ID.PERCENT.numValue)) {
                                numVal = id.numValue;
                                calcVal = calculation + CalculatorUI.ID.MULT.numValue + id.numValue;
                            }
                        } else {
                            if (id == CalculatorUI.ID.ZERO) {
                                if (!currentNum.equals("") && !currentNum.contains(".") && currentNum.charAt(0) == '0') {
                                    numVal = currentNum;
                                    calcVal = calculation;
                                }
                            }

                            if (!currentNum.equals("") && currentNum.charAt(0) == '0' && currentNum.length() == 1) {
                                calcVal = calculation.substring(0, calculation.length() - 1) + id.numValue;
                                numVal = id.numValue;
                            }
                        }
                    } else {
                        numVal = currentNum;
                        calcVal = calculation;
                    }
                }
                calculation = calcVal;
                currentNum = numVal;
            }

            numberValue = calculation;
            showAns = !EquationHandler.isNum(calculation);
        }

        String formatedNumValue = formatCommas(numberValue);
        String finalNumberText = "";
        for (int i = 0; i < formatedNumValue.length(); i++){
            String val = formatedNumValue.substring(i, i + 1);
            if (!val.matches(CalculatorUI.ID.NUM_VALUES) && !val.matches(CalculatorUI.ID.VARIABLE_VALUES) && !val.equals(CalculatorUI.ID.ANSWER.numValue)) {
                finalNumberText += "<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + val + "</font>";
            }else{
                finalNumberText += val;
            }
        }

        finalNumberText = finalNumberText.replace(CalculatorUI.ID.EULER.numValue, "e").replace(CalculatorUI.ID.ANSWER.numValue, "ANS");

        number.setText(Html.fromHtml(finalNumberText), TextView.BufferType.SPANNABLE);

        String prevAnsText = answer.getText().toString();
        if (showAns) {
            String ans = formatCommas(EquationHandler.simplifyAns(EquationHandler.answerValue(calculation)));
//            ans = ((ans.contains("e") || ans.contains(CalculatorUI.ID.SUB.numValue)) && EquationHandler.getError(ans) == null) ? "(" + ans + ")" : ans;
            answer.setText(EquationHandler.getError(ans) != null && EquationHandler.getError(ans).equals(EquationHandler.ERROR) ? "" : ans);
        }else{
            answer.setText("");
        }
        final String scrollToStartfinal = scrollToStart;
        if (id != CalculatorUI.ID.EQUAL) {

            if (!prevAnsText.equals(answer.getText().toString())) answer.setVisibility(View.INVISIBLE);

            calc.answerText.post(new Runnable() {
                @Override
                public void run() {

                    int scrollTo = calc.numberText.width() - calc.numberLayout.width();
                    if (scrollToStartfinal != null) {
                        String text;
                        String numText = number.getText() + "";

                        if (scrollToStartfinal.equals(SCROLL_TO_CURRENTNUM)) {
                            int tracker = currentNum.length() - 1;
                            int index = numText.length() - 1;

                            while (index >= 0) {
                                if (tracker == -1) break;

                                if (numText.substring(index, index + 1).equals(currentNum.substring(tracker, tracker + 1))) {
                                    tracker--;
                                }

                                index--;
                            }

                            text = numText.substring(0, index);
                        }else{
                            text = numText.substring(0, numText.lastIndexOf(scrollToStartfinal));
                        }

                        Rect subBounds = new Rect();
                        number.getPaint().getTextBounds(text, 0, text.length(), subBounds);
                        scrollTo = subBounds.width();
                    }
                    ((UIView.MHScrollView)calc.numberLayout.view).smoothScrollTo(scrollTo,0);

                    float width = calc.textLayout.width();
                    float maxHeight = calc.answerText.height();
                    float scaleX = width / calc.answerText.width();
                    float scaleY = scaleX;

                    if (scaleY * calc.answerText.height() > maxHeight) {
                        scaleY = 1;
                        scaleX = 1;
                    }

                    answer.setPivotX(calc.answerText.width());
                    answer.setPivotY(0);
                    answer.setScaleX(scaleX);
                    answer.setScaleY(scaleY);

                    ((UIView.MHScrollView)calc.answerLayout.view).scrollTo(calc.answerLayout.width(),0);
                    answer.setVisibility(View.VISIBLE);
                }
            });
        }
        prevID = id;
    }
    public String formatCommas(String calculation) {
        final String format = "FORMAT";
        ArrayList<String> numsToFormat = new ArrayList<>();

        int start = -1;
        int end = -1;
        boolean calcLength = true;
        while (calcLength) {
            calcLength = false;
            for (int i = 0; i < calculation.length(); i++) {
                String s = calculation.substring(i, i + 1);
                if (start == -1) {
                    if (s.matches(CalculatorUI.ID.NUM_VALUES) || s.equals(".")) {
                        start = i;
                        end = i;
                    }
                } else {
                    if (s.matches(CalculatorUI.ID.NUM_VALUES) || s.equals(".")) {
                        end = i;
                    } else {
                        String numToAdd = calculation.substring(start, end + 1);
                        numsToFormat.add(numToAdd);
                        calculation = calculation.replaceFirst(numToAdd, format);

                        start = -1;
                        end = -1;
                        break;
                    }
                }
                if (i != calculation.length() - 1) {
                    calcLength = true;
                }else{
                    calcLength = false;
                }
            }
        }
        if (start != -1) {
            String numToAdd = calculation.substring(start, calculation.length());
            numsToFormat.add(numToAdd);
            calculation = calculation.replaceFirst(numToAdd, format);
        }

        for (int i = 0; i < numsToFormat.size(); i++) {
            String raw = numsToFormat.get(i);
            String preDec;
            String postDec;

            if (raw.contains(".")) {
                preDec = raw.substring(0, raw.indexOf("."));
                postDec = raw.substring(raw.indexOf("."), raw.length());
            }else{
                preDec = raw.substring(0, raw.length());
                postDec = "";
            }

            preDec = NumberFormat.getNumberInstance(Locale.US).format(Long.parseLong(preDec));
            calculation = calculation.replaceFirst(format, preDec + postDec);
        }
        return calculation;
    }
}