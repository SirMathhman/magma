import java.io.PrintWriter;
import java.io.StringWriter;
struct JavaError(Exception e) implements Error {
	@Override
String display(){
		final var writer=new StringWriter();
		this.e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}

