package magma.compile.pass;

public class Generator {
    int counter = -1;

    public Generator() {
    }

    String generateUniqueName(String category) {
        counter++;
        return "__" + category + counter + "__";
    }
}