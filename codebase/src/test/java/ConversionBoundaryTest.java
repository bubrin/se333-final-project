import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.lang3.Conversion;

public class ConversionBoundaryTest {
    
    @Test
    public void testHexDigitToIntBoundaryValues() {
        // Test valid hex digit boundaries
        assertEquals(0, Conversion.hexDigitToInt('0'));
        assertEquals(15, Conversion.hexDigitToInt('f'));
        assertEquals(15, Conversion.hexDigitToInt('F'));
        assertEquals(10, Conversion.hexDigitToInt('a'));
        assertEquals(10, Conversion.hexDigitToInt('A'));
        
        // Test invalid hex digits (should throw exception)
        assertThrows(IllegalArgumentException.class, () -> Conversion.hexDigitToInt('g'));
        assertThrows(IllegalArgumentException.class, () -> Conversion.hexDigitToInt('Z'));
        assertThrows(IllegalArgumentException.class, () -> Conversion.hexDigitToInt(' '));
    }
    
    @Test
    public void testIntToHexDigitBoundaryValues() {
        // Test valid nibble boundaries
        assertEquals('0', Conversion.intToHexDigit(0));
        assertEquals('f', Conversion.intToHexDigit(15));
        
        // Test invalid nibbles (should throw exception)
        assertThrows(IllegalArgumentException.class, () -> Conversion.intToHexDigit(-1));
        assertThrows(IllegalArgumentException.class, () -> Conversion.intToHexDigit(16));
        assertThrows(IllegalArgumentException.class, () -> Conversion.intToHexDigit(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> Conversion.intToHexDigit(Integer.MIN_VALUE));
    }
    
    @Test
    public void testByteArrayToIntBoundaryValues() {
        // Test minimum array
        byte[] minArray = new byte[1];
        assertEquals(0, Conversion.byteArrayToInt(minArray, 0, 0, 0, 1));
        
        // Test maximum values
        byte[] maxArray = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        int result = Conversion.byteArrayToInt(maxArray, 0, 0, 0, 4);
        assertEquals(-1, result); // All bytes set gives -1 in two's complement
        
        // Test boundary positions
        byte[] testArray = {0x01, 0x02, 0x03, 0x04, 0x05};
        assertEquals(0x02, Conversion.byteArrayToInt(testArray, 1, 0, 0, 1));
        assertEquals(0x0504, Conversion.byteArrayToInt(testArray, 3, 0, 0, 2));
        
        // Test invalid array access
        assertThrows(IllegalArgumentException.class, 
            () -> Conversion.byteArrayToInt(testArray, 2, 0, 0, 5)); // srcPos + nBytes > array length
        assertThrows(IllegalArgumentException.class, 
            () -> Conversion.byteArrayToInt(testArray, -1, 0, 0, 1)); // negative srcPos
    }
    
    @Test
    public void testHexToIntBoundaryValues() {
        // Test minimum and maximum hex values
        assertEquals(0, Conversion.hexToInt("0", 0, 0, 0, 1));
        assertEquals(15, Conversion.hexToInt("f", 0, 0, 0, 1));
        assertEquals(255, Conversion.hexToInt("ff", 0, 0, 0, 2));
        
        // Test maximum 32-bit signed integer in hex
        assertEquals(Integer.MAX_VALUE, Conversion.hexToInt("7fffffff", 0, 0, 0, 8));
        
        // Test boundary positions
        String hexString = "123456789abcdef0";
        assertEquals(0x1, Conversion.hexToInt(hexString, 0, 0, 0, 1));
        assertEquals(0xf0, Conversion.hexToInt(hexString, 14, 0, 0, 2));
        
        // Test empty and null inputs
        assertThrows(IllegalArgumentException.class, 
            () -> Conversion.hexToInt("", 0, 0, 0, 1));
        assertThrows(StringIndexOutOfBoundsException.class, 
            () -> Conversion.hexToInt("123", 2, 0, 0, 2)); // srcPos + nHex > string length
    }
    
    @Test
    public void testBinaryToHexDigitBoundaryValues() {
        // Test minimum binary (all false)
        boolean[] minBinary = {false, false, false, false};
        assertEquals('0', Conversion.binaryToHexDigit(minBinary));
        
        // Test maximum binary (all true)
        boolean[] maxBinary = {true, true, true, true};
        assertEquals('f', Conversion.binaryToHexDigit(maxBinary));
        
        // Test individual bit positions
        boolean[] testBinary1 = {true, false, false, false}; // LSB set
        assertEquals('1', Conversion.binaryToHexDigit(testBinary1));
        
        boolean[] testBinary8 = {false, false, false, true}; // MSB set  
        assertEquals('8', Conversion.binaryToHexDigit(testBinary8));
        
        // Test array size boundaries
        boolean[] oversizedArray = {true, true, true, true, false, false};
        assertEquals('f', Conversion.binaryToHexDigit(oversizedArray, 0)); // Uses first 4 bits
        
        boolean[] undersizedArray = {true, true};
        assertEquals('3', Conversion.binaryToHexDigit(undersizedArray)); // Missing bits treated as false
        
        // Test invalid positions
        assertThrows(IllegalArgumentException.class, 
            () -> Conversion.binaryToHexDigit(minBinary, -1)); // negative position
        assertThrows(IllegalArgumentException.class, 
            () -> Conversion.binaryToHexDigit(minBinary, 4)); // position >= array length
    }
}
