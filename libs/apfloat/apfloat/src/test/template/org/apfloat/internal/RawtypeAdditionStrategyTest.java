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
package org.apfloat.internal;

import org.apfloat.*;
import org.apfloat.spi.*;

import junit.framework.TestSuite;

/**
 * @version 1.7.0
 * @author Mikko Tommila
 */

public class RawtypeAdditionStrategyTest
    extends RawtypeTestCase
    implements RawtypeRadixConstants
{
    public RawtypeAdditionStrategyTest(String methodName)
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

        suite.addTest(new RawtypeAdditionStrategyTest("testAdd"));
        suite.addTest(new RawtypeAdditionStrategyTest("testSubtract"));
        suite.addTest(new RawtypeAdditionStrategyTest("testMultiplyAdd"));
        suite.addTest(new RawtypeAdditionStrategyTest("testDivide"));

        return suite;
    }

    private static DataStorage createDataStorage(rawtype[] data)
    {
        int size = data.length;
        ApfloatContext ctx = ApfloatContext.getContext();
        DataStorageBuilder dataStorageBuilder = ctx.getBuilderFactory().getDataStorageBuilder();
        DataStorage dataStorage = dataStorageBuilder.createDataStorage(size * sizeof(rawtype));
        dataStorage.setSize(size);

        ArrayAccess arrayAccess = dataStorage.getArray(DataStorage.WRITE, 0, size);
        System.arraycopy(data, 0, arrayAccess.getData(), arrayAccess.getOffset(), size);
        arrayAccess.close();

        return dataStorage;
    }

    private static void check(String message, rawtype[] expected, DataStorage actual)
    {
        ArrayAccess arrayAccess = actual.getArray(DataStorage.READ, 0, expected.length);
        assertEquals(message + " length", expected.length, arrayAccess.getLength());
        for (int i = 0; i < arrayAccess.getLength(); i++)
        {
            assertEquals(message + " [" + i + "]", (long) expected[i], (long) arrayAccess.getRawtypeData()[arrayAccess.getOffset() + i]);
        }
        arrayAccess.close();
    }

    public static void testAdd()
    {
        DataStorage src1 = createDataStorage(new rawtype[] { (rawtype) 0, (rawtype) 1, (rawtype) 2, (rawtype) 3 }),
                    src2 = createDataStorage(new rawtype[] { (rawtype) 4, (rawtype) 5, (rawtype) 6, (rawtype) 7 }),
                    dst = createDataStorage(new rawtype[4]);

        RawtypeAdditionStrategy strategy = new RawtypeAdditionStrategy(10);
        rawtype carry = strategy.zero();

        carry = strategy.add(src1.iterator(DataStorage.READ, 0, 4),
                             src2.iterator(DataStorage.READ, 0, 4),
                             carry,
                             dst.iterator(DataStorage.WRITE, 0, 4),
                             4);

        assertEquals("carry", strategy.zero(), (RawType) carry);
        check("result", new rawtype[] { (rawtype) 4, (rawtype) 6, (rawtype) 8, (rawtype) 10 }, dst);
    }

    public static void testSubtract()
    {
        DataStorage src1 = createDataStorage(new rawtype[] { (rawtype) 4, (rawtype) 5, (rawtype) 6, (rawtype) 7 }),
                    src2 = createDataStorage(new rawtype[] { (rawtype) 0, (rawtype) 1, (rawtype) 2, (rawtype) 3 }),
                    dst = createDataStorage(new rawtype[4]);

        RawtypeAdditionStrategy strategy = new RawtypeAdditionStrategy(10);
        rawtype carry = strategy.zero();

        carry = strategy.subtract(src1.iterator(DataStorage.READ, 0, 4),
                                  src2.iterator(DataStorage.READ, 0, 4),
                                  carry,
                                  dst.iterator(DataStorage.WRITE, 0, 4),
                                  4);

        assertEquals("carry", strategy.zero(), (RawType) carry);
        check("result", new rawtype[] { (rawtype) 4, (rawtype) 4, (rawtype) 4, (rawtype) 4 }, dst);
    }

    public static void testMultiplyAdd()
    {
        DataStorage src1 = createDataStorage(new rawtype[] { (rawtype) 1, (rawtype) 2, (rawtype) 3, (rawtype) 4 }),
                    src2 = createDataStorage(new rawtype[] { (rawtype) 5, (rawtype) 6, (rawtype) 7, (rawtype) 8 }),
                    dst = createDataStorage(new rawtype[4]);

        RawtypeAdditionStrategy strategy = new RawtypeAdditionStrategy(10);
        rawtype carry = strategy.zero();

        carry = strategy.multiplyAdd(src1.iterator(DataStorage.READ, 0, 4),
                                     src2.iterator(DataStorage.READ, 0, 4),
                                     (rawtype) 9,
                                     carry,
                                     dst.iterator(DataStorage.WRITE, 0, 4),
                                     4);

        assertEquals("carry", strategy.zero(), (RawType) carry);
        check("result", new rawtype[] { (rawtype) 14, (rawtype) 24, (rawtype) 34, (rawtype) 44 }, dst);
    }

    public static void testDivide()
    {
        DataStorage src1 = createDataStorage(new rawtype[] { (rawtype) 0, (rawtype) 2, (rawtype) 4, (rawtype) 7 }),
                    dst = createDataStorage(new rawtype[4]);

        RawtypeAdditionStrategy strategy = new RawtypeAdditionStrategy(10);
        rawtype carry = strategy.zero();

        carry = strategy.divide(src1.iterator(DataStorage.READ, 0, 4),
                                (rawtype) 2,
                                carry,
                                dst.iterator(DataStorage.WRITE, 0, 4),
                                4);

        assertEquals("carry", 1, (long) carry);
        check("result", new rawtype[] { (rawtype) 0, (rawtype) 1, (rawtype) 2, (rawtype) 3 }, dst);
    }
}
