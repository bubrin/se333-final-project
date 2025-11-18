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
 * Decision table tests for CompositeFormat
 * Based on static analysis of conditional logic in CompositeFormat
 */
public class CompositeFormatDecisionTableTest {
    
    /**
     * Decision Table for format() method:
     * 
     * Input Object | Formatter | Expected Behavior
     * -------------|-----------|------------------
     * null         | valid     | Delegate to formatter with null
     * non-null     | valid     | Delegate to formatter with object
     * any          | null      | NullPointerException (expected)
     */
    @Test
    public void testFormatDecisionTable() {
        Format validFormatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                if (obj == null) {
                    return toAppendTo.append("NULL_OBJECT");
                }
                return toAppendTo.append("NON_NULL:" + obj.toString());
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        // Test case 1: null object, valid formatter
        CompositeFormat composite1 = new CompositeFormat(new SimpleDateFormat("yyyy"), validFormatter);
        StringBuffer buffer1 = new StringBuffer();
        composite1.format(null, buffer1, new FieldPosition(0));
        assertEquals("NULL_OBJECT", buffer1.toString(), "Null object with valid formatter");
        
        // Test case 2: non-null object, valid formatter
        StringBuffer buffer2 = new StringBuffer();
        composite1.format("test", buffer2, new FieldPosition(0));
        assertEquals("NON_NULL:test", buffer2.toString(), "Non-null object with valid formatter");
        
        // Test case 3: any object, null formatter - should cause NPE when called
        CompositeFormat composite3 = new CompositeFormat(new SimpleDateFormat("yyyy"), null);
        assertThrows(NullPointerException.class, () -> {
            composite3.format("test", new StringBuffer(), new FieldPosition(0));
        }, "Null formatter should cause NPE");
    }
    
