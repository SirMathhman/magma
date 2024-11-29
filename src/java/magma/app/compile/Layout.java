package magma.app.compile;

import magma.java.JavaList;

public interface Layout {
    int computeTotalSize();

    int computeSize(JavaList<Integer> indices);
}
