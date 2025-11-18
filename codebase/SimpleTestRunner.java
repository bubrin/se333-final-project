import java.lang.reflect.Method;

public class SimpleTestRunner {
    public static void main(String[] args) {
        System.out.println("Running CompositeFormat Tests...\n");
        
        runTestClass("CompositeFormatBoundaryTest");
        runTestClass("CompositeFormatEquivalenceTest");
        runTestClass("CompositeFormatDecisionTableTest");
        runTestClass("CompositeFormatContractTest");
    }
    
    private static void runTestClass(String className) {
        try {
            Class<?> testClass = Class.forName(className);
            Object testInstance = testClass.newInstance();
            Method[] methods = testClass.getDeclaredMethods();
            
            int passed = 0;
            int failed = 0;
            
            System.out.println("Running " + className + ":");
            
            for (Method method : methods) {
                if (method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                    try {
                        method.invoke(testInstance);
                        System.out.println("  ✓ " + method.getName() + " - PASSED");
                        passed++;
                    } catch (Exception e) {
                        System.out.println("  ✗ " + method.getName() + " - FAILED: " + e.getCause());
                        failed++;
                    }
                }
            }
            
            System.out.println("  Results: " + passed + " passed, " + failed + " failed\n");
            
        } catch (Exception e) {
            System.out.println("Error running " + className + ": " + e.getMessage());
        }
    }
}