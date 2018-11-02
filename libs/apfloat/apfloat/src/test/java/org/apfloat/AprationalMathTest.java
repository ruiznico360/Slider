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
package org.apfloat;

import java.math.RoundingMode;

import junit.framework.TestSuite;

/**
 * @version 1.7.0
 * @author Mikko Tommila
 */

public class AprationalMathTest
    extends ApfloatTestCase
{
    public AprationalMathTest(String methodName)
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

        suite.addTest(new AprationalMathTest("testIntegerPow"));
        suite.addTest(new AprationalMathTest("testScale"));
        suite.addTest(new AprationalMathTest("testAbs"));
        suite.addTest(new AprationalMathTest("testRound"));
        suite.addTest(new AprationalMathTest("testCopySign"));
        suite.addTest(new AprationalMathTest("testNegate"));
        suite.addTest(new AprationalMathTest("testProduct"));
        suite.addTest(new AprationalMathTest("testSum"));

        return suite;
    }

    public static void testIntegerPow()
    {
        Aprational x = new Aprational("2");
        assertEquals("2^30", new Aprational(new Apint(1 << 30)), AprationalMath.pow(x, 30));
        assertEquals("2^60", new Aprational(new Apint(1L << 60)), AprationalMath.pow(x, 60));
        assertEquals("2^-1", new Aprational("1/2"), AprationalMath.pow(x, -1));
        assertEquals("2^-3", new Aprational("1/8"), AprationalMath.pow(x, -3));
        assertEquals("2^0", new Aprational("1"), AprationalMath.pow(x, 0));

        try
        {
            AprationalMath.pow(new Aprational(new Apint(0)), 0);
            fail("0^0 accepted");
        }
        catch (ArithmeticException ae)
        {
            // OK; result would be undefined
        }
    }

    public static void testScale()
    {
        Aprational x = AprationalMath.scale(new Aprational("3/7"), 5);
        assertEquals("1 string", "300000/7", x.toString());

        x = AprationalMath.scale(new Aprational("-7/9"), -1);
        assertEquals("-1 string", "-7/90", x.toString());

        x = AprationalMath.scale(new Apfloat("1e" + 0x4000000000000000L, 1, 2).truncate(), Long.MIN_VALUE);
        assertEquals("scale min", new Apfloat("1e-" + 0x4000000000000000L, 1, 2), x);
    }

    public static void testRound()
    {
        Apfloat x = AprationalMath.round(new Aprational("3/2"), 1, RoundingMode.UP);
        assertEquals("3/2 UP", "2", x.toString());

        x = AprationalMath.round(new Aprational("3/2"), 1, RoundingMode.DOWN);
        assertEquals("3/2 DOWN", "1", x.toString());

        x = AprationalMath.round(new Aprational("10/2", 3), 1, RoundingMode.HALF_EVEN);
        assertEquals("3/2 base 3 EVEN", "2", x.toString());

        x = AprationalMath.round(new Aprational("12/2", 3), 1, RoundingMode.HALF_EVEN);
        assertEquals("5/2 base 3 EVEN", "2", x.toString());

        x = AprationalMath.round(new Aprational("-10/2", 3), 1, RoundingMode.HALF_EVEN);
        assertEquals("-3/2 base 3 EVEN", "-2", x.toString());

        x = AprationalMath.round(new Aprational("-12/2", 3), 1, RoundingMode.HALF_EVEN);
        assertEquals("-5/2 base 3 EVEN", "-2", x.toString());
    }

    public static void testAbs()
    {
        Aprational x = new Aprational("2/3");
        assertEquals("2/3", new Aprational("2/3"), AprationalMath.abs(x));

        x = new Aprational("-2/3");
        assertEquals("-2/3", new Aprational("2/3"), AprationalMath.abs(x));

        x = new Aprational("0");
        assertEquals("0", new Aprational("0"), AprationalMath.abs(x));
    }

    public static void testCopySign()
    {
        assertEquals("2/3, 1/2", new Aprational("2/3"), AprationalMath.copySign(new Aprational("2/3"), new Aprational("1/2")));
        assertEquals("2/3, -1/2", new Aprational("-2/3"), AprationalMath.copySign(new Aprational("2/3"), new Aprational("-1/2")));
        assertEquals("-2/3, 1/2", new Aprational("2/3"), AprationalMath.copySign(new Aprational("-2/3"), new Aprational("1/2")));
        assertEquals("-2/3, -1/2", new Aprational("-2/3"), AprationalMath.copySign(new Aprational("-2/3"), new Aprational("-1/2")));

        assertEquals("0, 0", new Aprational("0"), AprationalMath.copySign(new Aprational("0"), new Aprational("0")));
        assertEquals("0, 1/2", new Aprational("0"), AprationalMath.copySign(new Aprational("0"), new Aprational("1/2")));
        assertEquals("0, -1/2", new Aprational("0"), AprationalMath.copySign(new Aprational("0"), new Aprational("-1/2")));
        assertEquals("1/2, 0", new Aprational("0"), AprationalMath.copySign(new Aprational("1/2"), new Aprational("0")));
        assertEquals("-1/2, 0", new Aprational("0"), AprationalMath.copySign(new Aprational("-1/2"), new Aprational("0")));
    }

    @SuppressWarnings("deprecation")
    public static void testNegate()
    {
        Aprational x = new Aprational("2/3");
        assertEquals("2/3", new Aprational("-2/3"), AprationalMath.negate(x));

        x = new Aprational("-2/3");
        assertEquals("-2/3", new Aprational("2/3"), AprationalMath.negate(x));

        x = new Aprational("0");
        assertEquals("0", new Aprational("0"), AprationalMath.negate(x));
    }

    public static void testProduct()
    {
        Aprational a = AprationalMath.product(new Aprational("2/3"), new Aprational("4/5"));
        assertEquals("2/3,4/5 value", new Aprational("8/15"), a);

        a = AprationalMath.product(new Aprational("2"), new Aprational("3/5"), new Aprational("7/11"));
        assertEquals("2,3/5,7/9 value", new Aprational("42/55"), a);

        a = AprationalMath.product(Aprational.ZERO, new Aprational("12345"));
        assertEquals("0 value", new Aprational("0"), a);

        Aprational[] x = new Aprational[] { new Aprational("1000000000000"), new Aprational("1") };
        AprationalMath.product(x);
        assertEquals("Array product 1 [0]", new Aprational("1000000000000"), x[0]);
        assertEquals("Array product 1 [1]", new Aprational("1"), x[1]);

        x = new Aprational[] { new Aprational("1"), new Aprational("1000000000000") };
        AprationalMath.product(x);
        assertEquals("Array product 2 [0]", new Aprational("1"), x[0]);
        assertEquals("Array product 2 [1]", new Aprational("1000000000000"), x[1]);

        assertEquals("Empty product", new Aprational("1"), AprationalMath.product());
    }

    public static void testSum()
    {
        Aprational a = AprationalMath.sum(new Aprational("1/2"), new Aprational("2/3"));
        assertEquals("1/2,2/3 value", new Aprational("7/6"), a);

        a = AprationalMath.sum(new Aprational("2/3"), new Aprational("3/4"), new Aprational("4/5"));
        assertEquals("2/3,3/4,4/5 value", new Aprational("133/60"), a);

        a = AprationalMath.sum(new Aprational("0"), new Aprational("12345"));
        assertEquals("0-0 value", new Aprational("12345"), a);

        a = AprationalMath.sum(new Aprational("2"));
        assertEquals("2 value", new Aprational("2"), a);

        a = AprationalMath.sum(new Aprational("2"), new Aprational("222"), new Aprational("22"));
        assertEquals("2 222 22 value", new Aprational("246"), a);

        Aprational[] x = new Aprational[] { new Aprational("1000000000000"), new Aprational("1") };
        AprationalMath.sum(x);
        assertEquals("Array sum 1 [0]", new Aprational("1000000000000"), x[0]);
        assertEquals("Array sum 1 [1]", new Aprational("1"), x[1]);

        x = new Aprational[] { new Aprational("1"), new Aprational("1000000000000") };
        AprationalMath.sum(x);
        assertEquals("Array sum 2 [0]", new Aprational("1"), x[0]);
        assertEquals("Array sum 2 [1]", new Aprational("1000000000000"), x[1]);

        assertEquals("Empty sum", new Aprational("0"), AprationalMath.sum());
    }
}
