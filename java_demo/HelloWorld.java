public class HelloWorld {
    
    public static String getGreeting() {
        return "Hello, World!";
    }
    
    public static String getGreeting(String name) {
        return "Hello, " + name + "!";
    }
    
    public static void main(String[] args) {
        System.out.println(getGreeting());
        
        if (args.length > 0) {
            System.out.println(getGreeting(args[0]));
        }
    }
}