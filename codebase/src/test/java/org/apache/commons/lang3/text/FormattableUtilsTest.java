/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.text;

import static java.util.FormattableFlags.LEFT_JUSTIFY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Formattable;
import java.util.Formatter;

import org.junit.Test;

/**
 * Unit tests {@link FormattableUtils}.
 *
 * @version $Id$
 */
public class FormattableUtilsTest {

    @Test
    public void testConstructor() {
        // Test that constructor is accessible and creates an instance
        final FormattableUtils utils = new FormattableUtils();
        assertNotNull(utils);
    }

    @Test
    public void testToString() {
        final Formattable formattable = new Formattable() {
            @Override
            public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
                formatter.format("test");
            }
        };
        assertEquals("test", FormattableUtils.toString(formattable));
    }

    @Test
    public void testToStringWithComplexFormattable() {
        final Formattable formattable = new Formattable() {
            @Override
            public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
                formatter.format("Complex:%s", "Value");
            }
        };
        assertEquals("Complex:Value", FormattableUtils.toString(formattable));
    }

    @Test
    public void testEmptyString() {
        assertEquals("", FormattableUtils.append("", new Formatter(), 0, -1, -1).toString());
        assertEquals("   ", FormattableUtils.append("", new Formatter(), 0, 3, -1).toString());
        assertEquals("   ", FormattableUtils.append("", new Formatter(), LEFT_JUSTIFY, 3, -1).toString());
    }

    @Test
    public void testSingleCharacter() {
        assertEquals("a", FormattableUtils.append("a", new Formatter(), 0, -1, -1).toString());
        assertEquals(" a", FormattableUtils.append("a", new Formatter(), 0, 2, -1).toString());
        assertEquals("a ", FormattableUtils.append("a", new Formatter(), LEFT_JUSTIFY, 2, -1).toString());
        assertEquals("", FormattableUtils.append("a", new Formatter(), 0, -1, 0).toString());
    }

    @Test
    public void testZeroPrecision() {
        assertEquals("", FormattableUtils.append("foo", new Formatter(), 0, -1, 0).toString());
        assertEquals("  ", FormattableUtils.append("foo", new Formatter(), 0, 2, 0).toString());
        assertEquals("  ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 2, 0).toString());
    }

    @Test
    public void testZeroWidth() {
        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), 0, 0, -1).toString());
        assertEquals("fo", FormattableUtils.append("foo", new Formatter(), 0, 0, 2).toString());
        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 0, -1).toString());
        assertEquals("fo", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 0, 2).toString());
    }

    @Test
    public void testNullEllipsis() {
        assertEquals("fo", FormattableUtils.append("foo", new Formatter(), 0, -1, 2, null).toString());
        assertEquals(" fo", FormattableUtils.append("foo", new Formatter(), 0, 3, 2, null).toString());
        assertEquals("fo ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2, null).toString());
    }

    @Test
    public void testEmptyEllipsis() {
        assertEquals("fo", FormattableUtils.append("foo", new Formatter(), 0, -1, 2, "").toString());
        assertEquals(" fo", FormattableUtils.append("foo", new Formatter(), 0, 3, 2, "").toString());
        assertEquals("fo ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2, "").toString());
    }

    @Test
    public void testLongString() {
        final String longString = "This is a very long string that needs to be truncated";
        assertEquals("This i...", FormattableUtils.append(longString, new Formatter(), 0, -1, 9, "...").toString());
        assertEquals("    This i...", FormattableUtils.append(longString, new Formatter(), 0, 13, 9, "...").toString());
        assertEquals("This i...    ", FormattableUtils.append(longString, new Formatter(), LEFT_JUSTIFY, 13, 9, "...").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEllipsisTooLongForPrecision() {
        FormattableUtils.append("foo", new Formatter(), 0, -1, 2, "too long");
    }

    @Test
    public void testEllipsisEqualToPrecision() {
        // Test ellipsis length equal to precision (should work)
        assertEquals("abc", FormattableUtils.append("abcdef", new Formatter(), 0, -1, 3, "abc").toString());
    }

    @Test
    public void testEllipsisMaxLength() {
        // Test ellipsis length equal to precision - 1 (should work)
        assertEquals("a...", FormattableUtils.append("abcdef", new Formatter(), 0, -1, 4, "...").toString());
    }

    @Test
    public void testMaxPrecisionWithEllipsis() {
        // Test when precision exactly equals ellipsis length
        assertEquals("...", FormattableUtils.append("abcdef", new Formatter(), 0, -1, 3, "...").toString());
    }

    @Test
    public void testDefaultAppend() {
        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), 0, -1, -1).toString());
        assertEquals("fo", FormattableUtils.append("foo", new Formatter(), 0, -1, 2).toString());
        assertEquals(" foo", FormattableUtils.append("foo", new Formatter(), 0, 4, -1).toString());
        assertEquals("   foo", FormattableUtils.append("foo", new Formatter(), 0, 6, -1).toString());
        assertEquals(" fo", FormattableUtils.append("foo", new Formatter(), 0, 3, 2).toString());
        assertEquals("   fo", FormattableUtils.append("foo", new Formatter(), 0, 5, 2).toString());
        assertEquals("foo ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 4, -1).toString());
        assertEquals("foo   ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 6, -1).toString());
        assertEquals("fo ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2).toString());
        assertEquals("fo   ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 5, 2).toString());
    }

    @Test
    public void testAlternatePadCharacter() {
        final char pad='_';
        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), 0, -1, -1, pad).toString());
        assertEquals("fo", FormattableUtils.append("foo", new Formatter(), 0, -1, 2, pad).toString());
        assertEquals("_foo", FormattableUtils.append("foo", new Formatter(), 0, 4, -1, pad).toString());
        assertEquals("___foo", FormattableUtils.append("foo", new Formatter(), 0, 6, -1, pad).toString());
        assertEquals("_fo", FormattableUtils.append("foo", new Formatter(), 0, 3, 2, pad).toString());
        assertEquals("___fo", FormattableUtils.append("foo", new Formatter(), 0, 5, 2, pad).toString());
        assertEquals("foo_", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 4, -1, pad).toString());
        assertEquals("foo___", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 6, -1, pad).toString());
        assertEquals("fo_", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2, pad).toString());
        assertEquals("fo___", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 5, 2, pad).toString());
    }

    @Test
    public void testEllipsis() {
        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), 0, -1, -1, "*").toString());
        assertEquals("f*", FormattableUtils.append("foo", new Formatter(), 0, -1, 2, "*").toString());
        assertEquals(" foo", FormattableUtils.append("foo", new Formatter(), 0, 4, -1, "*").toString());
        assertEquals("   foo", FormattableUtils.append("foo", new Formatter(), 0, 6, -1, "*").toString());
        assertEquals(" f*", FormattableUtils.append("foo", new Formatter(), 0, 3, 2, "*").toString());
        assertEquals("   f*", FormattableUtils.append("foo", new Formatter(), 0, 5, 2, "*").toString());
        assertEquals("foo ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 4, -1, "*").toString());
        assertEquals("foo   ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 6, -1, "*").toString());
        assertEquals("f* ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2, "*").toString());
        assertEquals("f*   ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 5, 2, "*").toString());

        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), 0, -1, -1, "+*").toString());
        assertEquals("+*", FormattableUtils.append("foo", new Formatter(), 0, -1, 2, "+*").toString());
        assertEquals(" foo", FormattableUtils.append("foo", new Formatter(), 0, 4, -1, "+*").toString());
        assertEquals("   foo", FormattableUtils.append("foo", new Formatter(), 0, 6, -1, "+*").toString());
        assertEquals(" +*", FormattableUtils.append("foo", new Formatter(), 0, 3, 2, "+*").toString());
        assertEquals("   +*", FormattableUtils.append("foo", new Formatter(), 0, 5, 2, "+*").toString());
        assertEquals("foo ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 4, -1, "+*").toString());
        assertEquals("foo   ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 6, -1, "+*").toString());
        assertEquals("+* ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2, "+*").toString());
        assertEquals("+*   ", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 5, 2, "+*").toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIllegalEllipsis() {
        FormattableUtils.append("foo", new Formatter(), 0, -1, 1, "xx");
    }

    @Test
    public void testAlternatePadCharAndEllipsis() {
        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), 0, -1, -1, '_', "*").toString());
        assertEquals("f*", FormattableUtils.append("foo", new Formatter(), 0, -1, 2, '_', "*").toString());
        assertEquals("_foo", FormattableUtils.append("foo", new Formatter(), 0, 4, -1, '_', "*").toString());
        assertEquals("___foo", FormattableUtils.append("foo", new Formatter(), 0, 6, -1, '_', "*").toString());
        assertEquals("_f*", FormattableUtils.append("foo", new Formatter(), 0, 3, 2, '_', "*").toString());
        assertEquals("___f*", FormattableUtils.append("foo", new Formatter(), 0, 5, 2, '_', "*").toString());
        assertEquals("foo_", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 4, -1, '_', "*").toString());
        assertEquals("foo___", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 6, -1, '_', "*").toString());
        assertEquals("f*_", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2, '_', "*").toString());
        assertEquals("f*___", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 5, 2, '_', "*").toString());

        assertEquals("foo", FormattableUtils.append("foo", new Formatter(), 0, -1, -1, '_', "+*").toString());
        assertEquals("+*", FormattableUtils.append("foo", new Formatter(), 0, -1, 2, '_', "+*").toString());
        assertEquals("_foo", FormattableUtils.append("foo", new Formatter(), 0, 4, -1, '_', "+*").toString());
        assertEquals("___foo", FormattableUtils.append("foo", new Formatter(), 0, 6, -1, '_', "+*").toString());
        assertEquals("_+*", FormattableUtils.append("foo", new Formatter(), 0, 3, 2, '_', "+*").toString());
        assertEquals("___+*", FormattableUtils.append("foo", new Formatter(), 0, 5, 2, '_', "+*").toString());
        assertEquals("foo_", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 4, -1, '_', "+*").toString());
        assertEquals("foo___", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 6, -1, '_', "+*").toString());
        assertEquals("+*_", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 3, 2, '_', "+*").toString());
        assertEquals("+*___", FormattableUtils.append("foo", new Formatter(), LEFT_JUSTIFY, 5, 2, '_', "+*").toString());
    }

}
