#include "./InvocationTypeMatcher.h"
struct InvocationTypeMatcher implements Locator{
	String unwrap(){
		return ">";
	}
	int length(){
		return 1;
	}
	Stream<Integer> locate(String input){
		var depth=0;
		int i=0;
		while(i<input.length()){
			var c=input.charAt(i);
			if(c=='>'&&depth==0)return Streams.of(i);
			if(c=='<')depth++;
			if(c=='>')depth--;
			i++;
		}
		return Streams.empty();
	}
}
