#include "../../../../magma/api/Tuple.h"
#include "../../../../magma/api/stream/Stream.h"
#include "../../../../magma/api/stream/Streams.h"
#include "../../../../java/util/LinkedList.h"
#include "../../../../java/util/Optional.h"
#include "../../../../java/util/stream/Collectors.h"
#include "../../../../java/util/stream/IntStream.h"
struct ParenthesesMatcher implements Locator{
	String unwrap(){
		return ")";
	}
	int length(){
		return 1;
	}
	Stream<Integer> locate(String input){
		var depth=0;
		var queue=IntStream.range(0, input.length()).mapToObj(()->new Tuple<>(index, input.charAt(index))).collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
			var tuple=queue.pop();
			var i=tuple.left();
			var c=tuple.right();
			if(c=='\''){
				var tuple1=queue.pop();
				if(tuple1.right() == '\\'){
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
