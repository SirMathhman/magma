package magma.app.compile.rule;

import java.util.List;

public interface Splitter {
    List<String> split(String input);

    StringBuilder append(StringBuilder buffer, String str);
}
