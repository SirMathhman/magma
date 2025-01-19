#include <temp.h>
#include <temp.h>
struct JavaError {
	Exception e;
	struct JavaError new(Exception e){
		struct JavaError this;
		this.e = e;
		return this;
	}
	String display(void* _this_){
		struct JavaError this = *(struct JavaError*) this;
		temp = temp;
		this.e.printStackTrace();
		return writer.toString();
	}
};