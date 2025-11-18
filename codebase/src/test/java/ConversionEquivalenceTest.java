import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.lang3.Conversion;

public class ConversionEquivalenceTest {
    
    @Test
    public void testHexDigitEquivalenceClasses() {
        // Equivalence Class 1: Numeric digits (0-9)
        for (char digit = '0'; digit <= '9'; digit++) {
            int expected = digit - '0';
            assertEquals(expected, Conversion.hexDigitToInt(digit));
        }
        
        // Equivalence Class 2: Lowercase letters (a-f)
        for (char letter = 'a'; letter <= 'f'; letter++) {
            int expected = 10 + (letter - 'a');
            assertEquals(expected, Conversion.hexDigitToInt(letter));
        }
        
        // Equivalence Class 3: Uppercase letters (A-F)
        for (char letter = 'A'; letter <= 'F'; letter++) {
            int expected = 10 + (letter - 'A');
            assertEquals(expected, Conversion.hexDigitToInt(letter));
        }
        
        // Equivalence Class 4: Invalid characters
        char[] invalidChars = {'g', 'G', 'z', 'Z', ' ', '@', '!', '\n'};
        for (char invalidChar : invalidChars) {
            assertThrows(IllegalArgumentException.class, 
                () -> Conversion.hexDigitToInt(invalidChar));
        }
    }
    
    @Test
    public void testIntToHexDigitEquivalenceClasses() {
        // Equivalence Class 1: Single digit numbers (0-9)
        for (int i = 0; i <= 9; i++) {
            char expected = (char) ('0' + i);
            assertEquals(expected, Conversion.intToHexDigit(i));
        }
        
        // Equivalence Class 2: Hex letters (10-15)
        for (int i = 10; i <= 15; i++) {
            char expected = (char) ('a' + (i - 10));
            assertEquals(expected, Conversion.intToHexDigit(i));
        }
        
        // Equivalence Class 3: Invalid negative numbers
        int[] invalidNegatives = {-1, -10, -100, Integer.MIN_VALUE};
        for (int invalid : invalidNegatives) {
            assertThrows(IllegalArgumentException.class, 
                () -> Conversion.intToHexDigit(invalid));
        }
        
        // Equivalence Class 4: Invalid large positive numbers
        int[] invalidPositives = {16, 17, 100, Integer.MAX_VALUE};
        for (int invalid : invalidPositives) {
            assertThrows(IllegalArgumentException.class, 
                () -> Conversion.intToHexDigit(invalid));
        }
    }
    
    @Test
    public void testByteArrayToIntEquivalenceClasses() {
        // Equivalence Class 1: Single byte arrays
        byte[] singleByte = {42};
        assertEquals(42, Conversion.byteArrayToInt(singleByte, 0, 0, 0, 1));
        
        // Equivalence Class 2: Multi-byte little-endian arrays
        byte[] littleEndian = {0x12, 0x34, 0x56, 0x78};
        int expectedLE = 0x78563412; // Little-endian interpretation
        assertEquals(expectedLE, Conversion.byteArrayToInt(littleEndian, 0, 0, 0, 4));
        
        // Equivalence Class 3: Arrays with zero bytes
        byte[] withZeros = {0x00, 0x12, 0x00, 0x34};
        int expectedZeros = 0x34001200;
        assertEquals(expectedZeros, Conversion.byteArrayToInt(withZeros, 0, 0, 0, 4));
        
        // Equivalence Class 4: Arrays with negative bytes (sign extension)
        byte[] negativeBytes = {(byte)0xFF, (byte)0xFF};
        int expectedNegative = 0xFFFF; // No sign extension for partial reads
        assertEquals(expectedNegative, Conversion.byteArrayToInt(negativeBytes, 0, 0, 0, 2));
        
        // Equivalence Class 5: Partial array reads
        byte[] largeArray = {0x11, 0x22, 0x33, 0x44, 0x55, 0x66};
        assertEquals(0x2211, Conversion.byteArrayToInt(largeArray, 0, 0, 0, 2));
        assertEquals(0x4433, Conversion.byteArrayToInt(largeArray, 2, 0, 0, 2));
        assertEquals(0x6655, Conversion.byteArrayToInt(largeArray, 4, 0, 0, 2));
    }
    
