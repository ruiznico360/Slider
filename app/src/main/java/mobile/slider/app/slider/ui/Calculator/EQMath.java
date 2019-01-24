package mobile.slider.app.slider.ui.Calculator;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import mobile.slider.app.slider.util.Util;

import static mobile.slider.app.slider.ui.Calculator.EquationHandler.BD_SCALE;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.ERROR;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.NAN;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.POS_INFINITY;
import static mobile.slider.app.slider.ui.Calculator.EquationHandler.isNum;

public class EQMath {
    public static final int PRECISION = BD_SCALE + 10;
    public static final String RAW_NEG = "_";
    public static final String NEG = CalculatorUI.ID.SUB.numValue;

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
            equation = equation.substring(0,leftBLoc) + reduce(bracketEquation) + equation.substring(rightBLoc == equation.length() ? rightBLoc : rightBLoc + 1, equation.length());
        }else{
            CalculatorUI.ID[] order = new CalculatorUI.ID[]{CalculatorUI.ID.PERCENT, CalculatorUI.ID.SQROOT, CalculatorUI.ID.POW,CalculatorUI.ID.DIVIDE, CalculatorUI.ID.MULT, CalculatorUI.ID.SUB, CalculatorUI.ID.ADD};

            if (equation.substring(0,1).equals(NEG)) {
                equation = RAW_NEG + "1" + CalculatorUI.ID.MULT.numValue + equation.substring(1, equation.length());
            }
            boolean contLoop = true;
            while (contLoop) {
                contLoop = false;
                for (CalculatorUI.ID id : order) {
                    if (isNum(equation)) break;

                    if (equation.contains(id.numValue)) {
                        if (id.equals(CalculatorUI.ID.PERCENT)) {
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

                                        if (prev.equals(RAW_NEG)) break;
                                    }else if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                        finishedReading = true;
                                    }
                                }

                                if (finishedReading) {
                                    if (prev.equals(CalculatorUI.ID.ADD.numValue)) {
                                        operation = ADD;
                                    }else if (prev.equals(CalculatorUI.ID.SUB.numValue)) {
                                        if (n != 0) {
                                            operation = SUB;
                                        }

//                                    if (n != 0 && (equation.substring(n - 1, n).matches(CalculatorUI.ID.NUM_VALUES) || !equation.substring(n - 1, n).equals(")"))) {
//                                        operation = SUB;
//                                    }
                                    }
                                    break;
                                }
                            }

                            String s;
                            if (operation.equals(SUB)) {
                                s = equation.substring(0, start - 1) + CalculatorUI.ID.MULT.numValue + "(1" + CalculatorUI.ID.SUB.numValue + equation.substring(start,end) + CalculatorUI.ID.DIVIDE.numValue + "100)" + equation.substring(end + 1,equation.length());
                            }else if (operation.equals(ADD)) {
                                s = equation.substring(0, start - 1) + CalculatorUI.ID.MULT.numValue + "(1" + CalculatorUI.ID.ADD.numValue  + equation.substring(start,end) + CalculatorUI.ID.DIVIDE.numValue + "100)" + equation.substring(end + 1,equation.length());
                            }else {
                                s = equation.substring(0, start) + "(" + equation.substring(start,end) + CalculatorUI.ID.DIVIDE.numValue + "100)" + equation.substring(end + 1,equation.length());
                            }
                            return s;
                        }else if (id.equals(CalculatorUI.ID.SQROOT)) {
                            int i = equation.lastIndexOf(CalculatorUI.ID.SQROOT.numValue);
                            if (i == equation.length() - 1) {
                                return ERROR;
                            } else {
                                int start = i + 1;
                                int end = equation.length();


                                for (int n = i + 1; n < equation.length(); n++) {
                                    String prev = equation.substring(n, n + 1);
                                    if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                        end = n;
                                        break;
                                    }
                                }

                                String sq = equation.substring(start, end);
                                String value;

                                EQMath.Operation op = new EQMath.Operation();

                                if (op.newOP(sq, 0.5 + "")) {
                                    return ERROR;
                                }
                                value = op.pow();

                                equation = equation.substring(0, i) + value + equation.substring(end, equation.length());
                                contLoop = true;
                                break;
                            }
                        }else {
                            int i = equation.indexOf(id.numValue);
                            if (i == equation.length() - 1) {
                                return ERROR;
                            } else {
                                String num1, num2;

                                int end = i;
                                int start = 0;
                                int trueStart = 0, trueEnd = 0;

                                for (int n = i - 1; n >= 0; n--) {
                                    String prev = equation.substring(n, n + 1);
                                    if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                        start = n + 1;
                                        break;
                                    }
                                }

                                trueStart = start;
                                num1 = equation.substring(start, end);

                                start = i + 1;
                                end = equation.length();


                                for (int n = i + 1; n < equation.length(); n++) {
                                    String prev = equation.substring(n, n + 1);
                                    if ((!prev.matches(CalculatorUI.ID.NUM_VALUES) && !prev.equals("."))) {
                                        end = n;
                                        break;
                                    }
                                }

                                trueEnd = end;
                                num2 = equation.substring(start, end);

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
        }
        return equation;
    }

    public static String simplifyNotation(String s) {
        if (s.contains("e")) {
            if (!s.contains(".")) s = s.substring(0,s.indexOf("e")) + ".0" + s.substring(s.indexOf("e"),s.length());
            boolean pos;
            int zeros;
            if (s.substring(s.indexOf("e") + 1, s.indexOf("e") + 2).equals("-")) {
                pos = false;
                zeros = Integer.parseInt(s.substring(s.indexOf("e") + 2, s.length()));
            }else{
                pos = true;
                zeros = Integer.parseInt(s.substring(s.indexOf("e") + 1, s.length()));
            }

            int index = s.indexOf(".");
            s = s.substring(0,s.indexOf("e")).replace(".","");

            if (pos) {
                for (int i = 0; i < zeros; i++) {
                    if (index == s.length()) s += "0";
                    index++;
                }
                s = s.substring(0,index) + "." + s.substring(index,s.length());
            }else{
                for (int i = 0; i < zeros; i++) {
                    if (index <= 0) s = "0" + s;
                    index--;
                }
                s = s.substring(0,index > 0 ? index : 0) + "." + s.substring(index > 0 ? index : 0,s.length());
            }
            return s;
        }else{
            return s;
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

    public static boolean isNegative(String num) {
        return num.substring(0,1).equals(NEG);
    }

    public static class Operation {
        Value num1, num2;

        public boolean newOP(String num1, String num2) {
            this.num1 = Value.gen(num1);
            this.num2 = Value.gen(num2);

            if (this.num1 == null || this.num2 == null) {
                return true;
            }
            return false;
        }

        public String pow() {

            double maxD = Math.pow(num1.getNumerator().divide(num1.getDenominator()).doubleValue(), num2.getNumerator().divide(num2.getDenominator()).doubleValue());

            String max = max(maxD);
            if (max != null) {
                return max;
            }

            boolean num2Neg = false;
            String num1Neg = "";
            String numerDisplay;
            String denomDisplay;

            if (isNegative(num2.numerator)) {
                num2Neg = true;
                num2.setNumerator(num2.getNumerator().multiply(getVal(-1)));
            }
            Apfloat pow = num2.getNumerator().divide(num2.getDenominator());

            if (isNegative(num1.numerator)) {
                String d = num2.derationalize().numerator;
                if (hasDecimalValue(d)) {
                    return NAN;
                }
                num1Neg = (hasDecimalValue(num2.derationalize().getNumerator().divide(getVal(2)).toString(true)) ? "-" : "");
                num1.setNumerator(num1.getNumerator().multiply(getVal(-1)));
            }else if (num1.getDoubleNumerator() == 0) return "1/1";

            Apfloat a = ApfloatMath.pow(num1.getNumerator(), pow);
            Apfloat b = ApfloatMath.pow(num1.getDenominator(), pow);

            if (a.doubleValue() == Double.POSITIVE_INFINITY || b.doubleValue() == Double.POSITIVE_INFINITY) {
                numerDisplay = ApfloatMath.pow(num1.getNumerator().divide(num1.getDenominator()), pow).toString(true);
                denomDisplay = "1";
            }else {
                numerDisplay = a.toString(true);
                denomDisplay = b.toString(true);
            }
            Value newVal = Value.gen(num1Neg + (num2Neg ? (denomDisplay) + "/" + (numerDisplay) : (numerDisplay) + "/" + (denomDisplay)));

            if (newVal.isRational()) {
                return newVal.derationalize().rawNumerator();
            }

            return newVal.rawValue();
        }
        public String mult() {
            String max = max(num1.derationalize().getDoubleNumerator() * (num2.derationalize().getDoubleNumerator()));
            if (max != null) {
                return max;
            }

            Value newVal = new Value();
            newVal.setNumerator(num1.getNumerator().multiply(num2.getNumerator()));
            newVal.setDenominator(num1.getDenominator().multiply(num2.getDenominator()));

            if (newVal.isRational()) {
                return newVal.derationalize().rawNumerator();
            }

            return newVal.rawValue();


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



            if (newVal.isRational()) {
                return newVal.derationalize().rawNumerator();
            }

            return newVal.rawValue();

        }
        public String subtract() {
            num2.setNumerator(num2.getNumerator().multiply(getVal(-1)));
            String add = add();
            return add;
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

    public static Num getVal(String s) {
        if (s.substring(0,1).equals(RAW_NEG)) {
            s = s.replace(RAW_NEG, NEG);
        }
        return new Num(s);
    }
    public static Num getVal(double d) {
        return getVal(d + "");
    }

    public static class Value {
        public String numerator,denominator;

        public Value() {
            this.numerator = "1";
            this.denominator = "1";
        }

        public static Value gen(String num) {
            Value val = new Value();
            try {
                if (num.contains("/")) {
                    val.setNumerator(EQMath.getVal((Operation.numerator(num))));
                    val.setDenominator(EQMath.getVal((Operation.denominator(num))));
                }else {
                    val.setNumerator(getVal((num)));
                    val.setDenominator(getVal(1));
                }
            }catch(Exception e) {
                return null;
            }
            return val;
        }

        public double getDoubleNumerator() {
            return EquationHandler.parse(numerator);
        }
        public double getDoubleDenominator() {
            return EquationHandler.parse(denominator);
        }
        public Num getNumerator() {
            return getVal(numerator);
        }
        public Num getDenominator() {
            return getVal(denominator);
        }

        public String rawNumerator() {
            return (isNegative(numerator) ? RAW_NEG + numerator.substring(1,numerator.length()) : numerator);
        }
        public String rawDenominator() {
            return (isNegative(denominator) ? RAW_NEG + denominator.substring(1,denominator.length()) : denominator);
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

        public void vaildateNegatives() {
            if (isNegative(denominator)) {
                numerator = (getNumerator().multiply(getVal(-1))).toString(true);
                denominator = (getDenominator().multiply(getVal(-1))).toString(true);
            }
        }
        public String rawValue() {
            return rawNumerator() + "/" + rawDenominator();
        }

        public Value derationalize() {
            Value v = new Value();

            if (v.isRational()) {
                Apfloat numer = (getNumerator().divide(getDenominator()));

                if (numer.toString(true).replace(".","").length() >= PRECISION) {
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
