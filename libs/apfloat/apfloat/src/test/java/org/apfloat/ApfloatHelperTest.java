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

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @version 1.4
 * @author Mikko Tommila
 */

public class ApfloatHelperTest
    extends TestCase
{
    public ApfloatHelperTest(String methodName)
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

        suite.addTest(new ApfloatHelperTest("testGetMatchingPrecisions2"));
        suite.addTest(new ApfloatHelperTest("testGetMatchingPrecisions4"));
        suite.addTest(new ApfloatHelperTest("testSetPrecision"));

        return suite;
    }

    public static void testGetMatchingPrecisions2()
    {
        long[] precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(12345678, 5), new Apfloat(12345, 5));

        assertEquals("5-2 [0]", 5, precisions[0]);
        assertEquals("5-2 [1]", 2, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(12345678, 10), new Apfloat(12345, 5));

        assertEquals("8-5 [0]", 8, precisions[0]);
        assertEquals("8-5 [1]", 5, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(0), new Apfloat(12345, 5));

        assertEquals("0-0 [0]", 0, precisions[0]);
        assertEquals("0-0 [1]", 0, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(12345678, 6), new Apfloat(12, 2));

        assertEquals("6-0 [0]", 6, precisions[0]);
        assertEquals("6-0 [1]", 0, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(12345678, 5), new Apfloat(12, 2));

        assertEquals("5-0 [0]", 5, precisions[0]);
        assertEquals("5-0 [1]", 0, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(10), new Apfloat(100, 2));

        assertEquals("10, 100 2 [0]", 1, precisions[0]);
        assertEquals("10, 100 2 [1]", 2, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(10), new Apfloat(100, 3));

        assertEquals("10, 100 3 [0]", 2, precisions[0]);
        assertEquals("10, 100 3 [1]", 3, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(10), new Apfloat(1, 2));

        assertEquals("10, 1 2 [0]", 3, precisions[0]);
        assertEquals("10, 1 2 [1]", 2, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(10), new Apfloat(1, 3));

        assertEquals("10, 1 3 [0]", 4, precisions[0]);
        assertEquals("10, 1 3 [1]", 3, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(10), new Apfloat(10));

        assertEquals("10, 10 MAX [0]", Apfloat.INFINITE, precisions[0]);
        assertEquals("10, 10 MAX [1]", Apfloat.INFINITE, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(10), new Apfloat(100));

        assertEquals("10, 100 MAX [0]", Apfloat.INFINITE, precisions[0]);
        assertEquals("10, 100 MAX [1]", Apfloat.INFINITE, precisions[1]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat("1e9000000000000000000", Apfloat.INFINITE), new Apfloat("1e-9000000000000000000", Apfloat.INFINITE));

        assertEquals("MAX-MIN [0]", Apfloat.INFINITE, precisions[0]);
        assertEquals("MAX-MIN [1]", 0, precisions[1]);
    }

    public static void testGetMatchingPrecisions4()
    {
        // xxxxx000 * xxxxx + xxx00000 * xxx0 =
        // xxxxxx0000000 + xxxx00000000 =
        // xxxxx00000000 + xxxx00000000 =
        // xxxx000000000 / xxxx00000000
        long[] precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(12345678, 8), new Apfloat(12345, 5), new Apfloat(12345678, 5), new Apfloat(1234, 3));

        assertEquals("5-4-4 [0]", 5, precisions[0]);
        assertEquals("5-4-4 [1]", 4, precisions[1]);
        assertEquals("5-4-4 [2]", 4, precisions[2]);

        // xxxxx000 * xxxxx + x * x =
        // xxxxxx0000000 + xx =
        // xxxxx00000000 / xxxxx0000000
        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat(12345678, 8), new Apfloat(12345, 5), new Apfloat(1, 1), new Apfloat(2, 1));

        assertEquals("6-0-5 [0]", 6, precisions[0]);
        assertEquals("6-0-5 [1]", 0, precisions[1]);
        assertEquals("6-0-5 [2]", 5, precisions[2]);

        precisions = ApfloatHelper.getMatchingPrecisions(new Apfloat("1e4500000000000000000", Apfloat.INFINITE), new Apfloat("1e4500000000000000000", Apfloat.INFINITE), new Apfloat("1e-4500000000000000000", Apfloat.INFINITE), new Apfloat("1e-4500000000000000000", Apfloat.INFINITE));

        assertEquals("MAX-0-MAX [0]", Apfloat.INFINITE, precisions[0]);
        assertEquals("MAX-0-MAX [1]", 0, precisions[1]);
        assertEquals("MAX-0-MAX [2]", Apfloat.INFINITE, precisions[2]);
    }

    public static void testSetPrecision()
    {
        Apcomplex a = ApfloatHelper.setPrecision(new Apcomplex(new Apfloat(2), Apfloat.ZERO), 20);
        assertEquals("(2, 0) real prec", 20, a.real().precision());
        assertEquals("(2, 0) real value", new Apfloat(2, 20), a.real());
        assertEquals("(2, 0) imag prec", Apfloat.INFINITE, a.imag().precision());
        assertEquals("(2, 0) imag value", Apfloat.ZERO, a.imag());
        assertEquals("(2, 0) prec", 20, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex(Apfloat.ZERO, new Apfloat(2)), 20);
        assertEquals("(0, 2) real prec", Apfloat.INFINITE, a.real().precision());
        assertEquals("(0, 2) real value", Apfloat.ZERO, a.real());
        assertEquals("(0, 2) imag prec", 20, a.imag().precision());
        assertEquals("(0, 2) imag value", new Apfloat(2, 20), a.imag());
        assertEquals("(0, 2) prec", 20, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex("(0.0000000000123, 1.123456789012345)"), 11);
        assertEquals("(0.0000000000123, 1.123456789012345) real prec", Apfloat.INFINITE, a.real().precision());
        assertEquals("(0.0000000000123, 1.123456789012345) real value", Apfloat.ZERO, a.real());
        assertEquals("(0.0000000000123, 1.123456789012345) imag prec", 11, a.imag().precision());
        assertEquals("(0.0000000000123, 1.123456789012345) imag value", new Apfloat("1.1234567890"), a.imag());
        assertEquals("(0.0000000000123, 1.123456789012345) prec", 11, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex("(1.123456789012345, 0.0000000000123)"), 11);
        assertEquals("(1.123456789012345, 0.0000000000123) real prec", 11, a.real().precision());
        assertEquals("(1.123456789012345, 0.0000000000123) real real value", new Apfloat("1.1234567890"), a.real());
        assertEquals("(1.123456789012345, 0.0000000000123) imag prec", Apfloat.INFINITE, a.imag().precision());
        assertEquals("(1.123456789012345, 0.0000000000123) imag value", Apfloat.ZERO, a.imag());
        assertEquals("(1.123456789012345, 0.0000000000123) prec", 11, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex("(0.0000000000987, 1.098765432109876)"), 13);
        assertEquals("(0.0000000000987, 1.098765432109876) real prec", 2, a.real().precision());
        assertEquals("(0.0000000000987, 1.098765432109876) real value", new Apfloat("0.000000000098"), a.real());
        assertEquals("(0.0000000000987, 1.098765432109876) imag prec", 15, a.imag().precision());
        assertEquals("(0.0000000000987, 1.098765432109876) imag value", new Apfloat("1.09876543210987"), a.imag());
        assertEquals("(0.0000000000987, 1.098765432109876) prec", 13, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex("(1.098765432109876, 0.0000000000987)"), 13);
        assertEquals("(1.098765432109876, 0.0000000000987) real prec", 15, a.real().precision());
        assertEquals("(1.098765432109876, 0.0000000000987) real real value", new Apfloat("1.09876543210987"), a.real());
        assertEquals("(1.098765432109876, 0.0000000000987) imag prec", 2, a.imag().precision());
        assertEquals("(1.098765432109876, 0.0000000000987) imag value", new Apfloat("0.000000000098"), a.imag());
        assertEquals("(1.098765432109876, 0.0000000000987) prec", 13, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex(new Apfloat(111), new Apfloat("22.2")), 10);
        assertEquals("(111, 22.2) real prec", Apfloat.INFINITE, a.real().precision());
        assertEquals("(111, 22.2) real value", new Apfloat(111), a.real());
        assertEquals("(111, 22.2) imag prec", 9, a.imag().precision());
        assertEquals("(111, 22.2) imag value", new Apfloat("22.2"), a.imag());
        assertEquals("(111, 22.2) prec", 10, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex(new Apfloat("22.2"), new Apfloat(111)), 10);
        assertEquals("(22.2, 111) real prec", 9, a.real().precision());
        assertEquals("(22.2, 111) real real value", new Apfloat("22.2"), a.real());
        assertEquals("(22.2, 111) imag prec", Apfloat.INFINITE, a.imag().precision());
        assertEquals("(22.2, 111) imag value", new Apfloat(111), a.imag());
        assertEquals("(22.2, 111) prec", 10, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex("(22, 3.33)"), 5);
        assertEquals("(22, 3.33) real prec", 5, a.real().precision());
        assertEquals("(22, 3.33) real real value", new Apfloat("22"), a.real());
        assertEquals("(22, 3.33) imag prec", 6, a.imag().precision());
        assertEquals("(22, 3.33) imag value", new Apfloat("3.33"), a.imag());
        assertEquals("(22, 3.33) prec", 5, a.precision());

        a = ApfloatHelper.setPrecision(new Apcomplex("(3.33, 22)"), 5);
        assertEquals("(3.33, 22) real prec", 6, a.real().precision());
        assertEquals("(3.33, 22) real value", new Apfloat("3.33"), a.real());
        assertEquals("(3.33, 22) imag prec", 5, a.imag().precision());
        assertEquals("(3.33, 22) imag value", new Apfloat("22"), a.imag());
        assertEquals("(3.33, 22) prec", 5, a.precision());
    }
}
