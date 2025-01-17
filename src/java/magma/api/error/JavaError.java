package magma.api.error;

import java.io.PrintWriter;
import java.io.StringWriter;

public record JavaError(Exception e) implements Error {
    @Override
    public String display() {
        var writer = new StringWriter();
        this.e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
