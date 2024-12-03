package magma.app.compile.lang.common;

public class Generator {
    public int counter = -1;

    public Generator() {
    }

    public String createUniqueName(String type) {
        final var generated = counter;
        counter++;
        final var generatedName = "__" + type + generated + "__";
        return generatedName;
    }
}