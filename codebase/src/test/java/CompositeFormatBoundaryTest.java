import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.lang3.text.CompositeFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Boundary value tests for CompositeFormat
 * Based on static analysis of CompositeFormat implementation
 */
public class CompositeFormatBoundaryTest {
    
    @Test
    public void testNullFormatConstructor() {
        // Boundary: null parser
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        CompositeFormat composite1 = new CompositeFormat(null, formatter);
        assertNull(composite1.getParser(), "Should handle null parser");
        assertSame(formatter, composite1.getFormatter(), "Should store formatter");
        
        // Boundary: null formatter  
        Format parser = new SimpleDateFormat("MM/dd/yyyy");
        CompositeFormat composite2 = new CompositeFormat(parser, null);
        assertSame(parser, composite2.getParser(), "Should store parser");
        assertNull(composite2.getFormatter(), "Should handle null formatter");
        
        // Boundary: both null
        CompositeFormat composite3 = new CompositeFormat(null, null);
        assertNull(composite3.getParser(), "Should handle null parser");
        assertNull(composite3.getFormatter(), "Should handle null formatter");
    }
    
    @Test
    public void testEmptyStringParsing() {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                if (source.isEmpty()) {
                    return "empty";
                }
                return null;
            }
        };
        
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Boundary: empty string
        ParsePosition pos = new ParsePosition(0);
        Object result = composite.parseObject("", pos);
        assertEquals("empty", result, "Should handle empty string");
    }
    
    @Test
    public void testZeroLengthBuffer() {
        Format parser = new SimpleDateFormat("yyyy-MM-dd");
        Format formatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo; // Don't append anything
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Boundary: zero-length buffer
        StringBuffer buffer = new StringBuffer();
        StringBuffer result = composite.format("test", buffer, new FieldPosition(0));
        
        assertSame(buffer, result, "Should return same buffer");
        assertEquals(0, result.length(), "Buffer should remain empty");
    }
    
    @Test
    public void testParsePositionBoundaries() {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                // Test boundary positions
                int index = pos.getIndex();
                if (index < source.length()) {
                    pos.setIndex(source.length()); // Set to end
                    return "parsed";
                }
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, new SimpleDateFormat("yyyy"));
        
        // Boundary: position at start
        ParsePosition startPos = new ParsePosition(0);
        Object result1 = composite.parseObject("test", startPos);
        assertEquals("parsed", result1, "Should parse from start");
        assertEquals(4, startPos.getIndex(), "Should advance to end");
        
        // Boundary: position at end
        ParsePosition endPos = new ParsePosition(4);
        Object result2 = composite.parseObject("test", endPos);
        assertNull(result2, "Should not parse from end position");
    }
    
    @Test
    public void testFieldPositionBoundaries() {
        Format parser = new SimpleDateFormat("yyyy-MM-dd");
        Format formatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                // Test field position boundaries
                pos.setBeginIndex(0);
                pos.setEndIndex(toAppendTo.length());
                return toAppendTo.append("formatted");
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Boundary: FieldPosition with initial values
        FieldPosition pos = new FieldPosition(0);
        assertEquals(0, pos.getBeginIndex(), "Initial begin index");
        assertEquals(0, pos.getEndIndex(), "Initial end index");
        
        StringBuffer buffer = new StringBuffer();
        composite.format("test", buffer, pos);
        
        // Verify boundaries were set by formatter
        assertTrue(pos.getEndIndex() >= 0, "End index should be updated");
        assertTrue(pos.getBeginIndex() >= 0, "Begin index should be valid");
    }
    
    @Test
    public void testMaxStringLengthHandling() {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                // Handle very long strings
                return source.length() > 1000 ? "long" : "short";
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, new SimpleDateFormat("yyyy"));
        
        // Boundary: very long string
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 1500; i++) {
            longString.append('a');
        }
        
        try {
            Object result = composite.parseObject(longString.toString());
            assertEquals("long", result, "Should handle long string");

            // Boundary: normal string
            Object result2 = composite.parseObject("short");
            assertEquals("short", result2, "Should handle normal string");
        } catch (ParseException e) {
            fail("Should not throw ParseException: " + e.getMessage());
        }
    }
    
    @Test
    public void testReformatBoundaries() throws ParseException {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                if (source == null) {
                    return null;
                }
                pos.setIndex(source.length());
                return "parsed:" + source;
            }
        };
        
        Format formatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                if (obj == null) {
                    return toAppendTo.append("null");
                }
                return toAppendTo.append("formatted:" + obj.toString());
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Boundary: single character
        String result1 = composite.reformat("a");
        assertEquals("formatted:parsed:a", result1, "Should reformat single char");
        
        // Boundary: empty string
        String result2 = composite.reformat("");
        assertEquals("formatted:parsed:", result2, "Should reformat empty string");
    }
}
