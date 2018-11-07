package mobile.slider.app.slider.ui.Calculator;

import android.os.SystemClock;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.Aprational;
import org.apfloat.AprationalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import mobile.slider.app.slider.ui.Calculator.BD.BigDecimalMath;
import mobile.slider.app.slider.ui.Calculator.BD.Rational;
import mobile.slider.app.slider.util.Util;

import static mobile.slider.app.slider.ui.Calculator.EquationHandler.BD_SCALE;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.ERROR;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.NAN;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.POS_INFINITY;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.isNum;

public class EQMath {
    public static final int PRECISION = BD_SCALE * 2;
    public static String reduce(String equation) {
        if (equation.length() == 0) return ERROR;
        if (equation.contains("(")) {
            int leftBLoc = equation.lastIndexOf("(");
            int rightBLoc = equation.length();

            for (int i = leftBLoc; i < equation.length(); i++) {
                if (equation.charAt(i) == ')') {
                    rightBLoc = i;
                    break;
                }
            }

            String bracketEquation = equation.substring(leftBLoc + 1, rightBLoc == equation.length() ? rightBLoc : rightBLoc);
            String start;
            if (leftBLoc != 0 && equation.substring(leftBLoc - 1,leftBLoc).equals(CalculatorUI.ID.SUB.numValue)) {
                String reduce = reduce(bracketEquation);
                String negTracker = "-";
                for (int n = 0; n < reduce.length(); n++) {
                    if (reduce.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                        negTracker = negTracker.equals("") ? "-" : "";
                    }
                }
                reduce = reduce.replace("-", "");

                if (negTracker.equals("-")) {
                    start = equation.substring(0, leftBLoc - 1) + negTracker + reduce;
                }else{
                    if (leftBLoc > 1 && equation.substring(leftBLoc - 2, leftBLoc - 1).matches(CalculatorUI.ID.NUM_VALUES)) {
                        start = equation.substring(0, leftBLoc - 1) + "+" + reduce;
                    }else {
                        start = equation.substring(0, leftBLoc - 1) + negTracker + reduce;
                    }
                }
            }else{
                start = equation.substring(0,leftBLoc) + reduce(bracketEquation);
            }
            equation = start + equation.substring(rightBLoc == equation.length() ? rightBLoc : rightBLoc + 1, equation.length());
        }else{
            CalculatorUI.ID[] order = new CalculatorUI.ID[]{CalculatorUI.ID.PERCENT, CalculatorUI.ID.SQROOT, CalculatorUI.ID.POW,CalculatorUI.ID.DIVIDE, CalculatorUI.ID.MULT, CalculatorUI.ID.SUB, CalculatorUI.ID.ADD};

            boolean contLoop = true;
            while (contLoop) {
                contLoop = false;
                for (CalculatorUI.ID id : order) {
                    if (isNum(equation)) break;

                    if (id.equals(CalculatorUI.ID.PERCENT) && equation.contains(id.numValue)) {
                        final String ADD = "ADD", SUB = "SUB", NULL = "NULL";

                        int i = equation.lastIndexOf(id.numValue);
                        int start = i - 1;
                        int end = i;
                        boolean finishedReading = false;
                        String operation = NULL;


                        for (int n = i - 1; n >= 0; n--) {
                            String prev = equation.substring(n, n + 1);

                            if (!finishedReading) {
                                if (prev.matches(CalculatorUI.ID.NUM_VALUES)) {
                                    start = n;
                                }else if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                    finishedReading = true;
                                }
                            }

                            if (finishedReading) {
                                if (prev.equals(CalculatorUI.ID.ADD.numValue)) {
                                    operation = ADD;
                                }else if (prev.equals(CalculatorUI.ID.SUB.numValue)) {
                                    if (n != 0 && (equation.substring(n - 1, n).matches(CalculatorUI.ID.NUM_VALUES) || !equation.substring(n - 1, n).equals(")"))) {
                                        operation = SUB;
                                    }
                                }
                                break;
                            }
                        }

                        String s;
                        if (operation.equals(SUB)) {
                            s = equation.substring(0, start - 1) + CalculatorUI.ID.MULT.numValue + "(1" + CalculatorUI.ID.SUB.numValue  + equation.substring(start,end) + CalculatorUI.ID.DIVIDE.numValue + "100)" + equation.substring(end + 1,equation.length());
                        }else if (operation.equals(ADD)) {
                            s = equation.substring(0, start - 1) + CalculatorUI.ID.MULT.numValue + "(1" + CalculatorUI.ID.ADD.numValue  + equation.substring(start,end) + CalculatorUI.ID.DIVIDE.numValue + "100)" + equation.substring(end + 1,equation.length());
                        }else {
                            s = equation.substring(0, start) + "(" + equation.substring(start,end) + CalculatorUI.ID.DIVIDE.numValue + "100)" + equation.substring(end + 1,equation.length());
                        }
                        Util.log(operation + " = " + s);
                        return s;
                    }
                    else if (id.equals(CalculatorUI.ID.SQROOT) && equation.contains(id.numValue)) {
                        int i = equation.lastIndexOf(CalculatorUI.ID.SQROOT.numValue);
                        if (i == equation.length() - 1) {
                            return ERROR;
                        } else {
                            boolean readNum = false;
                            int start = i + 1;
                            int end = equation.length();


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

                            String negTracker = "";
                            for (int n = 0; n < sq.length(); n++) {
                                if (sq.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                                    negTracker = negTracker.equals("") ? "-" : "";
                                }
                            }

                            String value;
                            sq = sq.replace(CalculatorUI.ID.SUB.numValue, "");

                            EQMath.Operation op = new EQMath.Operation();

                            if (op.newOP(negTracker + sq, + 0.5 + "")) {
                                return ERROR;
                            }
                            value = op.pow();
//                            Double num;

//                            try {
//                                num = parse(sq) * negTracker;
//                            } catch (Exception e) {
//                                return ERROR;
//                            }
//                            double d = Math.pow(num, 0.5);
//                            if (getError(d + "") != null) {
//                                value = getError(d + "");
//                            } else {
//                                value = BigDecimal.valueOf(d).toPlainString();
//                            }

                            equation = equation.substring(0, i) + value + equation.substring(end, equation.length());
                            contLoop = true;
                            break;
                        }
                    } else if (equation.contains(id.numValue)) {
                        int i = equation.indexOf(id.numValue);
                        if (id == CalculatorUI.ID.SUB) {
                            int loc = -1;
                            for (int p = 0; p < equation.length(); p++) {
                                if (equation.substring(p, p + 1).equals(CalculatorUI.ID.SUB.numValue)) {
                                    if (p == 0 || !equation.substring(p - 1, p).matches(CalculatorUI.ID.NUM_VALUES)) {
                                        continue;
                                    }else {
                                        loc = p;
                                        break;
                                    }
                                }
                            }
                            if (loc == -1) {
                                continue;
                            }else {
                                i = loc;
                            }
                        }
                        if (i == equation.length() - 1) {
                            return ERROR;
                        } else {
                            String num1, num2;

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

                            String negTracker = "";
                            for (int n = 0; n < sq.length(); n++) {
                                if (sq.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                                    negTracker = negTracker.equals("") ? "-" : "";
                                }
                            }
                            sq = sq.replace(CalculatorUI.ID.SUB.numValue, "");

                            num1 = negTracker + sq;

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

                            negTracker = "";
                            for (int n = 0; n < sq.length(); n++) {
                                if (sq.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
                                    negTracker = negTracker.equals("") ? "-" : "";
                                }
                            }
                            sq = sq.replace(CalculatorUI.ID.SUB.numValue, "");

                            num2 = negTracker + sq;

                            String value = null;

                            EQMath.Operation operation = new EQMath.Operation();

                            if (operation.newOP(num1, num2)) {
                                return ERROR;
                            }

                            try {
                                if (id == CalculatorUI.ID.POW) {
                                    value = operation.pow();
                                } else if (id == CalculatorUI.ID.ADD) {
                                    value = operation.add();
                                } else if (id == CalculatorUI.ID.SUB) {
                                    value = operation.subtract();
                                } else if (id == CalculatorUI.ID.MULT) {
                                    value = operation.mult();
                                } else if (id == CalculatorUI.ID.DIVIDE) {
                                    value = operation.divide();
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

    public static class Operation {
        Value num1, num2;

        public boolean newOP(String num1, String num2) {
            this.num1 = gen(num1);
            this.num2 = gen(num2);

            if (this.num1 == null || this.num2 == null) {
                return true;
            }
            return false;
        }
        public static Value gen(String num) {
            Value val = new Value();
            if (num.contains("/")) {
                try {
                    val.setNumerator(getVal((numerator(num))));
                    val.setDenominator(getVal((denominator(num))));
                }catch(Exception e) {
                    return null;
                }
            }else {
                try {
                    val.setNumerator(getVal((num)));
                    val.setDenominator(getVal(1));
                }catch(Exception e) {
                    return null;
                }
            }
            return val;
        }
        public String pow() {
            String max = max(Math.pow(derationalize(num1).getDoubleNumerator(),(derationalize(num2).getDoubleNumerator())));
            if (max != null) {
                return max;
            }

            boolean num2Neg = false;
            boolean maxNegative = false;
            String num1Neg = "";
            String numerDisplay;
            String denomDisplay = 1 + "";

            if (num2.getDoubleNumerator() < 0) {
                if (derationalize(num2).getDoubleNumerator() < -100) {
                    maxNegative = true;
                }
                num2Neg = true;
                num2.setNumerator(num2.getNumerator().multiply(getVal(-1)));
            }

            if (num1.getDoubleNumerator() < 0) {
                String d = derationalize(num2).numerator;
                if (hasDecimalValue(d)) {
                    return NAN;
                }
                num1Neg = (hasDecimalValue(derationalize(num2).getNumerator().divide(new Apfloat(2, PRECISION)).toString(true)) ? "-" : "");
                num1.setNumerator(num1.getNumerator().multiply(getVal(-1)));
            }else if (num1.getDoubleNumerator() == 0) return "1/1";

            if (maxNegative) return "0/1";

            String numerV = ApfloatMath.pow(num1.getNumerator(), num2.getNumerator().divide(num2.getDenominator())).toString(true);

            if (EquationHandler.getError(numerV + "") != null) {
                return EquationHandler.getError(numerV + "");
            }

            numerDisplay = numerV;

            if (!num1.isRational()) {
                String denomV = ApfloatMath.pow(num1.getDenominator(), num2.getNumerator().divide(num2.getDenominator())).toString(true);

                if (EquationHandler.getError(denomV + "") != null) {
                    return EquationHandler.getError(denomV + "");
                }

                denomDisplay = denomV;
            }
            String s = num1Neg + (num2Neg ? denomDisplay + "/" + numerDisplay : numerDisplay + "/" + denomDisplay);
            return s;
        }
        public String mult() {
            String max = max(derationalize(num1).getDoubleNumerator() * (derationalize(num2).getDoubleNumerator()));
            if (max != null) {
                return max;
            }

            Value newVal = new Value();
            newVal.setNumerator(num1.getNumerator().multiply(num2.getNumerator()));
            newVal.setDenominator(num1.getDenominator().multiply(num2.getDenominator()));

            if (newVal.isRational()) {
                return derationalize(newVal).getNumerator().toString(true);
            }else{
                return newVal.getNumerator() + "/" + newVal.getDenominator();
            }

        }
        public String divide() {
            if (num2.getDoubleNumerator() == 0) {
                double val = (derationalize(num1).getDoubleNumerator() / derationalize(num2).getDoubleNumerator());
                return EquationHandler.getError( val + "");
            }
            String max = max(derationalize(num1).getDoubleNumerator() / derationalize(num2).getDoubleNumerator());
            if (max != null) {
                return max;
            }
            Value newVal = new Value();
            newVal.setNumerator(num1.getNumerator().multiply(num2.getDenominator()));
            newVal.setDenominator(num1.getDenominator().multiply(num2.getNumerator()));

            if (newVal.isRational()) {
                return derationalize(newVal).getNumerator().toString(true);
            }else{
                return newVal.getNumerator() + "/" + newVal.getDenominator();
            }
        }
        public String add() {
            String max = max(derationalize(num1).getDoubleNumerator() + (derationalize(num2).getDoubleNumerator()));
            if (max != null) {
                return max;
            }

            Value newVal = new Value();
            if (num1.getDenominator().equals(num2.getDenominator())) {
                newVal.setNumerator(num1.getNumerator().add(num2.getNumerator()));
                newVal.setDenominator(num1.getDenominator());
            }else{
                newVal.setNumerator(num1.getNumerator().multiply(num2.getDenominator()).add(num2.getNumerator().multiply(num1.getDenominator())));
                newVal.setDenominator(num1.getDenominator().multiply(num2.getDenominator()));
            }



            if (newVal.isRational()) {
                return derationalize(newVal).getNumerator().toString(true);
            }else{
                return newVal.getNumerator() + "/" + newVal.getDenominator();
            }
        }
        public String subtract() {
            num2.setNumerator(num2.getNumerator().multiply(getVal(-1)));
            String add = add();
            return add;
        }
        public static Value derationalize(Value num) {
            Value v = new Value();

            v.setNumerator(ApfloatMath.round(num.getNumerator().divide(num.getDenominator()), PRECISION / 2, RoundingMode.HALF_EVEN));
            v.setDenominator(getVal(1));
            return v;
        }

        public String max(double d) {
            if (d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY) return d + "";
            return null;
        }

        public static String numerator(String num) {
            return num.substring(0,num.indexOf("/"));
        }
        public static String denominator(String num) {
            return num.substring(num.indexOf("/") + 1, num.length());
        }
    }

    public static boolean hasDecimalValue(String s) {
        if (s.contains(".")) {
            for (int i = s.indexOf(".") + 1; i < s.length(); i++) {
                if (!s.substring(i, i + 1).equals("0")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Num getVal(String s) {
        return new Num(s);
    }
    public static Num getVal(double d) {
        return getVal(d + "");
    }

    public static class Value {
        private String numerator,denominator;

        public Value() {
            this.numerator = "1";
            this.denominator = "1";
        }

        public double getDoubleNumerator() {
            return Double.parseDouble(numerator);
        }
        public double getDoubleDenominator() {
            return Double.parseDouble(denominator);
        }
        public Apfloat getNumerator() {
            return getVal(numerator);
        }

        public void setNumerator(Apfloat numerator) {
            this.numerator = numerator.toString(true);
            vaildateNegatives();
        }

        public Apfloat getDenominator() {
            return getVal(denominator);
        }

        public void vaildateNegatives() {
            if (getDoubleDenominator() < 0) {
                numerator = (getNumerator().multiply(getVal(-1))).toString(true);
                denominator = (getDenominator().multiply(getVal(-1))).toString(true);
            }
        }
        public void setDenominator(Apfloat denominator) {
            this.denominator = denominator.toString(true);
            vaildateNegatives();
        }

        public boolean isRational() {
            if (getDoubleDenominator() == 1) return true;

            try {
                getNumerator().precision(0).divide(getDenominator().precision(0));
                return true;
            }catch(Exception e) {
                return false;
            }
        }
    }
    public static class Num extends Apfloat {

        @Override
        public String toString() {
            return this.toString(true);
        }

        public Num(String num) {
            super(num, PRECISION);
        }
        public Num(Apfloat a) {
            this(a.toString(true));
        }
    }
}
