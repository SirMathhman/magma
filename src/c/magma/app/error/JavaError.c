import java.io.PrintWriter;
import java.io.StringWriter;
public struct JavaError(Exception e) implements Error {
	@Override
    public String display();
}