    /**
     * Decision Table for parseObject() method:
     * 
     * Input String | Parser | Expected Behavior
     * -------------|--------|------------------
     * null         | valid  | Delegate to parser with null
     * empty        | valid  | Delegate to parser with empty string
     * valid        | valid  | Delegate to parser with string
     * any          | null   | NullPointerException (expected)
     */
    @Test
    public void testParseObjectDecisionTable() {
        Format validParser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                if (source == null) {
                    return "NULL_STRING";
                } else if (source.isEmpty()) {
                    pos.setIndex(0);
                    return "EMPTY_STRING";
                } else {
                    pos.setIndex(source.length());
                    return "VALID:" + source;
                }
            }
        };
        
        // Test case 1: null string, valid parser
        CompositeFormat composite1 = new CompositeFormat(validParser, new SimpleDateFormat("yyyy"));
        Object result1 = composite1.parseObject(null, new ParsePosition(0));
        assertEquals("NULL_STRING", result1, "Null string with valid parser");
        
        // Test case 2: empty string, valid parser
        ParsePosition pos2 = new ParsePosition(0);
        Object result2 = composite1.parseObject("", pos2);
        assertEquals("EMPTY_STRING", result2, "Empty string with valid parser");
        assertEquals(0, pos2.getIndex(), "Position should be set");
        
        // Test case 3: valid string, valid parser
        ParsePosition pos3 = new ParsePosition(0);
        Object result3 = composite1.parseObject("test", pos3);
        assertEquals("VALID:test", result3, "Valid string with valid parser");
        assertEquals(4, pos3.getIndex(), "Position should advance");
        
        // Test case 4: any string, null parser - should cause NPE when called
        CompositeFormat composite4 = new CompositeFormat(null, new SimpleDateFormat("yyyy"));
        assertThrows(NullPointerException.class, () -> {
            composite4.parseObject("test", new ParsePosition(0));
        }, "Null parser should cause NPE");
    }
    
    /**
     * Decision Table for reformat() method:
     * 
     * Parse Success | Format Success | Expected Behavior
     * --------------|----------------|------------------
     * true          | true           | Return formatted string
     * true          | false          | Runtime exception from formatter
     * false         | N/A            | ParseException
     */
    @Test
    public void testReformatDecisionTable() {
        // Test case 1: Parse success, format success
        Format successParser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                pos.setIndex(source.length());
                return "parsed:" + source;
            }
        };
        
        Format successFormatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo.append("formatted:" + obj.toString());
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        CompositeFormat composite1 = new CompositeFormat(successParser, successFormatter);
        try {
            String result1 = composite1.reformat("input");
            assertEquals("formatted:parsed:input", result1, "Parse and format success");
        } catch (ParseException e) {
            fail("Should not throw ParseException when both succeed");
        }
        
        // Test case 2: Parse success, format fails (throws exception)
        Format failFormatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                throw new RuntimeException("Format failed");
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        CompositeFormat composite2 = new CompositeFormat(successParser, failFormatter);
        assertThrows(RuntimeException.class, () -> {
            composite2.reformat("input");
        }, "Format failure should propagate exception");
        
        // Test case 3: Parse fails
        Format failParser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null; // Parse failure
            }
        };
        
        CompositeFormat composite3 = new CompositeFormat(failParser, successFormatter);
        assertThrows(ParseException.class, () -> {
            composite3.reformat("input");
        }, "Parse failure should throw ParseException");
    }
    
    /**
     * Decision Table for constructor behavior:
     * 
     * Parser | Formatter | Stored Parser | Stored Formatter
     * -------|-----------|---------------|------------------
     * null   | null      | null          | null
     * null   | valid     | null          | valid
     * valid  | null      | valid         | null
     * valid  | valid     | valid         | valid
     */
    @Test
    public void testConstructorDecisionTable() {
        Format validParser = new SimpleDateFormat("yyyy-MM-dd");
        Format validFormatter = new SimpleDateFormat("MM/dd/yyyy");
        
        // Test case 1: null parser, null formatter
        CompositeFormat composite1 = new CompositeFormat(null, null);
        assertNull(composite1.getParser(), "Null parser stored");
        assertNull(composite1.getFormatter(), "Null formatter stored");
        
        // Test case 2: null parser, valid formatter
        CompositeFormat composite2 = new CompositeFormat(null, validFormatter);
        assertNull(composite2.getParser(), "Null parser stored");
        assertSame(validFormatter, composite2.getFormatter(), "Valid formatter stored");
        
        // Test case 3: valid parser, null formatter
        CompositeFormat composite3 = new CompositeFormat(validParser, null);
        assertSame(validParser, composite3.getParser(), "Valid parser stored");
        assertNull(composite3.getFormatter(), "Null formatter stored");
        
        // Test case 4: valid parser, valid formatter
        CompositeFormat composite4 = new CompositeFormat(validParser, validFormatter);
        assertSame(validParser, composite4.getParser(), "Valid parser stored");
        assertSame(validFormatter, composite4.getFormatter(), "Valid formatter stored");
    }
    
    /**
     * Decision Table for ParsePosition state handling:
     * 
     * Initial Index | Parse Success | Final Index | Error Index
     * --------------|---------------|-------------|------------
     * 0             | true          | > 0         | -1
     * 0             | false         | 0           | 0
     * > 0           | true          | advanced    | -1
     * > 0           | false         | unchanged   | original
     */
    @Test
    public void testParsePositionDecisionTable() {
        Format conditionalParser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                int index = pos.getIndex();
                if (index < source.length() && source.charAt(index) == 'S') {
                    // Success case
                    pos.setIndex(index + 1);
                    return "success";
                } else {
                    // Failure case
                    pos.setErrorIndex(index);
                    return null;
                }
            }
        };
        
        CompositeFormat composite = new CompositeFormat(conditionalParser, new SimpleDateFormat("yyyy"));
        
        // Test case 1: Initial index 0, parse success
        ParsePosition pos1 = new ParsePosition(0);
        Object result1 = composite.parseObject("SUCCESS", pos1);
        assertEquals("success", result1, "Parse success result");
        assertEquals(1, pos1.getIndex(), "Index should advance");
        assertEquals(-1, pos1.getErrorIndex(), "Error index should be -1");
        
        // Test case 2: Initial index 0, parse failure
        ParsePosition pos2 = new ParsePosition(0);
        Object result2 = composite.parseObject("FAILURE", pos2);
        assertNull(result2, "Parse failure result");
        assertEquals(0, pos2.getIndex(), "Index should remain 0");
        assertEquals(0, pos2.getErrorIndex(), "Error index should be 0");
        
        // Test case 3: Initial index > 0, parse success
        ParsePosition pos3 = new ParsePosition(2);
        Object result3 = composite.parseObject("__SUCCESS", pos3);
        assertEquals("success", result3, "Parse success result");
        assertEquals(3, pos3.getIndex(), "Index should advance");
        assertEquals(-1, pos3.getErrorIndex(), "Error index should be -1");
        
        // Test case 4: Initial index > 0, parse failure
        ParsePosition pos4 = new ParsePosition(2);
        Object result4 = composite.parseObject("__FAILURE", pos4);
        assertNull(result4, "Parse failure result");
        assertEquals(2, pos4.getIndex(), "Index should remain unchanged");
        assertEquals(2, pos4.getErrorIndex(), "Error index should be at position");
    }
}
