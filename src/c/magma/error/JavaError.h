#include <temp.h>
#include <temp.h>
struct JavaError {
	Exception e;
	struct JavaError new(Exception e){
		struct JavaError this;
		this.e = e;
		return this;
	}
	String display(){
		temp = temp;
		this.e.printStackTrace();
		return writer.toString();
	}
};