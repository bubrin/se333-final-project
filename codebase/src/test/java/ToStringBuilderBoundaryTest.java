import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ToStringBuilderBoundaryTest {
    
    private static class TestObject {
        private int intField = 42;
        private String stringField = "test";
        private boolean boolField = true;
        private Object nullField = null;
    }
    
    @Test
    public void testNullObjectBoundary() {
        // Test ToStringBuilder with null object
        assertThrows(NullPointerException.class, 
            () -> new ToStringBuilder(null));
    }
    
    @Test
    public void testNullStyleBoundary() {
        TestObject obj = new TestObject();
        
        // Test with null style (should use default)
        ToStringBuilder builder = new ToStringBuilder(obj, null);
        String result = builder.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestObject"));
    }
    
    @Test
    public void testEmptyBuilderBoundary() {
        TestObject obj = new TestObject();
        
        // Test builder with no fields added
        ToStringBuilder emptyBuilder = new ToStringBuilder(obj);
        String result = emptyBuilder.toString();
        assertNotNull(result);
        assertTrue(result.contains("TestObject"));
        assertFalse(result.contains("intField"));
    }
    
    @Test
    public void testMaximumFieldsBoundary() {
        TestObject obj = new TestObject();
        ToStringBuilder builder = new ToStringBuilder(obj);
        
        // Add many fields to test boundary
        for (int i = 0; i < 1000; i++) {
            builder.append("field" + i, i);
        }
        
        String result = builder.toString();
        assertNotNull(result);
        assertTrue(result.contains("field0"));
        assertTrue(result.contains("field999"));
        assertTrue(result.length() > 1000); // Should be quite long
    }
    
    @Test
    public void testNullFieldValuesBoundary() {
        TestObject obj = new TestObject();
        ToStringBuilder builder = new ToStringBuilder(obj);
        
        // Test adding null values
        builder.append("nullString", (String)null);
        builder.append("nullObject", (Object)null);
        builder.append("nullArray", (String[])null);
        
        String result = builder.toString();
        assertNotNull(result);
        assertTrue(result.contains("nullString"));
        assertTrue(result.contains("<null>") || result.contains("null"));
    }
    
    @Test
    public void testArrayBoundaries() {
        TestObject obj = new TestObject();
        ToStringBuilder builder = new ToStringBuilder(obj);
        
        // Test empty arrays
        builder.append("emptyIntArray", new int[0]);
        builder.append("emptyStringArray", new String[0]);
        
        // Test large arrays
        int[] largeArray = new int[1000];
        for (int i = 0; i < 1000; i++) {
            largeArray[i] = i;
        }
        builder.append("largeArray", largeArray);
        
        String result = builder.toString();
        assertNotNull(result);
        assertTrue(result.contains("emptyIntArray"));
        assertTrue(result.contains("largeArray"));
    }
    
    @Test
    public void testFieldNameBoundaries() {
        TestObject obj = new TestObject();
        ToStringBuilder builder = new ToStringBuilder(obj);
        
        // Test empty field name
        builder.append("", "value");
        
        // Test very long field name
        String longFieldName = "a".repeat(1000);
        builder.append(longFieldName, "value");
        
        // Test special characters in field name
        builder.append("field$with%special@characters", "value");
        
        // Test null field name (should throw exception or handle gracefully)
        assertDoesNotThrow(() -> builder.append(null, "value"));
        
        String result = builder.toString();
        assertNotNull(result);
        assertTrue(result.contains(longFieldName));
        assertTrue(result.contains("field$with%special@characters"));
    }
    
    @Test
    public void testDifferentToStringStylesBoundary() {
        TestObject obj = new TestObject();
        
        // Test with different predefined styles
        ToStringStyle[] styles = {
            ToStringStyle.DEFAULT_STYLE,
            ToStringStyle.SHORT_PREFIX_STYLE,
            ToStringStyle.NO_FIELD_NAMES_STYLE,
            ToStringStyle.MULTI_LINE_STYLE,
            ToStringStyle.SIMPLE_STYLE
        };
        
        for (ToStringStyle style : styles) {
            ToStringBuilder builder = new ToStringBuilder(obj, style);
            builder.append("testField", "testValue");
            
            String result = builder.toString();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
    
    @Test
    public void testStringValuesBoundary() {
        TestObject obj = new TestObject();
        ToStringBuilder builder = new ToStringBuilder(obj);
        
        // Test empty string
        builder.append("emptyString", "");
        
        // Test very long string
        String longString = "x".repeat(10000);
        builder.append("longString", longString);
        
        // Test string with special characters
        builder.append("specialChars", "Line1\nLine2\tTabbed\"Quoted\"");
        
        // Test string with unicode
        builder.append("unicode", "Hello 世界 🌍");
        
        String result = builder.toString();
        assertNotNull(result);
        assertTrue(result.contains("emptyString"));
        assertTrue(result.contains(longString));
        assertTrue(result.contains("specialChars"));
        assertTrue(result.contains("unicode"));
    }
    
    @Test
    public void testNumericBoundaryValues() {
        TestObject obj = new TestObject();
        ToStringBuilder builder = new ToStringBuilder(obj);
        
        // Test extreme numeric values
        builder.append("maxInt", Integer.MAX_VALUE);
        builder.append("minInt", Integer.MIN_VALUE);
        builder.append("maxLong", Long.MAX_VALUE);
        builder.append("minLong", Long.MIN_VALUE);
        builder.append("maxDouble", Double.MAX_VALUE);
        builder.append("minDouble", Double.MIN_VALUE);
        builder.append("positiveInfinity", Double.POSITIVE_INFINITY);
        builder.append("negativeInfinity", Double.NEGATIVE_INFINITY);
        builder.append("nan", Double.NaN);
        
        String result = builder.toString();
        assertNotNull(result);
        assertTrue(result.contains(String.valueOf(Integer.MAX_VALUE)));
        assertTrue(result.contains(String.valueOf(Long.MIN_VALUE)));
        assertTrue(result.contains("Infinity"));
        assertTrue(result.contains("NaN"));
    }
}
