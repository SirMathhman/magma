package magma;

public interface Node {
    Node retype(String type);

    boolean is(String type);

    String display();

    Node merge(Node node);
}
