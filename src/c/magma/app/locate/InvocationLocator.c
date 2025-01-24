#include "./InvocationLocator.h"
struct InvocationLocator implements Locator{
	String unwrap(){
		return "(";
	}
	int length(){
		return 1;
	}
	Stream<Integer> locate(String input){
		var depth=0;
		int i=input.length() - 1;
		while(i>=0){
			var c=input.charAt(i);
			if(c=='('&&depth==0)return Streams.of(i);
			if(c==')')depth++;
			if(c=='(')depth--;
			i--;
		}
		return Streams.empty();
	}
}
