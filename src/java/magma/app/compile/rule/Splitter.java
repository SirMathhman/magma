package magma.app.compile.rule;

import magma.api.collect.List;

public interface Splitter {
    StringBuilder merge(StringBuilder buffer, String slice);

    List<String> split(String root);
}
