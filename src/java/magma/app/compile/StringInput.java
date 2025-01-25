package magma.app.compile;

public record StringInput(String propertyKey) implements Input {
    @Override
    public String unwrap() {
        return this.propertyKey;
    }
}
