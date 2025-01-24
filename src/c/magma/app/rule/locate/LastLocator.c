#include "./LastLocator.h"
struct LastLocator(String infix) implements Locator{
	Stream<Integer> locate(String input){
		var index=input.lastIndexOf(infix());
		return index==-1?Streams.empty():Streams.of(index);
	}
	String unwrap(){
		return this.infix;
	}
	int length(){
		return this.infix.length();
	}
}
