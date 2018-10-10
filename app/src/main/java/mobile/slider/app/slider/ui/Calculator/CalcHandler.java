package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.ui.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class CalcHandler {
    public static final int MAX_DIGITS = 15;
    public static final String ERROR = "ERROR";
    public CalculatorUI calc;
    public Context c;
    public String calculation = "";
    public String currentNum = "";
    boolean prevEqual = false;

    public CalcHandler(CalculatorUI calculator) {
        this.calc = calculator;
        this.c = calc.c;
    }
    public void handle(CalculatorUI.ID id) {
        if (calc.answerLayout.currentAnim != null) return;
        final TextView number = ((TextView)calc.numberText.view);
        final TextView answer = ((TextView)calc.answerText.view);
        boolean showAns = false;
        String numberValue = "";

        if (!id.isNumber()) {
            if (id == CalculatorUI.ID.EQUAL) {
                prevEqual = true;
                if (!isNum(calculation)) {
                    currentNum = answer();
                    calculation = answer();
                    numberValue = "";
                    showAns = true;

                    final Anim a = UserInterface.uiAnim(c, calc.answerLayout, 100);
                    float yOffset = (((float)calc.numberText.height()) / calc.answerText.height()) - 1;
                    float xOffset = (((float)calc.answerText.width()) / calc.answerText.height() * calc.numberText.height()) / calc.answerText.width() - 1;

                    a.addScale(1,xOffset,1,yOffset, calc.answerLayout.width());
                    a.addTranslate(0,(int)(calc.numberText.y() - calc.answerLayout.y()));

                    a.setEnd(new Runnable() {
                        @Override
                        public void run() {
                            if (calculation.equals(ERROR)) {
                                calculation = "";
                                number.setText(Html.fromHtml("<font color=red>" + ERROR + "</font>"), TextView.BufferType.SPANNABLE);

                            }else{
                                number.setText(Html.fromHtml("<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + calculation + "</font>"), TextView.BufferType.SPANNABLE);
                            }
                            answer.setText("");
                        }
                    });
                    calc.answerText.post(new Runnable() {
                        @Override
                        public void run() {
                            ((UIView.MHScrollView)calc.numberLayout.view).scrollTo(calc.numberText.width() - calc.numberLayout.width(),0);
                            ((UIView.MHScrollView)calc.answerLayout.view).scrollTo(calc.answerText.width() - calc.answerLayout.width(),0);
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
                    //fix incorrect assignation odf currentNum in delete
                    calculation = "";
                    currentNum = "";
                }else if (id == CalculatorUI.ID.DEC) {
                    if (currentNum.length() < 15) {
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
                            currentNum = "";
                            calculation += id.numValue;
                        }
                    }
                }else if (id == CalculatorUI.ID.BRACKET) {
                    if (calculation.equals("")) {
                        calculation += "(";
                    }else{
                        if (currentNum.equals("")) {
                            String prev = calculation.substring(calculation.length() - 1, calculation.length());
                            if (prev.matches(CalculatorUI.ID.OPERATOR_VALUES) || prev.equals("(")) {
                                calculation += "(";
                            }else{
                                if (leftBrackets() > rightBrackets()) {
                                    calculation += ")";
                                }else {
                                    calculation += CalculatorUI.ID.MULT.numValue + "(";
                                }
                            }
                        }else{
                            if (leftBrackets() > rightBrackets()) {
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
                        calculation = calculation.substring(0, loc) + operation + calculation.substring(loc, calculation.length());
                    }
                }

                prevEqual = false;
                numberValue = calculation;
                showAns = !isNum(calculation);
            }
        }else{
            if (prevEqual) {
                calculation = id.numValue;
                currentNum = id.numValue;
            }else{
                String numVal = currentNum + id.numValue;
                String calcVal = calculation + id.numValue;

                if (currentNum.length() < 15) {
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
            showAns = !isNum(calculation);
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
            String ans = answer();
            answer.setText(ans.equals(ERROR) ? "" : ans);
            calc.answerText.post(new Runnable() {
                @Override
                public void run() {
                    ((UIView.MHScrollView)calc.numberLayout.view).smoothScrollTo(calc.numberText.width() - calc.numberLayout.width(),0);
                    ((UIView.MHScrollView)calc.answerLayout.view).smoothScrollTo(0,0);
                }
            });
        }else{
            answer.setText("");
        }
    }
    public String answer() {
        String answer = calculation;
        int i = 0;
        while (!isNum(answer) && i < 101) {
            answer = reduce(answer);

            if (answer.contains(ERROR) || i == 100) {
                answer = ERROR;
                break;
            }
            i++;
        }

        return answer;
    }
    public String reduce(String equation) {
        if (equation.contains("(")) {
            int leftBLoc = equation.lastIndexOf("(");
            int rightBLoc = equation.length();

            for (int i = leftBLoc; i < equation.length(); i++) {
                if (equation.charAt(i) == ')') {
                    rightBLoc = i;
                    break;
                }
            }

            String bracket = equation.substring(leftBLoc, rightBLoc == equation.length() ? rightBLoc : rightBLoc + 1);
            String bracketEquation = equation.substring(leftBLoc + 1, rightBLoc == equation.length() ? rightBLoc : rightBLoc);

            equation = equation.substring(0,leftBLoc) + reduce(bracketEquation) + equation.substring(rightBLoc == equation.length() ? rightBLoc : rightBLoc + 1, equation.length());
//            Util.log(equation);
        }else{
            CalculatorUI.ID[] order = new CalculatorUI.ID[]{CalculatorUI.ID.SQROOT, CalculatorUI.ID.POW,CalculatorUI.ID.DIVIDE, CalculatorUI.ID.MULT, CalculatorUI.ID.SUB, CalculatorUI.ID.ADD};


            for (CalculatorUI.ID id : order) {
                if (equation.contains(CalculatorUI.ID.SQROOT.numValue)) {
                    int i = equation.lastIndexOf(CalculatorUI.ID.SQROOT.numValue);
                    if (i == equation.length() - 1) {
                        return ERROR;
                    }else {
                        int start = i + 1;
                        int end = equation.length();
                        for (int n = start; n < equation.length(); n++) {
                            String prev = equation.substring(n, n + 1);
                            if (prev.equals(CalculatorUI.ID.SUB.numValue)) {
                                if (n != start) {
                                    end = n;
                                }
                            }else if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                end = n;
                            }
                        }
                        String sq = equation.substring(start,end);
                        if (sq.equals(CalculatorUI.ID.SUB.numValue)) sq = "-1";
                        return equation.substring(0, i) + Math.sqrt(Double.parseDouble(sq)) + equation.substring(end, equation.length());
                    }
                }else if (equation.contains(id.numValue)) {
//                    int i = equation.lastIndexOf(id.numValue);
//                    if (i == equation.length() - 1) {
//                        return ERROR;
//                    }else {
//                        int start = i + 1;
//                        int end = equation.length();
//                        for (int n = start; n < equation.length(); n++) {
//                            String prev = equation.substring(n,n + 1);
//                            if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals(".") || prev.equals(CalculatorUI.ID.SUB.numValue) && n != start)) {
//                                end = n;
//                            }
//                        }
//                        equation = equation.substring(0, i) + Math.sqrt(Double.parseDouble(equation.substring(start,end))) + equation.substring(end, equation.length());
//                    }
                }
            }
        }
        return equation;
    }
    public boolean isNum(String num) {
        if (num.equals("")) return true;
        try {
            Double.parseDouble(num);
            return true;
        }catch (Exception e) {
            return false;
        }
    }
    public int leftBrackets() {
        int num = 0;
        for (int i = 0; i < calculation.length(); i++) {
            if (calculation.charAt(i) == '(') num++;
        }
        return num;
    }
    public int rightBrackets() {
        int num = 0;
        for (int i = 0; i < calculation.length(); i++) {
            if (calculation.charAt(i) == ')') num++;
        }
        return num;
    }
}