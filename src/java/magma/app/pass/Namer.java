package magma.app.pass;

public class Namer {
    private static int counter = 0;

    static String createUniqueName() {
        final var name = "_lambda" + counter + "_";
        counter++;
        return name;
    }
}
