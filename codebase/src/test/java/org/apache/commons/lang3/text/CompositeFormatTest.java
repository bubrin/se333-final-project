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

import org.junit.Test;
import static org.junit.Assert.*;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Unit tests for {@link org.apache.commons.lang3.text.CompositeFormat}.
 */
public class CompositeFormatTest {

    /**
     * Ensures that the parse/format separation is correctly maintained and actually exercises CompositeFormat methods. 
     */
    @Test
    public void testCompositeFormat() {
        // Use real SimpleDateFormat instances to ensure CompositeFormat code is executed
        final Format parser = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        final Format formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Test that constructor properly stores the formats
        assertSame("Parser get method incorrectly implemented", parser, composite.getParser());
        assertSame("Formatter get method incorrectly implemented", formatter, composite.getFormatter());
        
        // Test actual parsing and formatting to ensure code coverage
        try {
            final Object parsed = composite.parseObject("25/12/2023");
            assertNotNull("Parsed object should not be null", parsed);
            
            final StringBuffer buffer = new StringBuffer();
            final StringBuffer result = composite.format(parsed, buffer, new FieldPosition(0));
            assertSame("Format should return the same buffer", buffer, result);
            assertEquals("Should format date correctly", "2023-12-25", result.toString());
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testUsage() throws Exception {
        final Format f1 = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
        final Format f2 = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        final CompositeFormat c = new CompositeFormat(f1, f2);
        final String testString = "January 3, 2005";
        assertEquals(testString, c.format(c.parseObject("01032005")));
        assertEquals(testString, c.reformat("01032005"));
    }


    @Test
    public void testParseObject() {
        final Format parser = new Format() {
            @Override
            public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public Object parseObject(final String source, final ParsePosition pos) {
                if ("test".equals(source)) {
                    pos.setIndex(4);
                    return "parsed";
                }
                return null;
            }
        };

        final Format formatter = new Format() {
            @Override
            public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
                return toAppendTo.append("formatted");
            }

            @Override
            public Object parseObject(final String source, final ParsePosition pos) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        final ParsePosition pos = new ParsePosition(0);
        final Object result = composite.parseObject("test", pos);
        
        assertEquals("Should parse correctly", "parsed", result);
        assertEquals("Position should be updated", 4, pos.getIndex());
    }

    @Test
    public void testGetParser() {
        final Format parser = new SimpleDateFormat("yyyy-MM-dd");
        final Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        assertSame("Should return the same parser instance", parser, composite.getParser());
    }

    @Test
    public void testGetFormatter() {
        final Format parser = new SimpleDateFormat("yyyy-MM-dd");
        final Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        assertSame("Should return the same formatter instance", formatter, composite.getFormatter());
    }

    @Test
    public void testFormat() {
        final Format parser = new Format() {
            @Override
            public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public Object parseObject(final String source, final ParsePosition pos) {
                return null;
            }
        };

        final Format formatter = new Format() {
            @Override
            public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
                return toAppendTo.append("Formatted: ").append(obj.toString());
            }

            @Override
            public Object parseObject(final String source, final ParsePosition pos) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        final StringBuffer buffer = new StringBuffer();
        final FieldPosition pos = new FieldPosition(0);
        
        final StringBuffer result = composite.format("test", buffer, pos);
        
        assertEquals("Should format correctly", "Formatted: test", result.toString());
        assertSame("Should return the same buffer", buffer, result);
    }

    @Test
    public void testReformat() throws ParseException {
        final Format parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        final Format formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        final String result = composite.reformat("2023-12-25");
        assertEquals("Should reformat correctly", "Monday, December 25, 2023", result);
    }

    @Test(expected = ParseException.class)
    public void testReformatWithParseException() throws ParseException {
        final Format parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        final Format formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        composite.reformat("invalid-date");
    }

    @Test
    public void testNullInputHandling() {
        final Format parser = new Format() {
            @Override
            public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public Object parseObject(final String source, final ParsePosition pos) {
                return null;
            }
        };

        final Format formatter = new Format() {
            @Override
            public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
                if (obj == null) {
                    return toAppendTo.append("null");
                }
                return toAppendTo.append(obj.toString());
            }

            @Override
            public Object parseObject(final String source, final ParsePosition pos) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        final StringBuffer buffer = new StringBuffer();
        
        composite.format(null, buffer, new FieldPosition(0));
        assertEquals("Should handle null input", "null", buffer.toString());
    }
    
    @Test
    public void testActualFormatDelegation() {
        // Test that CompositeFormat actually delegates to the formatter
        final SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        try {
            // Parse a date
            final Object date = composite.parseObject("12/25/2023");
            assertNotNull("Date should be parsed", date);
            
            // Format the parsed date using CompositeFormat
            final StringBuffer buffer = new StringBuffer();
            composite.format(date, buffer, new FieldPosition(0));
            
            // Verify it uses the formatter's pattern
            assertEquals("Should use formatter's pattern", "25-12-2023", buffer.toString());
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testActualParseDelegation() {
        // Test that CompositeFormat actually delegates to the parser
        final SimpleDateFormat parser = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH);
        final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        final ParsePosition pos = new ParsePosition(0);
        final Object result = composite.parseObject("2023.12.25", pos);
        
        assertNotNull("Should parse the date", result);
        assertEquals("Should advance position correctly", 10, pos.getIndex());
    }
    
    @Test
    public void testConstructorParameterStorage() {
        // Test that constructor properly stores parameters
        final SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Verify exact same instances are returned
        assertSame("Parser should be exact same instance", parser, composite.getParser());
        assertSame("Formatter should be exact same instance", formatter, composite.getFormatter());
        
        // Verify they are not the same instance as each other
        assertNotSame("Parser and formatter should be different instances", parser, formatter);
    }
    
    @Test
    public void testReformatActuallyUsesClassMethods() throws ParseException {
        // Test that reformat() method actually calls parseObject and format methods of CompositeFormat
        final SimpleDateFormat parser = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Use reformat to ensure it exercises both parseObject and format methods
        final String result = composite.reformat("12-25-2023");
        
        assertNotNull("Reformatted result should not be null", result);
        assertEquals("Should reformat correctly", "25/12/2023", result);
    }
    
    @Test
    public void testInheritanceFromFormat() {
        // Verify that CompositeFormat properly extends Format
        final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        assertTrue("CompositeFormat should be instance of Format", composite instanceof Format);
        
        // Test inherited parseObject(String) method
        try {
            final Object parsed = composite.parseObject("2023-12-25");
            assertNotNull("Should parse using inherited method", parsed);
        } catch (ParseException e) {
            fail("Should not throw ParseException: " + e.getMessage());
        }
    }
    
    @Test
    public void testFieldPositionHandling() {
        // Test that FieldPosition is properly handled in format method
        final SimpleDateFormat parser = new SimpleDateFormat("yyyy/MM/dd");
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        try {
            final Object date = composite.parseObject("2023/12/25");
            final StringBuffer buffer = new StringBuffer();
            final FieldPosition fieldPos = new FieldPosition(SimpleDateFormat.YEAR_FIELD);
            
            composite.format(date, buffer, fieldPos);
            
            // Verify the format worked
            assertEquals("Should format correctly", "25-12-2023", buffer.toString());
            // FieldPosition should be updated by the underlying formatter
            assertTrue("Field position should have valid range", fieldPos.getEndIndex() >= fieldPos.getBeginIndex());
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testDirectMethodCalls() {
        // Direct test to ensure CompositeFormat methods are actually called
        final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        
        // Create CompositeFormat - this should execute constructor
        final CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Test getParser() method directly
        final Format retrievedParser = composite.getParser();
        assertSame("getParser() should return parser", parser, retrievedParser);
        
        // Test getFormatter() method directly  
        final Format retrievedFormatter = composite.getFormatter();
        assertSame("getFormatter() should return formatter", formatter, retrievedFormatter);
        
        try {
            // Test parseObject method directly
            final ParsePosition pos = new ParsePosition(0);
            final Object parsed = composite.parseObject("2023-12-25", pos);
            assertNotNull("parseObject should return result", parsed);
            assertTrue("parseObject should advance position", pos.getIndex() > 0);
            
            // Test format method directly
            final StringBuffer buffer = new StringBuffer();
            final FieldPosition fieldPos = new FieldPosition(0);
            final StringBuffer result = composite.format(parsed, buffer, fieldPos);
            assertSame("format should return same buffer", buffer, result);
            assertFalse("format should produce output", buffer.toString().isEmpty());
            
            // Test reformat method directly
            final String reformatted = composite.reformat("2023-12-25");
            assertNotNull("reformat should return result", reformatted);
            assertFalse("reformat should produce output", reformatted.isEmpty());
            
        } catch (ParseException e) {
            fail("Should not throw ParseException: " + e.getMessage());
        }
    }

    @Test
    public void testparseObject() {
        // Placeholder for auto-improvement
    }

    @Test
    public void testgetParser() {
        // Placeholder for auto-improvement
    }

    @Test
    public void testgetFormatter() {
        // Placeholder for auto-improvement
    }

}