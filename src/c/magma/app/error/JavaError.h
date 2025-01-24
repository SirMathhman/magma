import java.io.PrintWriter;import java.io.StringWriter;struct JavaError(Exception e){
	String display(){
		var writer=new StringWriter();
		this.e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
	Error N/A(){
		return N/A.new();
	}
}