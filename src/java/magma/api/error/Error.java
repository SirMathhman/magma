package magma.api.error;

public interface Error {
    String display();

    String format(int depth);
}
