package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class CalcHandler {
    public CalculatorUI calc;
    public Context c;
    public String calculation = "";
    public String currentNum = "";
    boolean prevEqual = false;
    public String prevAnsDisplay = "", prevAnsValue = "";

    public CalcHandler(CalculatorUI calculator) {
        this.calc = calculator;
        this.c = calc.c;
    }
    public void handle(CalculatorUI.ID id) {
        if (calc.answerLayout.currentAnim != null) return;
        if (calculation.length() > EquationHandler.MAX_LENGTH && !id.equals(CalculatorUI.ID.EQUAL) && !id.equals(CalculatorUI.ID.DELETE) && !id.equals(CalculatorUI.ID.CLEAR)) return;
        final TextView number = ((TextView)calc.numberText.view);
        final TextView answer = ((TextView)calc.answerText.view);
        boolean showAns = false;
        String numberValue;

        if (!id.isNumber()) {
            if (id == CalculatorUI.ID.EQUAL) {
                prevEqual = true;
                if (!EquationHandler.isNum(calculation)) {
//                    Util.log(prevAnsDisplay.equals("") ? "NO PREV ANSWER" : prevAnsDisplay + "=" + prevAnsValue);
                    final String answerValue = EquationHandler.answerValue(calculation, prevAnsDisplay, prevAnsValue);
                    numberValue = "";
                    showAns = true;

                    calc.answerText.post(new Runnable() {
                        @Override
                        public void run() {
                            ((UIView.MHScrollView)calc.numberLayout.view).scrollTo(0,0);
                            ((UIView.MHScrollView)calc.answerLayout.view).scrollTo(0,0);

                            final Anim a = UserInterface.uiAnim(c, calc.answerLayout, 100);
                            float yOffset = (((float)calc.numberText.height()) / calc.answerText.height()) - 1;
                            float xOffset = (((float)calc.answerText.width()) / calc.answerText.height() * calc.numberText.height()) / calc.answerText.width() - 1;

                            int transX = 0;
                            int pivot = calc.answerLayout.width();

                            if (((float)calc.answerText.width()) * (1f + xOffset) > calc.numberLayout.width()) {
                                pivot = 0;
                                transX = (int)((calc.answerLayout.x() - calc.answerText.x()) * (1f + xOffset));
                            }

                            a.addScale(1,xOffset,1,yOffset, pivot);
                            a.addTranslate(transX,(int)(calc.numberText.y() - calc.answerLayout.y()));

                            a.setStart(new Runnable() {
                                @Override
                                public void run() {
                                    currentNum = EquationHandler.simplifyAns(answerValue);
                                    calculation = currentNum;
                                }
                            });
                            a.setEnd(new Runnable() {
                                @Override
                                public void run() {
                                    if (EquationHandler.getError(calculation) != null) {
                                        number.setText(Html.fromHtml(EquationHandler.getError(calculation).equals(EquationHandler.ERROR) ?"<font color=red>" + EquationHandler.ERROR + "</font>" : "<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + calculation + "</font>"), TextView.BufferType.SPANNABLE);
                                        calculation = "";
                                        prevAnsDisplay = "";
                                        prevAnsValue = "";
                                    }else{
                                        number.setText(Html.fromHtml("<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + calculation + "</font>"), TextView.BufferType.SPANNABLE);
                                        prevAnsDisplay = calculation.replace("(-","");

                                        EquationHandler.Value v = new EquationHandler.Operation().gen(answerValue);
                                        if (v.isRational()) {
                                            prevAnsValue = EquationHandler.Operation.derationalize(v).getNumerator().toPlainString();
                                        }else{
                                            prevAnsValue = answerValue.replace("-","");
                                        }
//                                        Util.log("PREVANSVALUE " + v.getNumerator() + "/" + v.getDenominator() + " " + answerValue + " " + prevAnsDisplay);
                                    }
                                    answer.setText("");
                                }
                            });
                            a.start();
                        }
                    });
                }else{
                    numberValue = calculation;
                }
            }else{
                if (id == CalculatorUI.ID.DELETE) {
                    if (!calculation.equals("")) {
                        calculation = calculation.substring(0,calculation.length() - 1);

                        if (!prevAnsDisplay.equals("")) {
                            int start = -1;
                            int end = -1;
                            for (int i = 0; i < calculation.length(); i++) {
                                String prev = calculation.substring(i, i + 1);
                                if (prev.matches(CalculatorUI.ID.NUM_VALUES) || prev.equals(".")) {
                                    if (start == -1) {
                                        start = i;
                                        end = i;
                                    }else{
                                        end = i;
                                    }
                                }else {
                                    if (start != -1) {
                                        break;
                                    }
                                }
                            }

                            if (!prevAnsDisplay.equals(calculation.substring(start, end + 1))) {
                                prevAnsDisplay = "";
                                prevAnsValue = "";
                            }
                        }
                        if (calculation.length() >= 1) {
                            String prev = calculation.substring(calculation.length() - 1, calculation.length());
                            if (prev.matches(CalculatorUI.ID.NUM_VALUES) || prev.equals(".")) {
                                int loc = 0;
                                for (int i = calculation.length() - 1; i >= 0; i--) {
                                    String s = calculation.substring(i,i + 1);
                                    if (!s.matches(CalculatorUI.ID.NUM_VALUES) && !s.equals(".")) {
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
                }else if (id == CalculatorUI.ID.CLEAR) {
                    prevAnsDisplay = "";
                    prevAnsValue = "";
                    calculation = "";
                    currentNum = "";
                }else if (id == CalculatorUI.ID.DEC) {
                    if (currentNum.length() - (currentNum.contains(".") ? 1 : 0) < EquationHandler.MAX_DIGITS) {
                        if (currentNum.equals("") || prevEqual) {
                            handle(CalculatorUI.ID.ZERO);
                            handle(CalculatorUI.ID.DEC);
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
                                calculation += CalculatorUI.ID.MULT.numValue + "(";
                            }
                        }
                    }
                    currentNum = "";
                }else if (id == CalculatorUI.ID.SQROOT || id == CalculatorUI.ID.NEGATE) {
                    String operation = id == CalculatorUI.ID.SQROOT ? id.numValue + "(" : "(" + CalculatorUI.ID.SUB.numValue;
                    if (calculation.equals("")) {
                        calculation += operation;
                    }else if (currentNum.equals("")) {
                        String prev = calculation.substring(calculation.length() - 1, calculation.length());
                        if (!prev.matches(CalculatorUI.ID.OPERATOR_VALUES) && !prev.equals("(") && !prev.equals(CalculatorUI.ID.SQROOT.numValue)) {
                            calculation += CalculatorUI.ID.MULT.numValue + operation;
                        }else{
                            calculation += operation;
                        }
                    }else{
                        int loc = 0;
                        for (int i = calculation.length() - 1; i >= 0; i--) {
                            String s = calculation.substring(i , i + 1);
                            if (!s.matches(CalculatorUI.ID.NUM_VALUES) && !s.equals(".")) {
                                loc = i + 1;
                                break;
                            }
                        }
                        if (loc > 1) {
                            String prev = calculation.substring(loc - 2, loc);
                            if (prev.equals("(" + CalculatorUI.ID.SUB.numValue)) {
                                calculation = calculation.substring(0, loc - 2) + calculation.substring(loc, calculation.length());
                            }else if (prev.equals(CalculatorUI.ID.SQROOT.numValue + CalculatorUI.ID.SUB.numValue)) {
                                calculation = calculation.substring(0, loc - 1) + calculation.substring(loc, calculation.length());
                            }else{
                                calculation = calculation.substring(0, loc) + operation + calculation.substring(loc, calculation.length());
                            }
                        }else{
                            calculation = calculation.substring(0, loc) + operation + calculation.substring(loc, calculation.length());
                        }
                    }
                }

                prevEqual = false;
                numberValue = calculation;
                showAns = !EquationHandler.isNum(calculation);
            }
        }else{
            if (prevEqual) {
                calculation = id.numValue;
                currentNum = id.numValue;
            }else{
                String numVal = currentNum + id.numValue;
                String calcVal = calculation + id.numValue;

                if (currentNum.length() - (currentNum.contains(".") ? 1 : 0) < EquationHandler.MAX_DIGITS) {
                    if (currentNum.equals("") && !calculation.equals("")) {
                        if (calculation.substring(calculation.length() - 1, calculation.length()).equals(")")) {
                            numVal = id.numValue;
                            calcVal = calculation + CalculatorUI.ID.MULT.numValue + id.numValue;
                        }
                    }else {
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
                }else{
                    numVal = currentNum;
                    calcVal = calculation;
                }
                calculation = calcVal;
                currentNum = numVal;
            }

            numberValue = calculation;
            showAns = !EquationHandler.isNum(calculation);
            prevEqual = false;
        }

        String finalNumberText = "";
        for (int i = 0; i < numberValue.length(); i++){
            String val = numberValue.substring(i, i + 1);
            if (!val.matches(CalculatorUI.ID.NUM_VALUES)) {
                finalNumberText += "<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + val + "</font>";
            }else{
                finalNumberText += val;
            }
        }

        number.setText(Html.fromHtml(finalNumberText), TextView.BufferType.SPANNABLE);
        if (showAns) {
            String ans = EquationHandler.simplifyAns(EquationHandler.answerValue(calculation, prevAnsDisplay, prevAnsValue));
            answer.setText(EquationHandler.getError(ans) != null && EquationHandler.getError(ans).equals(EquationHandler.ERROR) ? "" : ans);
        }else{
            answer.setText("");
        }
        if (id != CalculatorUI.ID.EQUAL) {
            calc.answerText.post(new Runnable() {
                @Override
                public void run() {
                    ((UIView.MHScrollView)calc.numberLayout.view).smoothScrollTo(calc.numberText.width() - calc.numberLayout.width(),0);
                    ((UIView.MHScrollView)calc.answerLayout.view).smoothScrollTo(0,0);
                }
            });
        }
    }
}