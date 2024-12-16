package magma.app.compile.rule;

import magma.api.collect.List;

public interface Splitter {
    List<String> split(String root);
}