    @Test
    public void testHexStringEquivalenceClasses() {
        // Equivalence Class 1: Single hex digit strings
        assertEquals(0, Conversion.hexToInt("0", 0, 0, 0, 1));
        assertEquals(5, Conversion.hexToInt("5", 0, 0, 0, 1));
        assertEquals(15, Conversion.hexToInt("f", 0, 0, 0, 1));
        assertEquals(15, Conversion.hexToInt("F", 0, 0, 0, 1));
        
        // Equivalence Class 2: Multi-digit hex strings (lowercase)
        assertEquals(0x123, Conversion.hexToInt("123", 0, 0, 0, 3));
        assertEquals(0xabc, Conversion.hexToInt("abc", 0, 0, 0, 3));
        assertEquals(0xdef, Conversion.hexToInt("def", 0, 0, 0, 3));
        
        // Equivalence Class 3: Multi-digit hex strings (uppercase)
        assertEquals(0x123, Conversion.hexToInt("123", 0, 0, 0, 3));
        assertEquals(0xABC, Conversion.hexToInt("ABC", 0, 0, 0, 3));
        assertEquals(0xDEF, Conversion.hexToInt("DEF", 0, 0, 0, 3));
        
        // Equivalence Class 4: Mixed case hex strings
        assertEquals(0x1aB2, Conversion.hexToInt("1aB2", 0, 0, 0, 4));
        assertEquals(0xfE45, Conversion.hexToInt("fE45", 0, 0, 0, 4));
        
        // Equivalence Class 5: Maximum length strings for int (8 hex digits)
        assertEquals(0x12345678, Conversion.hexToInt("12345678", 0, 0, 0, 8));
        assertEquals(0xFFFFFFFF, Conversion.hexToInt("FFFFFFFF", 0, 0, 0, 8));
    }
    
    @Test
    public void testBinaryArrayEquivalenceClasses() {
        // Equivalence Class 1: All false arrays
        boolean[] allFalse = {false, false, false, false};
        assertEquals('0', Conversion.binaryToHexDigit(allFalse));
        
        // Equivalence Class 2: All true arrays  
        boolean[] allTrue = {true, true, true, true};
        assertEquals('f', Conversion.binaryToHexDigit(allTrue));
        
        // Equivalence Class 3: Single bit set arrays
        boolean[] bit0 = {true, false, false, false};
        assertEquals('1', Conversion.binaryToHexDigit(bit0));
        
        boolean[] bit1 = {false, true, false, false};
        assertEquals('2', Conversion.binaryToHexDigit(bit1));
        
        boolean[] bit2 = {false, false, true, false};
        assertEquals('4', Conversion.binaryToHexDigit(bit2));
        
        boolean[] bit3 = {false, false, false, true};
        assertEquals('8', Conversion.binaryToHexDigit(bit3));
        
        // Equivalence Class 4: Multiple bits set
        boolean[] multipleSet = {true, false, true, false}; // 0101 = 5
        assertEquals('5', Conversion.binaryToHexDigit(multipleSet));
        
        boolean[] anotherMultiple = {true, true, false, true}; // 1011 = B
        assertEquals('b', Conversion.binaryToHexDigit(anotherMultiple));
        
        // Equivalence Class 5: Arrays smaller than 4 bits (padded with false)
        boolean[] smallArray1 = {true};
        assertEquals('1', Conversion.binaryToHexDigit(smallArray1));
        
        boolean[] smallArray2 = {true, true};  
        assertEquals('3', Conversion.binaryToHexDigit(smallArray2));
        
        // Equivalence Class 6: Arrays larger than 4 bits (only first 4 used)
        boolean[] largeArray = {true, false, true, false, true, true};
        assertEquals('5', Conversion.binaryToHexDigit(largeArray)); // Uses first 4: 1010 = A, but LSB first so 0101 = 5
    }
}
