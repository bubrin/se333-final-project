import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.lang3.text.CompositeFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Equivalence class tests for CompositeFormat
 * Based on static analysis identifying different input/output classes
 */
public class CompositeFormatEquivalenceTest {
    
    @Test
    public void testValidFormatEquivalenceClasses() {
        // Equivalence class: Valid date formats
        SimpleDateFormat parser1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatter1 = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        CompositeFormat composite1 = new CompositeFormat(parser1, formatter1);
        
        // Valid date input
        try {
            String result = composite1.reformat("2023-12-25");
            assertEquals("12/25/2023", result, "Valid date should reformat");
        } catch (ParseException e) {
            fail("Valid date should not throw exception");
        }
        
        // Equivalence class: Valid number formats
        DecimalFormat parser2 = new DecimalFormat("#,###.##");
        DecimalFormat formatter2 = new DecimalFormat("$#,###.00");
        CompositeFormat composite2 = new CompositeFormat(parser2, formatter2);
        
        try {
            String result = composite2.reformat("1,234.56");
            assertEquals("$1,234.56", result, "Valid number should reformat");
        } catch (ParseException e) {
            fail("Valid number should not throw exception");
        }
    }
    
    @Test
    public void testParseableInputClasses() {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo.append(obj.toString());
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                if (source.startsWith("valid:")) {
                    pos.setIndex(source.length());
                    return source.substring(6);
                }
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, parser);
        
        try {
            // Equivalence class: Parseable inputs
            Object result1 = composite.parseObject("valid:data");
            assertEquals("data", result1, "Parseable input should return value");
            
            // Equivalence class: Non-parseable inputs
            Object result2 = composite.parseObject("invalid:data");
            assertNull(result2, "Non-parseable input should return null");
        } catch (ParseException e) {
            fail("Should not throw ParseException: " + e.getMessage());
        }
    }
    
    @Test
    public void testFormattableObjectClasses() {
        Format formatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                if (obj instanceof String) {
                    return toAppendTo.append("String:" + obj);
                } else if (obj instanceof Number) {
                    return toAppendTo.append("Number:" + obj);
                } else if (obj == null) {
                    return toAppendTo.append("null");
                } else {
                    return toAppendTo.append("Object:" + obj.toString());
                }
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return source;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(formatter, formatter);
        
        // Equivalence class: String objects
        StringBuffer buffer1 = new StringBuffer();
        composite.format("test", buffer1, new FieldPosition(0));
        assertEquals("String:test", buffer1.toString(), "String object formatting");
        
        // Equivalence class: Number objects
        StringBuffer buffer2 = new StringBuffer();
        composite.format(42, buffer2, new FieldPosition(0));
        assertEquals("Number:42", buffer2.toString(), "Number object formatting");
        
        // Equivalence class: null objects
        StringBuffer buffer3 = new StringBuffer();
        composite.format(null, buffer3, new FieldPosition(0));
        assertEquals("null", buffer3.toString(), "Null object formatting");
        
        // Equivalence class: Other objects
        StringBuffer buffer4 = new StringBuffer();
        composite.format(new Date(), buffer4, new FieldPosition(0));
        assertTrue(buffer4.toString().startsWith("Object:"), "Other object formatting");
    }
    
    @Test
    public void testParsePositionStates() {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                int index = pos.getIndex();
                if (index < source.length() && source.charAt(index) == 'X') {
                    pos.setIndex(index + 1);
                    return "found";
                }
                pos.setErrorIndex(index);
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, new SimpleDateFormat("yyyy"));
        
        // Equivalence class: Successful parse (position advances)
        ParsePosition pos1 = new ParsePosition(0);
        Object result1 = composite.parseObject("XYZ", pos1);
        assertEquals("found", result1, "Successful parse result");
        assertEquals(1, pos1.getIndex(), "Position should advance");
        assertEquals(-1, pos1.getErrorIndex(), "Error index should be -1");
        
        // Equivalence class: Failed parse (error index set)
        ParsePosition pos2 = new ParsePosition(0);
        Object result2 = composite.parseObject("ABC", pos2);
        assertNull(result2, "Failed parse result");
        assertEquals(0, pos2.getErrorIndex(), "Error index should be set");
    }
    
    @Test
    public void testStringBufferSizeClasses() {
        Format formatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo.append("FORMATTED");
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return source;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(formatter, formatter);
        
        // Equivalence class: Empty StringBuffer
        StringBuffer emptyBuffer = new StringBuffer();
        StringBuffer result1 = composite.format("test", emptyBuffer, new FieldPosition(0));
        assertEquals("FORMATTED", result1.toString(), "Empty buffer result");
        assertSame(emptyBuffer, result1, "Should return same buffer");
        
        // Equivalence class: Pre-filled StringBuffer
        StringBuffer prefilledBuffer = new StringBuffer("PREFIX:");
        StringBuffer result2 = composite.format("test", prefilledBuffer, new FieldPosition(0));
        assertEquals("PREFIX:FORMATTED", result2.toString(), "Prefilled buffer result");
        assertSame(prefilledBuffer, result2, "Should return same buffer");
        
        // Equivalence class: Large capacity StringBuffer
        StringBuffer largeBuffer = new StringBuffer(1000);
        StringBuffer result3 = composite.format("test", largeBuffer, new FieldPosition(0));
        assertEquals("FORMATTED", result3.toString(), "Large buffer result");
        assertSame(largeBuffer, result3, "Should return same buffer");
    }
    
    @Test
    public void testReformatInputClasses() throws ParseException {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo.append("[" + obj + "]");
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                if (source.matches("\\d+")) {
                    pos.setIndex(source.length());
                    return Integer.parseInt(source);
                } else if (source.matches("[a-zA-Z]+")) {
                    pos.setIndex(source.length());
                    return source.toUpperCase();
                }
                throw new RuntimeException("Cannot parse: " + source);
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, parser);
        
        // Equivalence class: Numeric string inputs
        String result1 = composite.reformat("123");
        assertEquals("[123]", result1, "Numeric string reformat");
        
        // Equivalence class: Alphabetic string inputs
        String result2 = composite.reformat("abc");
        assertEquals("[ABC]", result2, "Alphabetic string reformat");
        
        // Equivalence class: Invalid inputs (should throw ParseException)
        assertThrows(ParseException.class, () -> {
            composite.reformat("123abc");
        }, "Invalid input should throw ParseException");
    }
    
    @Test
    public void testFormatObjectTypeClasses() {
        // Test different object types that can be formatted
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        NumberFormat numberFormat = NumberFormat.getInstance();
        
        // Equivalence class: Date objects
        CompositeFormat dateComposite = new CompositeFormat(dateFormat, dateFormat);
        Date testDate = new Date(123, 11, 25); // December 25, 2023
        StringBuffer dateBuffer = new StringBuffer();
        dateComposite.format(testDate, dateBuffer, new FieldPosition(0));
        assertFalse(dateBuffer.toString().isEmpty(), "Date should be formatted");
        
        // Equivalence class: Number objects
        CompositeFormat numberComposite = new CompositeFormat(numberFormat, numberFormat);
        StringBuffer numberBuffer = new StringBuffer();
        numberComposite.format(1234.56, numberBuffer, new FieldPosition(0));
        assertFalse(numberBuffer.toString().isEmpty(), "Number should be formatted");
    }
}
