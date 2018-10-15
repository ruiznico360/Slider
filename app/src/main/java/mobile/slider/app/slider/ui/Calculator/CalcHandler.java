package mobile.slider.app.slider.ui.Calculator;

import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;

import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class CalcHandler {
    public static final int MAX_DIGITS = 10, MAX_LENGTH = 100;
    public static final MathContext MC = MathContext.DECIMAL128;
    public static final String ERROR = "ERROR",POS_INFINITY = Double.POSITIVE_INFINITY + "", NEG_INFINITY = Double.NEGATIVE_INFINITY + "", NAN = Double.NaN + "";
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
        if (calculation.length() > MAX_LENGTH && !id.equals(CalculatorUI.ID.EQUAL) && !id.equals(CalculatorUI.ID.DELETE) && !id.equals(CalculatorUI.ID.CLEAR)) return;
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

                            a.setEnd(new Runnable() {
                                @Override
                                public void run() {
                                    if (getError(calculation) != null) {
                                        number.setText(Html.fromHtml(getError(calculation).equals(ERROR) ?"<font color=red>" + ERROR + "</font>" : "<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + calculation + "</font>"), TextView.BufferType.SPANNABLE);
                                        calculation = "";
                                    }else{
                                        number.setText(Html.fromHtml("<font color=" + Util.hex(CalculatorUI.operatorRGB) + ">" + calculation + "</font>"), TextView.BufferType.SPANNABLE);
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
                    if (currentNum.length() - (currentNum.contains(".") ? 1 : 0) < MAX_DIGITS) {
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

                if (currentNum.length() - (currentNum.contains(".") ? 1 : 0) < MAX_DIGITS) {
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
            answer.setText(getError(ans) != null && getError(ans).equals(ERROR) ? "" : ans);
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
    public String getError(String answer) {
        if (answer.contains(ERROR) || answer.contains(NAN) || answer.contains(POS_INFINITY)) {
            if (answer.equals(ERROR)) {
                return ERROR;
            }else if (answer.equals(NAN)) {
                return NAN;
            }else if (answer.equals(NEG_INFINITY)) {
                return NEG_INFINITY;
            }else if (answer.equals(POS_INFINITY)) {
                return POS_INFINITY;
            }else {
                return ERROR;
            }
        }else{
            return null;
        }
    }
    public Double parse(String num) throws NumberFormatException{
        return Double.parseDouble(num);
    }

    public String answer() {
        String answer = calculation;
        int i = 0;
        do {
            answer = reduce(answer);

            if (i == 100) {
                return ERROR;
            }
            if (getError(answer) != null) {
                return getError(answer);
            }

            i++;
        }while (!isNum(answer) && i < 101);

        return simplifyAns(answer);
    }
    public String simplifyAns(String answer) {
        final int scientificDisplayDec = 4;
        boolean negative = answer.contains("-");
        answer = answer.replace("-","");
        int dec = answer.contains(".") ? answer.indexOf(".") : answer.length();


        if (answer.replace(".","").length() > MAX_DIGITS) {
            String builder;
            int firstNum = -1;
            int lastNum = -1;
            int length = 0;
            boolean roudingReq = false;

            for (int i = 0; i < answer.length(); i++) {
                String s = answer.substring(i, i + 1);
                if (firstNum == -1) {
                    if (!s.equals("0") && !s.equals(".")) {
                        firstNum = i;
                        lastNum = i;
                        length++;
                    }
                }else{
                    if (!s.equals(".")) {
                        if (length < MAX_DIGITS) {
                            if (!s.equals("0")) {
                                lastNum = i;
                            }
                            length++;
                        }else{
                            break;
                        }
                    }
                }
            }
            if (firstNum == -1) return "0";

            builder = answer.substring(firstNum,lastNum + 1).replace(".","");

            if (dec > lastNum) {
                if (dec - firstNum > MAX_DIGITS) {
                    if (builder.length() > 1) {
                        if (builder.length() > scientificDisplayDec + 1) {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,scientificDisplayDec + 1) + CalculatorUI.ID.MULT.numValue + "10" + CalculatorUI.ID.POW.numValue + ((dec - firstNum) - 1);
                        }else {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,builder.length()) + CalculatorUI.ID.MULT.numValue + "10" + CalculatorUI.ID.POW.numValue + ((dec - firstNum) - 1);
                        }
                    }else {
                        builder = builder.substring(0, 1) + ".0" + CalculatorUI.ID.MULT.numValue + "10" + CalculatorUI.ID.POW.numValue + ((dec - firstNum) - 1);
                    }
                }else{
                    builder = answer.substring(firstNum,dec);
                }
            }else if (dec > firstNum) {
                builder = answer.substring(firstNum,lastNum + 1);
            }else{
                if (lastNum - dec > MAX_DIGITS) {
                    if (builder.length() > 1) {
                        if (builder.length() > scientificDisplayDec + 1) {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,scientificDisplayDec + 1) + CalculatorUI.ID.MULT.numValue + "10" + CalculatorUI.ID.POW.numValue + -(firstNum - dec);
                        }else {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,builder.length()) + CalculatorUI.ID.MULT.numValue + "10" + CalculatorUI.ID.POW.numValue + -(firstNum - dec);
                        }
                    }else {
                        builder = builder.substring(0, 1) + ".0" + CalculatorUI.ID.MULT.numValue + "10" + CalculatorUI.ID.POW.numValue + -(firstNum - dec);
                    }
                }else{
                    builder = answer.substring(0,lastNum + 1);
                }
            }

            answer = builder;

        }else {
            int lastNum = -1;
            for (int i = dec + 1; i < answer.length(); i++) {
                if (answer.charAt(i) != '0') {
                    lastNum = i;
                }
            }
            answer = lastNum == -1 ? answer.substring(0,dec) : answer.substring(0,lastNum + 1);
        }
        return negative ? "(-" + answer : answer;
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
        }else{
            CalculatorUI.ID[] order = new CalculatorUI.ID[]{CalculatorUI.ID.SQROOT, CalculatorUI.ID.POW,CalculatorUI.ID.DIVIDE, CalculatorUI.ID.MULT, CalculatorUI.ID.SUB, CalculatorUI.ID.ADD};

            boolean contLoop = true;
            while (contLoop) {
                contLoop = false;
                for (CalculatorUI.ID id : order) {
//                    if (getError(equation) != null) return getError(equation);

                    if (isNum(equation)) break;
                    if (equation.contains(CalculatorUI.ID.SQROOT.numValue)) {
                        int i = equation.lastIndexOf(CalculatorUI.ID.SQROOT.numValue);
                        if (i == equation.length() - 1) {
                            return ERROR;
                        } else {
                            boolean readNum = false;
                            int start = i + 1;
                            int end = equation.length();
                            Double num;


                            for (int n = i + 1; n < equation.length(); n++) {
                                String prev = equation.substring(n, n + 1);
                                if (prev.matches(CalculatorUI.ID.NUM_VALUES)) {
                                    readNum = true;
                                } else if (prev.equals(CalculatorUI.ID.SUB.numValue)) {
                                    if (readNum) {
                                        end = n;
                                        break;
                                    }
                                } else if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                    end = n;
                                    break;
                                }
                            }

                            String sq = equation.substring(start, end);

                            int negTracker = 1;
                            for (int n = 0; n < sq.length(); n++) {
                                if (sq.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                                    negTracker *= -1;
                                }
                            }
                            sq = sq.replace(CalculatorUI.ID.SUB.numValue, "");
                            try {
                                num = parse(sq) * negTracker;
                            } catch (Exception e) {
                                return ERROR;
                            }
                            String value;
                            double d = Math.pow(num, 0.5);
                            if (getError(d + "") != null) {
                                value = getError(d + "");
                            } else {
                                value = BigDecimal.valueOf(d).toPlainString();
                            }
                            equation = equation.substring(0, i) + value + equation.substring(end, equation.length());
                            contLoop = true;
                            break;
                        }
                    } else if (equation.contains(id.numValue)) {
                        int i = equation.indexOf(id.numValue);
                        if (id == CalculatorUI.ID.SUB) {
                            boolean negateFix = true;
                            int indexOfNum = equation.length();
                            if (equation.substring(0, 1).equals(CalculatorUI.ID.SUB.numValue)) {
                                for (int p = 0; p < equation.length(); p++) {
                                    String s = equation.substring(p, p + 1);
                                    if (s.matches(CalculatorUI.ID.NUM_VALUES) || s.equals(".")) {
                                        if (p - indexOfNum > 1) {
                                            negateFix = false;
                                            break;
                                        }
                                        indexOfNum = p;
                                    }
                                }
                            }else{
                                negateFix = false;
                            }
                            if (negateFix) {
                                String negTracker = "";
                                for (int n = 0; n < equation.length(); n++) {
                                    if (equation.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                                        negTracker = negTracker.equals("") ? "-" : "";
                                    }
                                }
                                equation = equation.replace(CalculatorUI.ID.SUB.numValue, "");
                                return negTracker + equation;
                            }
                            if (i == 0 || !equation.substring(i - 1, i).matches(CalculatorUI.ID.NUM_VALUES))
                                continue;
                        }
                        if (i == equation.length() - 1) {
                            return ERROR;
                        } else {
                            Double num1, num2;

                            int end = i;
                            int start = 0;
                            int trueStart = 0, trueEnd = 0;

                            for (int n = i - 1; n >= 0; n--) {
                                String prev = equation.substring(n, n + 1);
                                if (prev.equals(CalculatorUI.ID.SUB.numValue)) {
                                    if (n != 0 && equation.substring(n - 1, n).matches(CalculatorUI.ID.NUM_VALUES)) {
                                        start = n + 1;
                                        break;
                                    }
                                } else if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                    start = n + 1;
                                    break;
                                }
                            }

                            trueStart = start;
                            String sq = equation.substring(start, end);

                            int negTracker = 1;
                            for (int n = 0; n < sq.length(); n++) {
                                if (sq.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                                    negTracker *= -1;
                                }
                            }
                            sq = sq.replace(CalculatorUI.ID.SUB.numValue, "");
                            try {
                                num1 = parse(sq) * negTracker;
                            } catch (Exception e) {
                                return ERROR;
                            }

                            boolean readNum = false;
                            start = i + 1;
                            end = equation.length();


                            for (int n = i + 1; n < equation.length(); n++) {
                                String prev = equation.substring(n, n + 1);
                                if (prev.matches(CalculatorUI.ID.NUM_VALUES)) {
                                    readNum = true;
                                } else if (prev.equals(CalculatorUI.ID.SUB.numValue)) {
                                    if (readNum) {
                                        end = n;
                                        break;
                                    }
                                } else if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                    end = n;
                                    break;
                                }
                            }

                            trueEnd = end;
                            sq = equation.substring(start, end);

                            negTracker = 1;
                            for (int n = 0; n < sq.length(); n++) {
                                if (sq.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                                    negTracker *= -1;
                                }
                            }
                            sq = sq.replace(CalculatorUI.ID.SUB.numValue, "");
                            try {
                                num2 = parse(sq) * negTracker;
                            } catch (Exception e) {
                                return ERROR;
                            }
                            String value = null;

                            try {
                                if (id == CalculatorUI.ID.POW) {
                                    double d = Math.pow(num1, num2);
                                    if (getError(d + "") != null) {
                                        value = getError(d + "");
                                    } else {
                                        value = BigDecimal.valueOf(d).toPlainString();
                                    }
                                } else if (id == CalculatorUI.ID.ADD) {
                                    value = BigDecimal.valueOf(num1).add(BigDecimal.valueOf(num2), MC).toPlainString();
                                } else if (id == CalculatorUI.ID.SUB) {
                                    value = BigDecimal.valueOf(num1).subtract(BigDecimal.valueOf(num2), MC).toPlainString();
                                } else if (id == CalculatorUI.ID.MULT) {
                                    value = BigDecimal.valueOf(num1).multiply(BigDecimal.valueOf(num2), MC).toPlainString();
                                } else if (id == CalculatorUI.ID.DIVIDE) {
                                    if (num2 == 0) {
                                        value = getError((num1 / num2) + "");
                                    } else {
                                        value = BigDecimal.valueOf(num1).divide(BigDecimal.valueOf(num2), MC).toPlainString();
                                    }
                                }
                            }catch (NumberFormatException e) {
                                return POS_INFINITY;
                            }
                            equation = equation.substring(0, trueStart) + value + equation.substring(trueEnd, equation.length());
                            contLoop = true;
                            break;
                        }
                    }
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