package magma;

import java.util.ArrayList;

public class Compiler {
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String STATEMENT_END = ";";

    static StringBuilder compile(String input) {
        final var segments = split(input);
        var output = new StringBuilder();
        for (String segment : segments) {
            final var segmentOutput = compileRootSegment(segment);
            output.append(segmentOutput);
        }
        return output;
    }

    static String compileRootSegment(String segment) {
        if (segment.startsWith(IMPORT_KEYWORD_WITH_SPACE) && segment.endsWith(STATEMENT_END)) {
            final var namespace = segment.substring(IMPORT_KEYWORD_WITH_SPACE.length(), segment.length() - STATEMENT_END.length());
            return renderImport(namespace);
        } else {
            return "";
        }
    }

    static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                segments.add(buffer.toString());
                buffer = new StringBuilder();
            }
        }
        segments.add(buffer.toString());
        return segments;
    }

    static String renderImport(String namespace) {
        return IMPORT_KEYWORD_WITH_SPACE + namespace + STATEMENT_END;
    }

    static String renderPackageStatement(String namespace) {
        return "package " + namespace + STATEMENT_END;
    }
}