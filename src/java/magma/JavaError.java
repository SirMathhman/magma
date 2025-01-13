package magma;

import java.io.PrintWriter;
import java.io.StringWriter;

public record JavaError(Exception e) implements Error {
    @Override
    public String display() {
        final var writer = new StringWriter();
        this.e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
