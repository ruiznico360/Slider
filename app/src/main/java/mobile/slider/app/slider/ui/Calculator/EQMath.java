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
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.PI;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.POS_INFINITY;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.isNum;

public class EQMath {
    public static final int PRECISION = BD_SCALE + 10;
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
                    if (leftBLoc > 1 && equation.substring(leftBLoc - 2, leftBLoc - 1).matches(CalculatorUI.ID.NUM_VALUES) || leftBLoc > 1 && equation.substring(leftBLoc - 2, leftBLoc - 1).equals(")")) {
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
                            Util.log(s);
                        }
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
                            }catch (Exception e) {
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
            Util.log("Commencing pow " + num1.getNumerator() + "/" + num1.getDenominator());
            String arg = num2.derationalize().numerator;
            double maxD = Math.pow(num1.derationalize().getDoubleNumerator(), EquationHandler.parse(arg));

            String max = max(maxD);
            if (max != null) {
                return max;
            }

            boolean num2Neg = false;
            String num1Neg = "";
            String numerDisplay;
            String denomDisplay = 1 + "";

            if (num2.getDoubleNumerator() < 0) {
                num2Neg = true;
                num2.setNumerator(num2.getNumerator().multiply(getVal(-1)));
            }

            if (num1.getDoubleNumerator() < 0) {
                String d = num2.derationalize().numerator;
                if (hasDecimalValue(d)) {
                    return NAN;
                }
                num1Neg = (hasDecimalValue(num2.derationalize().getNumerator().divide(getVal(2)).toString(true)) ? "-" : "");
                num1.setNumerator(num1.getNumerator().multiply(getVal(-1)));
            }else if (num1.getDoubleNumerator() == 0) return "1/1";

            Apfloat pow = num2.getNumerator().divide(num2.getDenominator());

            String numerV = (ApfloatMath.pow(num1.getNumerator(), pow)).toString(true);

            if (EquationHandler.getError(numerV + "") != null) {
                return EquationHandler.getError(numerV + "");
            }

            numerDisplay = numerV;

            String denomV = (ApfloatMath.pow(num1.getDenominator(), pow)).toString(true);

            if (EquationHandler.getError(denomV + "") != null) {
                return EquationHandler.getError(denomV + "");
            }

            denomDisplay = denomV;

            String s = num1Neg + (num2Neg ? (denomDisplay) + "/" + (numerDisplay) : (numerDisplay) + "/" + (denomDisplay));

            Util.log("pow " + s);
//            if (Operation.gen(s).isRational()) {
//                return (Operation.gen(s)).derationalize().numerator;
//            }
            return s;
        }
        public String mult() {
            String max = max(num1.derationalize().getDoubleNumerator() * (num2.derationalize().getDoubleNumerator()));
            if (max != null) {
                return max;
            }

            Value newVal = new Value();
            newVal.setNumerator(num1.getNumerator().multiply(num2.getNumerator()));
            newVal.setDenominator(num1.getDenominator().multiply(num2.getDenominator()));

            Util.log("Mult " + newVal.numerator +"/"+newVal.denominator + " " + num1.numerator + "/" + num1.denominator + " " + num2.numerator +"/" + num2.denominator);
//            if (newVal.isRational()) {
//                return newVal.derationalize().getNumerator().toString(true);
//            }
            return (newVal.getNumerator()) + "/" + (newVal.getDenominator());


        }
        public String divide() {
            if (num2.getDoubleNumerator() == 0) {
                double val = (num1.derationalize().getDoubleNumerator() / num2.derationalize().getDoubleNumerator());
                return EquationHandler.getError( val + "");
            }
            String temp = num2.numerator;
            num2.setNumerator(num2.denominator);
            num2.setDenominator(temp);
            return mult();
        }
        public String add() {
            String max = max(num1.derationalize().getDoubleNumerator() + num2.derationalize().getDoubleNumerator());
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



//            if (newVal.isRational()) {
//                return newVal.derationalize().getNumerator().toString(true);
//            }

            return (newVal.getNumerator()) + "/" + (newVal.getDenominator());

        }
        public String subtract() {
            num2.setNumerator(num2.getNumerator().multiply(getVal(-1)));
            String add = add();
            return add;
        }
        public static String cleanRound(Apfloat num) {
            return ApfloatMath.round(num, BD_SCALE, RoundingMode.HALF_EVEN).toString(true);
        }
        public static String round(Apfloat num) {
            return ApfloatMath.round(num, PRECISION - 5, RoundingMode.HALF_EVEN).toString(true);
        }
        public String max(double d) {
            if (d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY || d == 0) return d + "";
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
        public String numerator,denominator;

        public Value(int precision) {
            this.numerator = "1";
            this.denominator = "1";
        }
        public Value() {
            this.numerator = "1";
            this.denominator = "1";
        }

        public double getDoubleNumerator() {
            return EquationHandler.parse(numerator);
        }
        public double getDoubleDenominator() {
            return EquationHandler.parse(denominator);
        }
        public Apfloat getNumerator() {
            return getVal(numerator);
        }

        public void setNumerator(Apfloat numerator) {
            setNumerator(numerator.toString(true));

        }
        public void setDenominator(Apfloat denominator) {
            setDenominator(denominator.toString(true));
        }

        public void setNumerator(String numerator) {
            this.numerator = numerator;
            vaildateNegatives();
        }
        public void setDenominator(String denominator) {
            this.denominator = denominator;
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

        public Value derationalize() {
            Value v = new Value();

            if (v.isRational()) {
                Apfloat numer = (getNumerator().divide(getDenominator()));

                if (numer.toString(true).replace(".","").length() >= PRECISION) {
//                    int lastDigit = Integer.parseInt(numer.substring(numer.length() - 1));
//                    int numerDec = numer.contains(".") ? numer.indexOf(".") : numer.length() - 1;
//                    if (lastDigit >= 5) {
//                        numer = getVal(numer).add(ApfloatMath.pow(getVal(10), getVal(numerDec - (numer.length() - 1))).multiply(getVal(10 - lastDigit))).toString(true);
//                    }
//                    BigDecimal b = new BigDecimal("3").pow
//                    Util.log(numerator + "/" + denominator + " " + numer + " " + (ApfloatMath.round(getVal(numer), PRECISION - 1, RoundingMode.HALF_EVEN)));
                    Util.log( "der " + numer + " " + new Apfloat(Operation.round((numer))) + " " + numerator +"/" + denominator );
                    numer = new Apfloat(Operation.round((numer)));
                }

                v.setNumerator(numer);
                v.setDenominator(getVal(1));
            }else{
                v.setNumerator(getNumerator().divide(getDenominator()));
                v.setDenominator(getVal(1));
            }


            return v;
        }
        public boolean isRational() {
            if (getDoubleDenominator() == 1) return true;
            String s = getNumerator().divide(getDenominator()).multiply(getDenominator()).toString(true);
            int size = BD_SCALE + ((PRECISION - BD_SCALE) / 2);

//            if (size > numerator.length())
            size = numerator.length();

            Util.log( "Rational? " + numerator.substring(0, size) + " " +(s.substring(0,size)));

            return numerator.substring(0, size).equals(s.substring(0,size));
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
