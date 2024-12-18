package magma.app.error;

public record SimpleDetail(String value) implements Detail {
    @Override
    public String display() {
        return value;
    }
}
