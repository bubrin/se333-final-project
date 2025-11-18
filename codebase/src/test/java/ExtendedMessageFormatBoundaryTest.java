import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.lang3.text.ExtendedMessageFormat;
import java.text.*;
import java.util.*;

public class ExtendedMessageFormatBoundaryTest {
    
    @Test
    public void testNullPatternBoundary() {
        // Test null pattern
        assertThrows(IllegalArgumentException.class, 
            () -> new ExtendedMessageFormat(null));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new ExtendedMessageFormat(null, Locale.getDefault()));
    }
    
    @Test
    public void testEmptyPatternBoundary() {
        // Test empty pattern
        ExtendedMessageFormat emptyFormat = new ExtendedMessageFormat("");
        assertEquals("", emptyFormat.format(new Object[]{}));
        
        ExtendedMessageFormat emptyWithLocale = new ExtendedMessageFormat("", Locale.US);
        assertEquals("", emptyWithLocale.format(new Object[]{}));
    }
    
    @Test
    public void testSimplePatternBoundary() {
        // Test pattern with no placeholders
        String simplePattern = "Hello World";
        ExtendedMessageFormat simpleFormat = new ExtendedMessageFormat(simplePattern);
        assertEquals("Hello World", simpleFormat.format(new Object[]{}));
    }
    
    @Test
    public void testSingleArgumentBoundary() {
        // Test pattern with single argument
        ExtendedMessageFormat singleArg = new ExtendedMessageFormat("Hello {0}");
        assertEquals("Hello World", singleArg.format(new Object[]{"World"}));
        
        // Test with null argument
        assertEquals("Hello null", singleArg.format(new Object[]{null}));
        
        // Test with empty string argument
        assertEquals("Hello ", singleArg.format(new Object[]{""}));
    }
    
    @Test
    public void testMultipleArgumentsBoundary() {
        // Test pattern with multiple arguments
        ExtendedMessageFormat multiArgs = new ExtendedMessageFormat("Hello {0}, you have {1} messages");
        assertEquals("Hello John, you have 5 messages", 
            multiArgs.format(new Object[]{"John", 5}));
        
        // Test with insufficient arguments
        assertEquals("Hello John, you have {1} messages", 
            multiArgs.format(new Object[]{"John"}));
        
        // Test with excess arguments
        assertEquals("Hello John, you have 5 messages", 
            multiArgs.format(new Object[]{"John", 5, "extra"}));
    }
    
    @Test
    public void testArgumentIndexBoundaries() {
        // Test maximum argument index
        ExtendedMessageFormat maxIndex = new ExtendedMessageFormat("Value: {999}");
        Object[] args = new Object[1000];
        args[999] = "MaxValue";
        assertEquals("Value: MaxValue", maxIndex.format(args));
        
        // Test argument index 0
        ExtendedMessageFormat zeroIndex = new ExtendedMessageFormat("Value: {0}");
        assertEquals("Value: FirstValue", zeroIndex.format(new Object[]{"FirstValue"}));
        
        // Test non-sequential argument indices
        ExtendedMessageFormat nonSequential = new ExtendedMessageFormat("Values: {2}, {0}, {1}");
        assertEquals("Values: Third, First, Second", 
            nonSequential.format(new Object[]{"First", "Second", "Third"}));
    }
    
    @Test
    public void testFormatTypeBoundaries() {
        // Test number format
        ExtendedMessageFormat numberFormat = new ExtendedMessageFormat("Value: {0,number}");
        assertEquals("Value: 123", numberFormat.format(new Object[]{123}));
        assertEquals("Value: 123.45", numberFormat.format(new Object[]{123.45}));
        
        // Test date format
        ExtendedMessageFormat dateFormat = new ExtendedMessageFormat("Date: {0,date}");
        Date testDate = new Date(0); // Jan 1, 1970
        String result = dateFormat.format(new Object[]{testDate});
        assertTrue(result.startsWith("Date: "));
        
        // Test choice format
        ExtendedMessageFormat choiceFormat = new ExtendedMessageFormat(
            "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}.");
        assertEquals("There are no files.", choiceFormat.format(new Object[]{0}));
        assertEquals("There is one file.", choiceFormat.format(new Object[]{1}));
        assertEquals("There are 5 files.", choiceFormat.format(new Object[]{5}));
    }
    
    @Test
    public void testSpecialCharactersBoundary() {
        // Test single quotes (escape characters)
        ExtendedMessageFormat singleQuotes = new ExtendedMessageFormat("Don''t {0}");
        assertEquals("Don't stop", singleQuotes.format(new Object[]{"stop"}));
        
        // Test curly braces in text
        ExtendedMessageFormat curlyBraces = new ExtendedMessageFormat("'{'{0}'}'");
        assertEquals("{value}", curlyBraces.format(new Object[]{"value"}));
        
        // Test special characters in arguments
        ExtendedMessageFormat specialChars = new ExtendedMessageFormat("Message: {0}");
        assertEquals("Message: Hello\nWorld\t!", 
            specialChars.format(new Object[]{"Hello\nWorld\t!"}));
    }
    
    @Test
    public void testLocaleBoundaries() {
        // Test with different locales
        ExtendedMessageFormat usFormat = new ExtendedMessageFormat("Value: {0,number,currency}", Locale.US);
        ExtendedMessageFormat frFormat = new ExtendedMessageFormat("Value: {0,number,currency}", Locale.FRANCE);
        
        String usResult = usFormat.format(new Object[]{123.45});
        String frResult = frFormat.format(new Object[]{123.45});
        
        assertNotNull(usResult);
        assertNotNull(frResult);
        assertTrue(usResult.contains("123"));
        assertTrue(frResult.contains("123"));
    }
    
    @Test
    public void testNullArgumentsBoundary() {
        ExtendedMessageFormat format = new ExtendedMessageFormat("Value: {0}");
        
        // Test null arguments array
        assertThrows(NullPointerException.class, 
            () -> format.format((Object[])null));
        
        // Test null argument within array
        assertEquals("Value: null", format.format(new Object[]{null}));
    }
    
    @Test
    public void testLongPatternBoundary() {
        // Test very long pattern
        StringBuilder longPattern = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPattern.append("Value ").append(i).append(": {").append(i % 10).append("} ");
        }
        
        ExtendedMessageFormat longFormat = new ExtendedMessageFormat(longPattern.toString());
        Object[] args = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        String result = longFormat.format(args);
        
        assertNotNull(result);
        assertTrue(result.length() > 1000);
        assertTrue(result.contains("Value 0: A"));
        assertTrue(result.contains("Value 999: J"));
    }
    
    @Test
    public void testInvalidPatternBoundaries() {
        // Test unclosed placeholder
        assertThrows(IllegalArgumentException.class, 
            () -> new ExtendedMessageFormat("Hello {0"));
        
        // Test invalid format type
        ExtendedMessageFormat invalidType = new ExtendedMessageFormat("Value: {0,invalid}");
        // Should not throw exception during construction, but might during formatting
        assertDoesNotThrow(() -> invalidType.format(new Object[]{"test"}));
        
        // Test malformed choice pattern
        assertThrows(IllegalArgumentException.class, 
            () -> new ExtendedMessageFormat("Value: {0,choice,invalid}"));
    }
    
    @Test
    public void testApplyPatternBoundary() {
        ExtendedMessageFormat format = new ExtendedMessageFormat("Initial: {0}");
        assertEquals("Initial: test", format.format(new Object[]{"test"}));
        
        // Test applying new pattern
        format.applyPattern("New: {0}");
        assertEquals("New: test", format.format(new Object[]{"test"}));
        
        // Test applying null pattern
        assertThrows(IllegalArgumentException.class, 
            () -> format.applyPattern(null));
        
        // Test applying empty pattern
        format.applyPattern("");
        assertEquals("", format.format(new Object[]{}));
    }
}
