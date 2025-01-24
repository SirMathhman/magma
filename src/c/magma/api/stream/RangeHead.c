#include "../../../magma/api/option/None.h"
#include "../../../magma/api/option/Option.h"
#include "../../../magma/api/option/Some.h"
struct RangeHead implements Head<Integer>{
	int extent;
	int counter=0;
	public RangeHead(int extent){
		this.extent = extent;
	}
	Option<Integer> next(){
		if(this.counter >= this.extent)return new None<>();
		var value=this.counter;
		this.counter++;
		return new Some<>(value);
	}
}
