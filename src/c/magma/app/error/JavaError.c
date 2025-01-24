import java.io.PrintWriter;import java.io.StringWriter;struct JavaError(Exception e){
	String display(any* _ref_){
		var writer=new StringWriter();
		this.e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
	Error N/A(any* _ref_){
		return N/A.new();
	}
}