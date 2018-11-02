/*
 * Apfloat arbitrary precision arithmetic library
 * Copyright (C) 2002-2017  Mikko Tommila
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.apfloat.calc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @version 1.8.1
 * @author Mikko Tommila
 */

public class CalculatorTest
    extends TestCase
{
    public CalculatorTest(String methodName)
    {
        super(methodName);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new CalculatorTest("testOperators"));
        suite.addTest(new CalculatorTest("testFunctions"));

        return suite;
    }

    private static void assertCalculation(String expected, String input)
        throws ParseException
    {
        String actual = runCalculation(input);
        assertEquals(input, expected + NEWLINE, actual);
    }

    private static String runCalculation(String input)
        throws ParseException
    {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        String actual;

        try
        {
            InputStream in = new ByteArrayInputStream(input.getBytes());
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buffer, true);
            System.setIn(in);
            System.setOut(out);

            Calculator.main(new String[0]);

            actual = new String(buffer.toByteArray());
        }
        finally
        {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }

        return actual;
    }

    private static void assertCalculationFailure(String input)
    {
        try
        {
            runCalculation(input);
            fail(input + " accepted");
        }
        catch (ParseException pe)
        {
            // OK: syntax error
        }
        catch (ArithmeticException ae)
        {
            // OK: result is not a number
        }
        catch (IllegalArgumentException iae)
        {
            // OK: invalid parameter
        }
        catch (org.apfloat.ApfloatRuntimeException are)
        {
            // OK: attempt invalid calculation
        }
    }

    public static void testOperators()
        throws ParseException
    {
        assertCalculation("163", "1+2*3^4");
        assertCalculation("77", "99-22");
        assertCalculation("7.7e1", "99.1-22.1");
        assertCalculation("i", "2i-i");
        assertCalculation("5" + NEWLINE + "11", "x = 5; x + 6");
        assertCalculation("2", "5 % 3");
        assertCalculation("2.1", "5.1 % 3");
        assertCalculation("2", "5 + -3");
        assertCalculation("7/6", "1/2 + 2/3");
        assertCalculation("5e-1", "1./2");
        assertCalculation("5e-1", ".5");
        assertCalculation("5e-1", ".5e0");
        assertCalculation("5e-1", "5e-1");
        assertCalculation("5e-1", "5.e-1");
        assertCalculation("5e1", "5e1");
        assertCalculation("6.66", "20.0/3.00");
        assertCalculation("-1", "I*I");
        assertCalculation("3+2i", "2I+3");
        assertCalculation("3-2i", "i*-2+3");
        assertCalculation("35", "((5))*(3+4)");
        assertCalculation("7", "+++++++++++++7");
        assertCalculation("8", "--8");
        assertCalculation("-9", "-----9");
        assertCalculation("1", " ;;; \t 1 ;;;;;;;;; ");
        assertCalculation("1.41421", "2.0000^0.50000");
        assertCalculation("8.8080652584e646456992", "2^2147483647.0");
        assertCalculation("-1", "i^2");
        assertCalculation("1", "1^2i");

        assertCalculationFailure("6%i");
        assertCalculationFailure("5e");
        assertCalculationFailure("2^^2");
        assertCalculationFailure("2a2");
        assertCalculationFailure("x=");
        assertCalculationFailure("*5");
        assertCalculationFailure("/");
        assertCalculationFailure("6%");
        assertCalculationFailure("2-");
        assertCalculationFailure("bogus^5");
    }

    public static void testFunctions()
        throws ParseException
    {
        assertCalculation("3", "abs(-3)");
        assertCalculation("3", "abs(-3i)");
        assertCalculation("5", "abs(3.00 + 4.00I)");
        assertCalculation("2", "cbrt(8)");
        assertCalculation("2/3", "cbrt(8/27)");
        assertCalculation("7.07e-1+7.07e-1i", "cbrt(-0.707+0.707i)");
        assertCalculation("1.2599", "cbrt(2.0000)");
        assertCalculation("2", "ceil(1.1)");
        assertCalculation("2", "ceil(3/2)");
        assertCalculation("-2", "copySign(2, -3)");
        assertCalculation("5040", "factorial(7)");
        assertCalculation("2", "floor(2.9)");
        assertCalculation("2", "floor(29/10)");
        assertCalculation("9e-1", "frac(2.9)");
        assertCalculation("9/10", "frac(29/10)");
        assertCalculation("2", "fmod(5, 3)");
        assertCalculation("5", "hypot(3, 4)");
        assertCalculation("5e-1", "hypot(0.3, 0.4)");
        assertCalculation("5e-1", "inverseRoot(2.0, 1)");
        assertCalculation("5e-1", "inverseRoot(4.0, 2)");
        assertCalculation("7.07e-1-7.07e-1i", "inverseRoot(-0.707+0.707i, 3)");
        assertCalculation("-5e-1", "inverseRoot(4.0, 2, 1)");
        assertCalculation("1.23", "n(1.23456, 3)");
        assertCalculation("2", "root(8, 3)");
        assertCalculation("2/3", "root(16/81, 4)");
        assertCalculation("7.07e-1+7.07e-1i", "root(-0.707+0.707i, 3)");
        assertCalculation("1.2599", "root(2.0000, 3)");
        assertCalculation("-4", "root(16.0, 2, 1)");
        assertCalculation("3", "round(2.9, 1)");
        assertCalculation("3", "round(29/10, 1)");
        assertCalculation("3", "round(2.5, 1)");
        assertCalculation("4", "round(3.5, 1)");
        assertCalculation("-3", "round(-2.5, 1)");
        assertCalculation("-4", "round(-3.5, 1)");
        assertCalculation("20000000000", "scale(2, 10)");
        assertCalculation("1/5", "scale(2, -1)");
        assertCalculation("2.1e10", "scale(2.1, 10)");
        assertCalculation("2.5e10+1.5e10i", "scale(2.5+1.5i, 10)");
        assertCalculation("200000/3", "scale(2/3, 5)");
        assertCalculation("3", "sqrt(9)");
        assertCalculation("2/3", "sqrt(4/9)");
        assertCalculation("7.07+7.07i", "sqrt(100.i)");
        assertCalculation("1.4142", "sqrt(2.0000)");
        assertCalculation("2", "truncate(2.5)");
        assertCalculation("2", "truncate(5/2)");
        assertCalculation("1.79e2", "toDegrees(3.14)");
        assertCalculation("1.57", "toRadians(90.0)");
        assertCalculation("3.14", "acos(-1.00)");
        assertCalculation("1", "acosh(1.55)");
        assertCalculation("1.57", "asin(1.00)");
        assertCalculation("0", "asinh(0)");
        assertCalculation("7.85e-1", "atan(1.00)");
        assertCalculation("0", "atanh(0)");
        assertCalculation("1.57", "atan2(1.00, 0)");
        assertCalculation("1", "cos(0)");
        assertCalculation("1.54", "cosh(1.00)");
        assertCalculation("8.414709848e-1", "sin(1.000000000)");
        assertCalculation("1.17", "sinh(1.00)");
        assertCalculation("1.55", "tan(1.00)");
        assertCalculation("9.64e-1", "tanh(2.00)");
        assertCalculation("2.718", "exp(1.000)");
        assertCalculation("1.79+3.14i", "log(-6.00)");
        assertCalculation("1.58", "log(3.00, 2.00)");
        assertCalculation("1.584", "log(3.000, 2)");
        assertCalculation("1.5849", "log(3, 2.0000)");
        assertCalculation("1.57", "arg(1.00i)");
        assertCalculation("2-i", "conj(2+i)");
        assertCalculation("1", "imag(2+i)");
        assertCalculation("2", "real(2+i)");
        assertCalculation("2.47468", "agm(2.000000, 3.000000)");
        assertCalculation("5.671e-1", "w(1.000)");
        assertCalculation("-3.181e-1+1.3372i", "w(-1.0000)");
        assertCalculation("-1.34284+5.24724i", "w(1.00000+i, 1)");
        assertCalculation("3", "gcd(15, 12)");
        assertCalculation("60", "lcm(15, 12)");
        assertCalculation("3.14159", "pi(6)");
        assertCalculation("5", "add(2, 3)");
        assertCalculation("-1", "subtract(2, 3)");
        assertCalculation("6", "multiply(2, 3)");
        assertCalculation("2/3", "divide(2, 3)");
        assertCalculation("-2", "negate(2)");
        assertCalculation("2", "mod(5, 3)");
        assertCalculation("8", "pow(2, 3)");

        assertCalculationFailure("cbrt(2)");
        assertCalculationFailure("cbrt()");
        assertCalculationFailure("cbrt(8, 2)");
        assertCalculationFailure("ceil(i)");
        assertCalculationFailure("ceil()");
        assertCalculationFailure("ceil(2, 2)");
        assertCalculationFailure("copySign(2i, -3)");
        assertCalculationFailure("copySign(2)");
        assertCalculationFailure("copySign(2, 2, 2)");
        assertCalculationFailure("factorial(0.5)");
        assertCalculationFailure("factorial(2i)");
        assertCalculationFailure("factorial(2/3)");
        assertCalculationFailure("factorial()");
        assertCalculationFailure("factorial(2, 2)");
        assertCalculationFailure("floor(i)");
        assertCalculationFailure("floor()");
        assertCalculationFailure("floor(2, 2)");
        assertCalculationFailure("frac(i)");
        assertCalculationFailure("frac()");
        assertCalculationFailure("frac(2, 2)");
        assertCalculationFailure("fmod(2, i)");
        assertCalculationFailure("fmod(2)");
        assertCalculationFailure("fmod(2, 2, 2)");
        assertCalculationFailure("hypot(2, i)");
        assertCalculationFailure("hypot(2)");
        assertCalculationFailure("hypot(2, 2, 2)");
        assertCalculationFailure("inverseRoot(2.0, i)");
        assertCalculationFailure("inverseRoot(2.0, 1/2)");
        assertCalculationFailure("inverseRoot(2, 2)");
        assertCalculationFailure("inverseRoot(2)");
        assertCalculationFailure("inverseRoot(2.0, 1, i)");
        assertCalculationFailure("inverseRoot(2.0, i, 1)");
        assertCalculationFailure("inverseRoot(2.0, i, i)");
        assertCalculationFailure("inverseRoot(2.0, 2, 2, 2)");
        assertCalculationFailure("n(1.23456, i)");
        assertCalculationFailure("n(1.23456, 0.5)");
        assertCalculationFailure("n(1.23456)");
        assertCalculationFailure("n(1.23456, 2, 2)");
        assertCalculationFailure("root(2, 2)");
        assertCalculationFailure("root(2)");
        assertCalculationFailure("root(2, i)");
        assertCalculationFailure("root(2, 1, i)");
        assertCalculationFailure("root(2, i, 1)");
        assertCalculationFailure("root(2, i, i)");
        assertCalculationFailure("root(4.0, 2, 2, 2)");
        assertCalculationFailure("round(2)");
        assertCalculationFailure("round(2, 0)");
        assertCalculationFailure("round(2, 0.5)");
        assertCalculationFailure("round(2, i)");
        assertCalculationFailure("round(2, 2, 2)");
        assertCalculationFailure("scale(1.23456, i)");
        assertCalculationFailure("scale(1.23456, 0.5)");
        assertCalculationFailure("scale(1.23456)");
        assertCalculationFailure("scale(1.23456, 2, 2)");
        assertCalculationFailure("sqrt(2)");
        assertCalculationFailure("sqrt()");
        assertCalculationFailure("sqrt(4, 2)");
        assertCalculationFailure("truncate(i)");
        assertCalculationFailure("truncate()");
        assertCalculationFailure("truncate(2, 2)");
        assertCalculationFailure("toDegrees(2)");
        assertCalculationFailure("toDegrees(i)");
        assertCalculationFailure("toDegrees()");
        assertCalculationFailure("toDegrees(2, 2)");
        assertCalculationFailure("toRadians(2)");
        assertCalculationFailure("toRadians(i)");
        assertCalculationFailure("toRadians()");
        assertCalculationFailure("toRadians(2, 2)");
        assertCalculationFailure("acos()");
        assertCalculationFailure("acos(1, 1)");
        assertCalculationFailure("acosh()");
        assertCalculationFailure("acosh(1, 1)");
        assertCalculationFailure("asin()");
        assertCalculationFailure("asin(1, 1)");
        assertCalculationFailure("asinh()");
        assertCalculationFailure("asinh(1, 1)");
        assertCalculationFailure("atan()");
        assertCalculationFailure("atan(1, 1)");
        assertCalculationFailure("atanh()");
        assertCalculationFailure("atanh(1, 1)");
        assertCalculationFailure("atan2(1)");
        assertCalculationFailure("atan2(i, i)");
        assertCalculationFailure("atan2(1, 1, 1)");
        assertCalculationFailure("cos()");
        assertCalculationFailure("cos(1, 1)");
        assertCalculationFailure("cosh()");
        assertCalculationFailure("cosh(1, 1)");
        assertCalculationFailure("sin()");
        assertCalculationFailure("sin(1, 1)");
        assertCalculationFailure("sinh()");
        assertCalculationFailure("sinh(1, 1)");
        assertCalculationFailure("tan()");
        assertCalculationFailure("tan(1, 1)");
        assertCalculationFailure("tanh()");
        assertCalculationFailure("tanh(1, 1)");
        assertCalculationFailure("exp()");
        assertCalculationFailure("exp(1, 1)");
        assertCalculationFailure("log()");
        assertCalculationFailure("log(1, 1, 1)");
        assertCalculationFailure("arg()");
        assertCalculationFailure("arg(1, 1)");
        assertCalculationFailure("conj()");
        assertCalculationFailure("conj(1, 1)");
        assertCalculationFailure("imag()");
        assertCalculationFailure("imag(1, 1)");
        assertCalculationFailure("real()");
        assertCalculationFailure("real(1, 1)");
        assertCalculationFailure("agm(1)");
        assertCalculationFailure("agm(1, 1, 1)");
        assertCalculationFailure("w(1, 1, 1)");
        assertCalculationFailure("w(1, i)");
        assertCalculationFailure("w(1, 1.5)");
        assertCalculationFailure("w(1, 2/3)");
        assertCalculationFailure("gcd(2, i)");
        assertCalculationFailure("gcd(2, 2/3)");
        assertCalculationFailure("gcd(1)");
        assertCalculationFailure("gcd(1, 1, 1)");
        assertCalculationFailure("lcm(2, i)");
        assertCalculationFailure("lcm(2, 2/3)");
        assertCalculationFailure("lcm(1)");
        assertCalculationFailure("lcm(1, 1, 1)");
        assertCalculationFailure("pi(i)");
        assertCalculationFailure("pi(0.5)");
        assertCalculationFailure("pi()");
        assertCalculationFailure("pi(1, 1)");
        assertCalculationFailure("add(2)");
        assertCalculationFailure("add(2, 2, 2)");
        assertCalculationFailure("subtract(2)");
        assertCalculationFailure("subtract(2, 2, 2)");
        assertCalculationFailure("multiply(2)");
        assertCalculationFailure("multiply(2, 2, 2)");
        assertCalculationFailure("divide(2)");
        assertCalculationFailure("divide(2, 2, 2)");
        assertCalculationFailure("negate()");
        assertCalculationFailure("negate(2, 2)");
        assertCalculationFailure("mod(2, i)");
        assertCalculationFailure("mod(2)");
        assertCalculationFailure("mod(2, 2, 2)");
        assertCalculationFailure("pow(2)");
        assertCalculationFailure("pow(2, 2, 2)");
        assertCalculationFailure("bogusfunc(5)");
    }

    private static final String NEWLINE = System.getProperty("line.separator");
}
