package magma;

import java.io.PrintStream;

public class Results {
    static <T> T writeErr(String message, String root, T segments) {
        return write(System.err, message, root, segments);
    }

    static <T> T write(PrintStream stream, String message, String rootSegment, T value) {
        stream.println(message + ": " + rootSegment);
        return value;
    }
}
