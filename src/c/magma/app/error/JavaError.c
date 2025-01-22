import java.io.PrintWriter;import java.io.StringWriter;struct JavaError(Exception e) implements Error{
	String display(){
		var writer=StringWriter.new();
		this.e.printStackTrace(PrintWriter.new());
		return writer.toString();
	}
}