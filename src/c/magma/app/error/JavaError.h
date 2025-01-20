import java.io.PrintWriter;
import java.io.StringWriter;
public struct JavaError(Exception e) implements Error {
	String display=String display(){
		final var writer=new StringWriter();
		this.e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	};
}