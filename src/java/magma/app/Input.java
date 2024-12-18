package magma.app;

public final class Input {
    private final String root;
    private final int index;
    private final int length;

    public Input(String root, int index, int length) {
        this.root = root;
        this.index = index;
        this.length = length;
    }

    public String display() {
        return slice();
    }

    public String slice() {
        return root.substring(index, index + length);
    }
}