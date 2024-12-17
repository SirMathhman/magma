package magma.app.compile.rule;

import magma.api.collect.List;

public interface Divider {
    StringBuilder concat(StringBuilder buffer, String slice);

    List<String> divide(String root);
}
