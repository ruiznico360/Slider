package mobile.slider.app.slider.ui.Calculator;

import android.graphics.Path;
import android.os.SystemClock;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import mobile.slider.app.slider.util.Util;

import static mobile.slider.app.slider.ui.Calculator.EQMath.PRECISION;

public class EquationHandler {
    public static final int MAX_DIGITS = 12,MAX_LENGTH = 100, MAX_OPERATORS = 10, BD_SCALE = MAX_DIGITS, SCIENTIFIC_NOT_DIGITS = 5;
    public static final String ERROR = "ERROR",POS_INFINITY = Double.POSITIVE_INFINITY + "", NEG_INFINITY = Double.NEGATIVE_INFINITY + "", NAN = Double.NaN + "", PI = "245850922/78256779", E = "2.7182818284590452354";

    public static String getError(String answer) {
        if (answer.length() == 0) return ERROR;
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
    public static Double parse(String num) throws NumberFormatException{
        return Double.parseDouble(num);
    }

    public static String answerValue(String calculation) {
        String answer = calculation;

        for (int i = 0; i < answer.length(); i++) {
            String sub = answer.substring(i, i + 1);
            if (sub.equals("e")) {
                answer = answer.substring(0,i) + CalculatorUI.ID.MULT.numValue + "10" + CalculatorUI.ID.POW.numValue + answer.substring((answer.substring(i + 1, i + 2).equals(CalculatorUI.ID.ADD.numValue) ? i + 2 : i + 1), answer.length());
            }else if (sub.equals(CalculatorUI.ID.PI.numValue)) {
                answer = answer.substring(0,i) + PI + answer.substring(i + 1, answer.length());
            }else if (sub.equals(CalculatorUI.ID.EULER.numValue)) {
                answer = answer.substring(0,i) + E + answer.substring(i + 1, answer.length());
            }else if (sub.equals(CalculatorUI.ID.ANSWER.numValue)) {
                answer = answer.substring(0,i) + "(" + CalcHandler.ansValue + ")" + answer.substring(i + 1, answer.length());
            }
        }


        int i = 0;
        do {
            answer = EQMath.reduce(answer);


            if (i == 100) {
                return ERROR;
            }
            if (getError(answer) != null && (getError(answer).equals(ERROR) || getError(answer).equals(NAN))) {
                return getError(answer);
            }
            i++;
        }while (!isNum(answer) && i < 101);

        return answer;
    }
//    public static String checkPrecision(EQMath.Value v) {
//        int numerDec = v.getNumerator().toString().contains(".") ? v.getNumerator().toString().indexOf(".") : v.getNumerator().toString().length();
//        int denomDec = v.getDenominator().toString().contains(".") ? v.getDenominator().toString().indexOf(".") : v.getDenominator().toString().length();
//
//        if (denomDec - numerDec > BD_SCALE - SCIENTIFIC_NOT_DIGITS) {
//            v.setDenominator(EQMath.getVal("0." + v.getDenominator().toString().substring(0,SCIENTIFIC_NOT_DIGITS + 1).replace(".","")));
//
//            EQMath.Value newVal = v.derationalize();
//
//            String newAns = "0.";
//            for (int i = 0; i < denomDec - 1; i++) {
//                newAns += "0";
//            }
//            newAns += newVal.getNumerator().toString().replace(".","");
//            Util.log(v.numerator + " " + v.denominator + " " + newAns);
//            return newAns;
//        }else{
//            return v.derationalize().getNumerator().toString();
//        }
//    }
    public static String simplifyAns(String answer) {
        if (getError(answer) != null) {
            return getError(answer);
        }
        boolean negative = answer.contains(EQMath.RAW_NEG);
        answer = answer.replace(EQMath.RAW_NEG,"");

        answer = ApfloatMath.round(new Apfloat(((EQMath.Value.gen(answer).derationalize().numerator))), BD_SCALE, RoundingMode.HALF_EVEN).toString(true);


        int dec = answer.contains(".") ? answer.indexOf(".") : answer.length();


        if (answer.replace(".","").length() > MAX_DIGITS) {
            String builder;
            int firstNum = -1;
            int lastNum = -1;
            int length = 0;

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
                        if (builder.length() > SCIENTIFIC_NOT_DIGITS + 1) {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,SCIENTIFIC_NOT_DIGITS + 1) + "e+" + ((dec - firstNum) - 1);
                        }else {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,builder.length()) + "e+" + ((dec - firstNum) - 1);
                        }
                    }else {
                        builder = builder.substring(0, 1) + ".0" + "e+" + ((dec - firstNum) - 1);
                    }
                }else{
                    builder = answer.substring(firstNum,dec);
                }
            }else if (dec > firstNum) {
                builder = answer.substring(firstNum,lastNum + 1);
            }else{
                if (lastNum - dec > MAX_DIGITS) {
                    if (builder.length() > 1) {
                        if (builder.length() > SCIENTIFIC_NOT_DIGITS + 1) {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,SCIENTIFIC_NOT_DIGITS + 1) + "e" + -(firstNum - dec);
                        }else {
                            builder = builder.substring(0, 1) + "." + builder.substring(1,builder.length()) + "e" + -(firstNum - dec);
                        }
                    }else {
                        builder = builder.substring(0, 1) + ".0" + "e" + -(firstNum - dec);
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
        return (negative ? "-" : "") +  answer;
    }
    public static boolean isNum(String num) {
        if (num.equals("")) return true;
        num = num.replace(EQMath.RAW_NEG, EQMath.NEG);
        try {
            if (num.contains("/")) {
                parse(EQMath.Operation.numerator(num));
                parse(EQMath.Operation.denominator(num));
            }else{
                parse(num);
            }
            return true;
        }catch (Exception e) {
            return false;
        }
    }
    public static int leftBrackets(String calculation) {
        int num = 0;
        for (int i = 0; i < calculation.length(); i++) {
            if (calculation.charAt(i) == '(') num++;
        }
        return num;
    }
    public static int rightBrackets(String calculation) {
        int num = 0;
        for (int i = 0; i < calculation.length(); i++) {
            if (calculation.charAt(i) == ')') num++;
        }
        return num;
    }

}
