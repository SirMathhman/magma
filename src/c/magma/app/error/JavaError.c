import java.io.PrintWriter;import java.io.StringWriter;struct JavaError(Exception e) implements Error{
	struct Table{
		String display(){
			var writer=new StringWriter();
			this.e.printStackTrace(new PrintWriter(writer));
			return writer.toString();
		}
	}
	struct Impl{}
}