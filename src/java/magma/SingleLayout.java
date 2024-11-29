package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.app.compile.Layout;
import magma.java.JavaList;

public record SingleLayout(int size)  implements Layout {
    @Override
    public int computeElementSizeTo() {
        return size;
    }

    @Override
    public Option<Integer> computeElementSizeTo(JavaList<Integer> indices) {
        return new None<>();
    }

    @Override
    public String toString() {
        return String.valueOf(size);
    }
}
