import org.apache.commons.lang3.text.CompositeFormat;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Date;
import java.util.Calendar;

/**
 * Boundary value analysis tests for CompositeFormat class.
 * Tests edge cases, null values, empty strings, and extreme boundary conditions.
 */
public class CompositeFormatBoundaryTest {

    private Format nullTolerantParser;
    private Format nullTolerantFormatter;
    private Format strictParser;
    private Format strictFormatter;

    @Before
    public void setUp() {
        // Create null-tolerant formatters for boundary testing
        nullTolerantParser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                if (obj == null) {
                    return toAppendTo.append("null");
                }
                return toAppendTo.append(obj.toString());
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                if (source == null) {
                    pos.setIndex(0);
                    pos.setErrorIndex(-1);
                    return null;
                }
                if (source.isEmpty()) {
                    pos.setIndex(0);
                    pos.setErrorIndex(0);
                    return null;
                }
                pos.setIndex(source.length());
                return "parsed:" + source;
            }
        };

        nullTolerantFormatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                if (obj == null) {
                    return toAppendTo.append("formatted:null");
                }
                return toAppendTo.append("formatted:").append(obj.toString());
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null; // Not used in formatting
            }
        };

        // Create strict formatters that throw exceptions
        strictParser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        strictFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    }

    // ========== NULL INPUT BOUNDARY TESTS ==========

    @Test
    public void testConstructorWithNullParser() {
        // CompositeFormat allows null parser, but using it will cause NullPointerException later
        CompositeFormat composite = new CompositeFormat(null, nullTolerantFormatter);
        assertNull(composite.getParser());
        assertNotNull(composite.getFormatter());
    }

    @Test
    public void testConstructorWithNullFormatter() {
        // CompositeFormat allows null formatter, but using it will cause NullPointerException later
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, null);
        assertNotNull(composite.getParser());
        assertNull(composite.getFormatter());
    }

    @Test
    public void testConstructorWithBothNull() {
        // CompositeFormat allows both null, but using it will cause NullPointerException later
        CompositeFormat composite = new CompositeFormat(null, null);
        assertNull(composite.getParser());
        assertNull(composite.getFormatter());
    }

    @Test
    public void testFormatWithNullObject() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        StringBuffer buffer = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        
        StringBuffer result = composite.format(null, buffer, pos);
        
        assertSame(buffer, result);
        assertEquals("formatted:null", buffer.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testFormatWithNullStringBuffer() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        FieldPosition pos = new FieldPosition(0);
        
        composite.format("test", null, pos);
    }

    @Test
    public void testFormatWithNullFieldPosition() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        StringBuffer buffer = new StringBuffer();
        
        // Should not throw exception - FieldPosition can be null in some implementations
        try {
            composite.format("test", buffer, null);
            assertTrue(buffer.length() > 0);
        } catch (NullPointerException e) {
            // Acceptable if underlying formatter requires non-null FieldPosition
        }
    }

    @Test
    public void testParseObjectWithNullSource() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        ParsePosition pos = new ParsePosition(0);
        
        Object result = composite.parseObject(null, pos);
        
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    @Test(expected = NullPointerException.class)
    public void testParseObjectWithNullParsePosition() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        composite.parseObject("test", null);
    }

    // ========== EMPTY STRING BOUNDARY TESTS ==========

    @Test
    public void testParseObjectWithEmptyString() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        ParsePosition pos = new ParsePosition(0);
        
        Object result = composite.parseObject("", pos);
        
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testFormatWithEmptyStringBuffer() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        StringBuffer buffer = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        
        StringBuffer result = composite.format("test", buffer, pos);
        
        assertSame(buffer, result);
        assertEquals("formatted:test", buffer.toString());
    }

    @Test
    public void testFormatWithPrePopulatedStringBuffer() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        StringBuffer buffer = new StringBuffer("prefix:");
        FieldPosition pos = new FieldPosition(0);
        
        StringBuffer result = composite.format("test", buffer, pos);
        
        assertSame(buffer, result);
        assertEquals("prefix:formatted:test", buffer.toString());
    }

    // ========== PARSE POSITION BOUNDARY TESTS ==========

    @Test
    public void testParsePositionAtStringStart() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        ParsePosition pos = new ParsePosition(0);
        
        Object result = composite.parseObject("test", pos);
        
        assertEquals("parsed:test", result);
        assertEquals(4, pos.getIndex()); // Should advance to end of string
    }

    @Test
    public void testParsePositionAtStringEnd() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        String input = "test";
        ParsePosition pos = new ParsePosition(input.length());
        
        Object result = composite.parseObject(input, pos);
        
        // Behavior depends on parser implementation - likely null or error
        assertTrue(pos.getIndex() >= input.length() || pos.getErrorIndex() >= 0);
    }

    @Test
    public void testParsePositionBeyondStringEnd() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        String input = "test";
        ParsePosition pos = new ParsePosition(input.length() + 10);
        
        Object result = composite.parseObject(input, pos);
        
        // Should handle gracefully - our null tolerant parser doesn't validate position
        // so it just parses the whole string and advances position to end
        assertNotNull(result);
        assertEquals("parsed:" + input, result);
    }

    @Test
    public void testParsePositionNegative() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        ParsePosition pos = new ParsePosition(-1);
        
        // Should handle gracefully - some parsers may throw, others may treat as 0
        try {
            Object result = composite.parseObject("test", pos);
            // If no exception, verify reasonable behavior
            assertTrue(pos.getIndex() >= 0 || pos.getErrorIndex() >= 0);
        } catch (StringIndexOutOfBoundsException e) {
            // Acceptable behavior for negative position
        }
    }

    // ========== FIELD POSITION BOUNDARY TESTS ==========

    @Test
    public void testFieldPositionWithZeroField() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        StringBuffer buffer = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        
        composite.format("test", buffer, pos);
        
        // FieldPosition should be handled by underlying formatter
        assertTrue(buffer.length() > 0);
    }

    @Test
    public void testFieldPositionWithNegativeField() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        StringBuffer buffer = new StringBuffer();
        FieldPosition pos = new FieldPosition(-1);
        
        composite.format("test", buffer, pos);
        
        // Should handle gracefully
        assertTrue(buffer.length() > 0);
    }

    @Test
    public void testFieldPositionWithLargeField() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        StringBuffer buffer = new StringBuffer();
        FieldPosition pos = new FieldPosition(Integer.MAX_VALUE);
        
        composite.format("test", buffer, pos);
        
        // Should handle gracefully
        assertTrue(buffer.length() > 0);
    }

    // ========== DATE FORMAT BOUNDARY TESTS ==========

    @Test
    public void testDateFormatBoundaryValues() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Test minimum valid date
        String minDate = "0001-01-01";
        Object parsed = composite.parseObject(minDate);
        assertNotNull(parsed);
        
        StringBuffer buffer = new StringBuffer();
        composite.format(parsed, buffer, new FieldPosition(0));
        assertEquals("01/01/0001", buffer.toString());
    }

    @Test
    public void testDateFormatMaximumYear() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Test year 9999 (close to maximum)
        String maxDate = "9999-12-31";
        Object parsed = composite.parseObject(maxDate);
        assertNotNull(parsed);
        
        StringBuffer buffer = new StringBuffer();
        composite.format(parsed, buffer, new FieldPosition(0));
        assertEquals("31/12/9999", buffer.toString());
    }

    @Test
    public void testLeapYearBoundary() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Test leap year February 29th
        String leapDate = "2000-02-29";
        Object parsed = composite.parseObject(leapDate);
        assertNotNull(parsed);
        
        StringBuffer buffer = new StringBuffer();
        composite.format(parsed, buffer, new FieldPosition(0));
        assertEquals("29/02/2000", buffer.toString());
    }

    // ========== NUMBER FORMAT BOUNDARY TESTS ==========

    @Test
    public void testNumberFormatMinimumValue() {
        DecimalFormat parser = new DecimalFormat("#");
        DecimalFormat formatter = new DecimalFormat("0.00");
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        ParsePosition pos = new ParsePosition(0);
        Object parsed = composite.parseObject("0", pos);
        assertNotNull(parsed);
        
        StringBuffer buffer = new StringBuffer();
        composite.format(parsed, buffer, new FieldPosition(0));
        assertEquals("0.00", buffer.toString());
    }

    @Test
    public void testNumberFormatMaximumValue() {
        DecimalFormat parser = new DecimalFormat("#.#");
        DecimalFormat formatter = new DecimalFormat("0.00E0");
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        ParsePosition pos = new ParsePosition(0);
        Object parsed = composite.parseObject("999999999", pos);
        assertNotNull(parsed);
        
        StringBuffer buffer = new StringBuffer();
        composite.format(parsed, buffer, new FieldPosition(0));
        assertTrue(buffer.toString().contains("E"));
    }

    @Test
    public void testNumberFormatNegativeBoundary() {
        DecimalFormat parser = new DecimalFormat("#.#");
        DecimalFormat formatter = new DecimalFormat("+0.00;-0.00");
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        ParsePosition pos = new ParsePosition(0);
        Object parsed = composite.parseObject("-0.1", pos);
        assertNotNull(parsed);
        
        StringBuffer buffer = new StringBuffer();
        composite.format(parsed, buffer, new FieldPosition(0));
        assertEquals("-0.10", buffer.toString());
    }

    // ========== REFORMAT METHOD BOUNDARY TESTS ==========

    @Test
    public void testReformatWithNullInput() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        try {
            String result = composite.reformat(null);
            assertEquals("formatted:null", result);
        } catch (ParseException e) {
            // Acceptable if parser doesn't handle null
        }
    }

    @Test(expected = ParseException.class)
    public void testReformatWithEmptyString() throws ParseException {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        composite.reformat("");
    }

    @Test(expected = ParseException.class)
    public void testReformatWithInvalidInput() throws ParseException {
        CompositeFormat composite = new CompositeFormat(strictParser, strictFormatter);
        
        composite.reformat("invalid-date-format");
    }

    @Test
    public void testReformatValidBoundaryInput() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        String result = composite.reformat("2000-01-01");
        assertEquals("Saturday, January 01, 2000", result);
    }

    // ========== GETTER METHOD BOUNDARY TESTS ==========

    @Test
    public void testGetParserNotNull() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        Format parser = composite.getParser();
        
        assertNotNull(parser);
        assertSame(nullTolerantParser, parser);
    }

    @Test
    public void testGetFormatterNotNull() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        Format formatter = composite.getFormatter();
        
        assertNotNull(formatter);
        assertSame(nullTolerantFormatter, formatter);
    }

    // ========== EXTREME STRING LENGTH BOUNDARY TESTS ==========

    @Test
    public void testVeryLongString() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        // Create a very long string
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longString.append("a");
        }
        
        ParsePosition pos = new ParsePosition(0);
        Object result = composite.parseObject(longString.toString(), pos);
        
        assertNotNull(result);
        assertEquals(10000, pos.getIndex());
    }

    @Test
    public void testSingleCharacterString() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        ParsePosition pos = new ParsePosition(0);
        Object result = composite.parseObject("x", pos);
        
        assertNotNull(result);
        assertEquals("parsed:x", result);
        assertEquals(1, pos.getIndex());
    }

    // ========== UNICODE AND SPECIAL CHARACTER BOUNDARY TESTS ==========

    @Test
    public void testUnicodeCharacters() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        String unicodeString = "测试αβγ🌟";
        ParsePosition pos = new ParsePosition(0);
        Object result = composite.parseObject(unicodeString, pos);
        
        assertNotNull(result);
        assertEquals("parsed:" + unicodeString, result);
    }

    @Test
    public void testSpecialCharacters() {
        CompositeFormat composite = new CompositeFormat(nullTolerantParser, nullTolerantFormatter);
        
        String specialChars = "\t\n\r\"'\\";
        ParsePosition pos = new ParsePosition(0);
        Object result = composite.parseObject(specialChars, pos);
        
        assertNotNull(result);
        assertEquals("parsed:" + specialChars, result);
    }
}
