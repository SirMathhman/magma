package magma.java;

public class JavaLang {
    public static boolean isDefaultJavaValue(String value) {
        try {
            Class.forName("java.lang." + value);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
