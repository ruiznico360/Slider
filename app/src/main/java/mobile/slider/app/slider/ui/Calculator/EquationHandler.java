package mobile.slider.app.slider.ui.Calculator;

import java.math.BigDecimal;
import java.math.MathContext;

import mobile.slider.app.slider.util.Util;

public class EquationHandler {
    public static final int MAX_DIGITS = 10, MAX_LENGTH = 100;
    public static final MathContext MC = MathContext.DECIMAL128;
    public static final String ERROR = "ERROR",POS_INFINITY = Double.POSITIVE_INFINITY + "", NEG_INFINITY = Double.NEGATIVE_INFINITY + "", NAN = Double.NaN + "";

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

    public static String answerValue(String calculation, String prevAnsDisplay, String prevAnsValue) {
        String answer = calculation;

        if (!prevAnsDisplay.equals("")) {
            answer = answer.replace(prevAnsDisplay, prevAnsValue);
        }
        Util.log("ANS " + answer);

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

        return answer;
    }
    public static String simplifyAns(String answer) {
        if (getError(answer) != null) {
            return getError(answer);
        }

        answer = new Operation().derationalize(new Operation().gen(answer)).numerator.toPlainString();

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
            CalculatorUI.ID[] order = new CalculatorUI.ID[]{CalculatorUI.ID.SQROOT, CalculatorUI.ID.POW,CalculatorUI.ID.DIVIDE, CalculatorUI.ID.MULT, CalculatorUI.ID.SUB, CalculatorUI.ID.ADD};

            boolean contLoop = true;
            while (contLoop) {
                contLoop = false;
                for (CalculatorUI.ID id : order) {
                    if (isNum(equation)) break;
                    if (equation.contains(CalculatorUI.ID.SQROOT.numValue)) {
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

                            Operation op = new Operation();

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

//                            boolean negateFix = true;
//                            int indexOfNum = equation.length();
//                            if (equation.substring(0, 1).equals(CalculatorUI.ID.SUB.numValue)) {
//                                for (int p = 0; p < equation.length(); p++) {
//                                    String s = equation.substring(p, p + 1);
//                                    if (s.matches(CalculatorUI.ID.NUM_VALUES) || s.equals(".")) {
//                                        if (p - indexOfNum > 1) {
//                                            negateFix = false;
//                                            break;
//                                        }
//                                        indexOfNum = p;
//                                    }
//                                }
//                            }else{
//                                negateFix = false;
//                            }
//                            if (negateFix) {
//                                String negTracker = "";
//                                for (int n = 0; n < equation.length(); n++) {
//                                    if (equation.charAt(n) == CalculatorUI.ID.SUB.numValue.charAt(0)) {
//                                        negTracker = negTracker.equals("") ? "-" : "";
//                                    }
//                                }
//                                equation = equation.replace(CalculatorUI.ID.SUB.numValue, "");
//                                return negTracker + equation;
//                            }
//                            for (int p = 0; p < equation.length(); p++) {
//                                Util.log(i);
//                                if (equation.substring(p, p + 1).equals(CalculatorUI.ID.SUB.numValue)) {
//                                    if (p == 0 || !equation.substring(p - 1, p).matches(CalculatorUI.ID.NUM_VALUES)) {
//
//                                    }else{
//                                        i = p;
//                                        break;
//                                    }
//                                }
//                            }
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

                            Operation operation = new Operation();

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
    public static boolean isNum(String num) {
        if (num.equals("")) return true;
        try {
            if (num.contains("/")) {
                Operation o = new Operation();
                Double.parseDouble(o.numerator(num));
                Double.parseDouble(o.denominator(num));
            }else{
                Double.parseDouble(num);
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
        public Value gen(String num) {
            Value val = new Value();
            if (num.contains("/")) {
                try {
                    val.setNumerator(BigDecimal.valueOf(parse(numerator(num))));
                    val.setDenominator(BigDecimal.valueOf(parse(denominator(num))));
                }catch(Exception e) {
                    return null;
                }
            }else {
                try {
                    val.setNumerator(BigDecimal.valueOf(parse(num)));
                    val.setDenominator(BigDecimal.valueOf(1));
                }catch(Exception e) {
                    return null;
                }
            }
            return val;
        }
        public String pow() {
            boolean neg = false;
            String numerDisplay;
            String valuee;
            String denomDisplay = 1 + "";

            if (num2.getNumerator().doubleValue() < 0) {
                neg = true;
                num2.setNumerator(num2.getNumerator().multiply(BigDecimal.valueOf(-1)));
            }
            num2 = derationalize(num2);


            double numerV = Math.pow(num1.getNumerator().doubleValue(), num2.getNumerator().doubleValue());

            if (getError(numerV + "") != null) {
                return getError(numerV + "");
            }

            numerDisplay = BigDecimal.valueOf(numerV).toPlainString();

            if (!num1.isRational()) {
                double denomV = Math.pow(num1.getDenominator().doubleValue(), num2.getNumerator().doubleValue());

                if (getError(denomV + "") != null) {
                    return getError(denomV + "");
                }

                denomDisplay = BigDecimal.valueOf(denomV).toPlainString();
            }
            return neg ? denomDisplay + "/" + numerDisplay : numerDisplay + "/" + denomDisplay;
        }
        public String mult() {
            Value newVal = new Value();
            newVal.setNumerator(num1.getNumerator().multiply(num2.getNumerator()));
            newVal.setDenominator(num1.getDenominator().multiply(num2.getDenominator()));

            if (newVal.isRational()) {
                return derationalize(newVal).getNumerator().toPlainString();
            }else{
                return newVal.getNumerator() + "/" + newVal.getDenominator();
            }

        }
        public String divide() {

            if (num2.getNumerator().doubleValue() == 0) {
                double val = (derationalize(num1).getNumerator().doubleValue() / derationalize(num2).getNumerator().doubleValue());
                return getError( val + "");
            }
            Value newVal = new Value();
            newVal.setNumerator(num1.getNumerator().multiply(num2.getDenominator()));
            newVal.setDenominator(num1.getDenominator().multiply(num2.getNumerator()));

            if (newVal.isRational()) {
                return derationalize(newVal).getNumerator().toPlainString();
            }else{
                return newVal.getNumerator() + "/" + newVal.getDenominator();
            }
        }
        public String add() {
            Value newVal = new Value();
            if (num1.getDenominator().equals(num2.getDenominator())) {
                newVal.setNumerator(num1.getNumerator().add(num2.getNumerator()));
                newVal.setDenominator(num1.getDenominator());
            }else{
                newVal.setNumerator(num1.getNumerator().multiply(num2.getDenominator()).add(num2.getNumerator().multiply(num1.getDenominator())));
                newVal.setDenominator(num1.getDenominator().multiply(num2.getDenominator()));
            }
            if (newVal.isRational()) {
                return derationalize(newVal).getNumerator().toPlainString();
            }else{
                return newVal.getNumerator() + "/" + newVal.getDenominator();
            }
        }
        public String subtract() {
            num2.setNumerator(num2.getNumerator().multiply(BigDecimal.valueOf(-1)));
            String add = add();
            return add;
        }
        public static Value derationalize(Value num) {
            Value v = new Value();
            v.setNumerator(num.getNumerator().divide(num.getDenominator(), MC));
            v.setDenominator(BigDecimal.valueOf(1));
            return v;
        }
        public String numerator(String num) {
            return num.substring(0,num.indexOf("/"));
        }
        public String denominator(String num) {
            return num.substring(num.indexOf("/") + 1, num.length());
        }
    }
    public static class Value {
        private BigDecimal numerator,denominator;

        public Value() {
            this.numerator = BigDecimal.valueOf(1);
            this.denominator = BigDecimal.valueOf(1);
        }
        public BigDecimal getNumerator() {
            return numerator;
        }

        public void setNumerator(BigDecimal numerator) {
            this.numerator = numerator;
            vaildateNegatives();
        }

        public BigDecimal getDenominator() {
            return denominator;
        }

        private void vaildateNegatives() {
            if (denominator.doubleValue() < 0) {
                numerator = (numerator.multiply(BigDecimal.valueOf(-1)));
                denominator = (denominator.multiply(BigDecimal.valueOf(-1)));
            }
        }
        public void setDenominator(BigDecimal denominator) {
            this.denominator = denominator;
            vaildateNegatives();
        }

        public boolean isRational() {
            if (denominator.doubleValue() == 1) return true;

            try {
                numerator.divide(denominator, MathContext.UNLIMITED);

//                Value v = Operation.derationalize(this);
//                if (v.getNumerator().toPlainString().contains(".") && v.getNumerator().toPlainString().length() - v.getNumerator().toPlainString().indexOf("."))
                return true;
            }catch(Exception e) {
                return false;
            }
        }
    }
}
