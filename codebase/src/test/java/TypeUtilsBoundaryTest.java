import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.lang3.reflect.TypeUtils;
import java.lang.reflect.*;
import java.util.*;
import java.io.Serializable;

public class TypeUtilsBoundaryTest {
    
    @Test
    public void testIsAssignableBoundaryValues() {
        // Test basic primitive type boundaries
        assertTrue(TypeUtils.isAssignable(int.class, int.class));
        assertFalse(TypeUtils.isAssignable(int.class, long.class));
        
        // Test null boundaries
        assertTrue(TypeUtils.isAssignable(null, Object.class));
        assertTrue(TypeUtils.isAssignable(null, String.class));
        assertFalse(TypeUtils.isAssignable(null, int.class)); // primitive cannot be null
        assertFalse(TypeUtils.isAssignable(Object.class, null));
        
        // Test inheritance boundaries
        assertTrue(TypeUtils.isAssignable(String.class, Object.class));
        assertFalse(TypeUtils.isAssignable(Object.class, String.class));
        assertTrue(TypeUtils.isAssignable(ArrayList.class, List.class));
        assertFalse(TypeUtils.isAssignable(List.class, ArrayList.class));
        
        // Test array type boundaries
        assertTrue(TypeUtils.isAssignable(String[].class, Object[].class));
        assertFalse(TypeUtils.isAssignable(Object[].class, String[].class));
        assertTrue(TypeUtils.isAssignable(int[].class, int[].class));
        assertFalse(TypeUtils.isAssignable(int[].class, long[].class));
    }
    
    @Test
    public void testGetRawTypeBoundaryValues() {
        // Test simple class boundary
        assertEquals(String.class, TypeUtils.getRawType(String.class, null));
        assertEquals(Object.class, TypeUtils.getRawType(Object.class, null));
        
        // Test array type boundaries
        assertEquals(String[].class, TypeUtils.getRawType(String[].class, null));
        assertEquals(int[].class, TypeUtils.getRawType(int[].class, null));
        
        // Test with parameterized types
        Field listField;
        try {
            listField = TestClassWithGenerics.class.getDeclaredField("stringList");
            Type fieldType = listField.getGenericType();
            assertEquals(List.class, TypeUtils.getRawType(fieldType, TestClassWithGenerics.class));
        } catch (NoSuchFieldException e) {
            fail("Test setup failed");
        }
    }
    
    @Test 
    public void testGetTypeArgumentsBoundaryValues() {
        try {
            // Test with parameterized type
            Field mapField = TestClassWithGenerics.class.getDeclaredField("stringIntMap");
            ParameterizedType mapType = (ParameterizedType) mapField.getGenericType();
            
            Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(mapType);
            assertEquals(2, typeArgs.size());
            
            // Test with raw type (no type arguments)
            Map<TypeVariable<?>, Type> rawTypeArgs = TypeUtils.getTypeArguments(String.class, String.class);
            assertNotNull(rawTypeArgs);
            
        } catch (NoSuchFieldException e) {
            fail("Test setup failed");
        }
    }
    
    @Test
    public void testIsInstanceBoundaryValues() {
        // Test null values
        assertFalse(TypeUtils.isInstance(null, String.class));
        assertTrue(TypeUtils.isInstance(null, Object.class)); // null is instance of Object type
        
        // Test basic type checking
        assertTrue(TypeUtils.isInstance("test", String.class));
        assertFalse(TypeUtils.isInstance("test", Integer.class));
        assertTrue(TypeUtils.isInstance("test", Object.class));
        
        // Test array instances
        assertTrue(TypeUtils.isInstance(new String[]{"test"}, String[].class));
        assertFalse(TypeUtils.isInstance(new String[]{"test"}, Integer[].class));
        assertTrue(TypeUtils.isInstance(new int[]{1, 2, 3}, int[].class));
        
        // Test primitive instances
        assertTrue(TypeUtils.isInstance(42, int.class));
        assertTrue(TypeUtils.isInstance(42, Integer.class));
        assertFalse(TypeUtils.isInstance(42L, int.class));
        assertTrue(TypeUtils.isInstance(42L, long.class));
    }
    
