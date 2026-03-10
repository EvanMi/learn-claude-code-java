import org.junit.Test;
import static org.junit.Assert.*;

public class HelloWorldTest {
    
    @Test
    public void testGetGreeting() {
        String result = HelloWorld.getGreeting();
        assertEquals("Hello, World!", result);
    }
    
    @Test
    public void testGetGreetingWithName() {
        String result = HelloWorld.getGreeting("Alice");
        assertEquals("Hello, Alice!", result);
    }
    
    @Test
    public void testGetGreetingWithEmptyName() {
        String result = HelloWorld.getGreeting("");
        assertEquals("Hello, !", result);
    }
    
    @Test
    public void testGetGreetingWithNull() {
        String result = HelloWorld.getGreeting(null);
        assertEquals("Hello, null!", result);
    }
}