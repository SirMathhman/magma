import java.io.PrintWriter;
import java.io.StringWriter;

@Override
String display(){
	 auto writer=new StringWriter();
	this.e.printStackTrace(new PrintWriter(writer));
	return writer.toString();
}
struct JavaError(Exception e) implements Error {
}

