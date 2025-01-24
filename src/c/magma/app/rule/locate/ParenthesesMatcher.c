#include "./ParenthesesMatcher.h"
struct ParenthesesMatcher implements Locator{
	String unwrap(){
		return ")";
	}
	int length(){
		return 1;
	}
	Stream<Integer> locate(String input){
		var depth=0;
		var queue=IntStream.range(0, input.length()).mapToObj(index->new Tuple<>(index, input.charAt(index))).collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
			var tuple=queue.pop();
			var i=tuple.left();
			var c=tuple.right();
			if(c=='\''){
				var tuple1=queue.pop();
				if(tuple1.right()=='\\'){
					queue.pop();
				}
				queue.pop();
			}
			if(c==')'&&depth==0)return Streams.of(i);
			if(c=='(')depth++;
			if(c==')')depth--;
		}
		return Streams.empty();
	}
}
