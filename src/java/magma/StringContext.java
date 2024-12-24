package magma;

public class StringContext implements Context {
    private final String value;

    public StringContext(String value) {
        this.value = value;
    }

    @Override
    public String display() {
        return value;
    }
}
