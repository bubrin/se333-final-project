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
 * Contract-based tests for CompositeFormat
 * Based on static analysis of CompositeFormat's contract obligations
 */
public class CompositeFormatContractTest {
    
    /**
     * Contract: Constructor must store provided parser and formatter references
     * Invariant: getParser() returns exactly the parser passed to constructor
     * Invariant: getFormatter() returns exactly the formatter passed to constructor
     */
    @Test
    public void testConstructorContract() {
        Format parser = new SimpleDateFormat("yyyy-MM-dd");
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Contract: Constructor stores exact references
        assertSame(parser, composite.getParser(), "Constructor must store exact parser reference");
        assertSame(formatter, composite.getFormatter(), "Constructor must store exact formatter reference");
        
        // Contract: References remain stable across multiple calls
        assertSame(composite.getParser(), composite.getParser(), "Parser reference must be stable");
        assertSame(composite.getFormatter(), composite.getFormatter(), "Formatter reference must be stable");
    }
    
    /**
     * Contract: format() method must delegate to formatter.format() with same parameters
     * Post-condition: Returns the same StringBuffer instance passed in
     * Post-condition: StringBuffer contains result from formatter.format()
     */
    @Test
    public void testFormatDelegationContract() {
        // Create a formatter that marks when it's called
        final boolean[] formatterCalled = {false};
        final Object[] capturedArgs = new Object[3];
        
        Format formatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                formatterCalled[0] = true;
                capturedArgs[0] = obj;
                capturedArgs[1] = toAppendTo;
                capturedArgs[2] = pos;
                return toAppendTo.append("DELEGATED");
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(new SimpleDateFormat("yyyy"), formatter);
        
        Object testObj = "test";
        StringBuffer testBuffer = new StringBuffer();
        FieldPosition testPos = new FieldPosition(0);
        
        // Contract: Must delegate to formatter
        StringBuffer result = composite.format(testObj, testBuffer, testPos);
        
        assertTrue(formatterCalled[0], "format() must delegate to formatter");
        assertSame(testObj, capturedArgs[0], "Must pass same object to formatter");
        assertSame(testBuffer, capturedArgs[1], "Must pass same buffer to formatter");
        assertSame(testPos, capturedArgs[2], "Must pass same position to formatter");
        assertSame(testBuffer, result, "Must return same buffer instance");
        assertEquals("DELEGATED", result.toString(), "Buffer must contain formatter result");
    }
    
    /**
     * Contract: reformat() method must be equivalent to format(parseObject(input))
     * Pre-condition: input must be parseable by parser
     * Post-condition: Returns string equivalent to formatting the parsed object
     * Exception contract: Throws ParseException if parsing fails
     */
    @Test
    public void testReformatContract() throws ParseException {
        Format parser = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo;
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                if ("VALID".equals(source)) {
                    pos.setIndex(source.length());
                    return "PARSED";
                }
                return null; // Parse failure
            }
        };
        
        Format formatter = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo.append("FORMATTED:" + obj);
            }
            
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
        
        CompositeFormat composite = new CompositeFormat(parser, formatter);
        
        // Contract: reformat(input) == format(parseObject(input))
        String reformatResult = composite.reformat("VALID");
        
        Object parsed = composite.parseObject("VALID");
        String manualResult = composite.format(parsed);
        
        assertEquals(manualResult, reformatResult, "reformat() must equal format(parseObject())");
        assertEquals("FORMATTED:PARSED", reformatResult, "Expected reformat result");
        
        // Contract: Must throw ParseException on parse failure
        assertThrows(ParseException.class, () -> {
            composite.reformat("INVALID");
        }, "reformat() must throw ParseException on parse failure");
    }
}
