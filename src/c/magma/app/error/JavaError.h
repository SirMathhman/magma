#include "../../../java/io/PrintWriter.h"
#include "../../../java/io/StringWriter.h"
struct JavaError(Exception e) implements Error{
	String display(){
		var writer=new StringWriter();
		this.e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}
