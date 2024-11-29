package magma.app.compile;

import magma.api.option.Option;
import magma.java.JavaList;

public interface Layout {
    int computeElementSizeTo();

    Option<Integer> computeElementSizeTo(JavaList<Integer> indices);
}