    @Test
    public void testDetermineTypeArgumentsBoundaryValues() {
        // Test simple inheritance
        Map<TypeVariable<?>, Type> args1 = TypeUtils.determineTypeArguments(ArrayList.class, List.class);
        assertNotNull(args1);
        
        // Test with concrete generic class
        Map<TypeVariable<?>, Type> args2 = TypeUtils.determineTypeArguments(StringList.class, List.class);
        assertNotNull(args2);
        
        // Test with non-assignable types
        assertThrows(IllegalArgumentException.class, 
            () -> TypeUtils.determineTypeArguments(String.class, List.class));
        
        // Test with same class
        Map<TypeVariable<?>, Type> args3 = TypeUtils.determineTypeArguments(String.class, String.class);
        assertNotNull(args3);
        assertEquals(0, args3.size());
        
        // Test with ArrayList to Object
        Map<TypeVariable<?>, Type> args4 = TypeUtils.determineTypeArguments(ArrayList.class, Object.class);
        assertNotNull(args4);
    }
    
    @Test
    public void testNormalizeUpperBoundsBoundaryValues() {
        // Test empty bounds array
        Type[] emptyBounds = {};
        Type[] normalized1 = TypeUtils.normalizeUpperBounds(emptyBounds);
        assertEquals(1, normalized1.length);
        assertEquals(Object.class, normalized1[0]);
        
        // Test single bound
        Type[] singleBound = {String.class};
        Type[] normalized2 = TypeUtils.normalizeUpperBounds(singleBound);
        assertEquals(1, normalized2.length);
        assertEquals(String.class, normalized2[0]);
        
        // Test multiple bounds
        Type[] multipleBounds = {CharSequence.class, Serializable.class};
        Type[] normalized3 = TypeUtils.normalizeUpperBounds(multipleBounds);
        assertEquals(2, normalized3.length);
        assertTrue(Arrays.asList(normalized3).contains(CharSequence.class));
        assertTrue(Arrays.asList(normalized3).contains(Serializable.class));
        
        // Test null bounds array
        Type[] normalized4 = TypeUtils.normalizeUpperBounds(null);
        assertEquals(1, normalized4.length);
        assertEquals(Object.class, normalized4[0]);
    }
    
    @Test
    public void testIsArrayTypeBoundaryValues() {
        // Test simple array types
        assertTrue(TypeUtils.isArrayType(String[].class));
        assertTrue(TypeUtils.isArrayType(int[].class));
        assertTrue(TypeUtils.isArrayType(Object[].class));
        
        // Test multi-dimensional arrays
        assertTrue(TypeUtils.isArrayType(String[][].class));
        assertTrue(TypeUtils.isArrayType(int[][][].class));
        
        // Test non-array types
        assertFalse(TypeUtils.isArrayType(String.class));
        assertFalse(TypeUtils.isArrayType(int.class));
        assertFalse(TypeUtils.isArrayType(List.class));
        assertFalse(TypeUtils.isArrayType(Object.class));
        
        // Test null type
        assertFalse(TypeUtils.isArrayType(null));
    }
    
    @Test
    public void testGetArrayComponentTypeBoundaryValues() {
        // Test simple array component types
        assertEquals(String.class, TypeUtils.getArrayComponentType(String[].class));
        assertEquals(int.class, TypeUtils.getArrayComponentType(int[].class));
        assertEquals(Object.class, TypeUtils.getArrayComponentType(Object[].class));
        
        // Test multi-dimensional arrays
        assertEquals(String[].class, TypeUtils.getArrayComponentType(String[][].class));
        assertEquals(int[][].class, TypeUtils.getArrayComponentType(int[][][].class));
        
        // Test non-array types (should return null)
        assertNull(TypeUtils.getArrayComponentType(String.class));
        assertNull(TypeUtils.getArrayComponentType(int.class));
        assertNull(TypeUtils.getArrayComponentType(List.class));
        
        // Test null type
        assertNull(TypeUtils.getArrayComponentType(null));
    }
    
    // Helper classes for testing
    private static class TestClassWithGenerics {
        private List<String> stringList;
        private Map<String, Integer> stringIntMap;
        private Set<Number> numberSet;
    }
    
    private static class StringList extends ArrayList<String> {
        // Concrete generic class for testing
    }
}
