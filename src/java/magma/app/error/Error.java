package magma.app.error;

public interface Error {
    String display();

    String format(int depth);
}
