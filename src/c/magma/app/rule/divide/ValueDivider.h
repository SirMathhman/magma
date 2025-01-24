#include "../../../../magma/api/result/Err.h"
#include "../../../../magma/api/result/Ok.h"
#include "../../../../magma/api/result/Result.h"
#include "../../../../magma/app/error/CompileError.h"
#include "../../../../magma/app/error/context/StringContext.h"
#include "../../../../magma/app/rule/Splitter.h"
#include "../../../../java/util/ArrayList.h"
#include "../../../../java/util/LinkedList.h"
#include "../../../../java/util/List.h"
#include "../../../../java/util/stream/Collectors.h"
#include "../../../../java/util/stream/IntStream.h"
struct ValueDivider implements Divider{
	Divider VALUE_DIVIDER=new ValueDivider();
	private ValueDivider(){}
	String merge(String current, String value){
		return current+", "+value;
	}
	Result<List<String>, CompileError> divide(String input){
		var segments=new ArrayList<String>();
		var buffer=new StringBuilder();
		var depth=0;
		var queue=IntStream.range(0, input.length()).mapToObj(input::charAt).collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
			var c=queue.pop();
			if(c=='\''){
				buffer.append(c);
				if(queue.isEmpty()){
					return new Err<>(new CompileError("Malformed chars", new StringContext(input)));
				}
				var c1=queue.pop();
				buffer.append(c1);
				if(c1=='\\'){
					buffer.append(queue.pop());
				}
				buffer.append(queue.pop());
			}
			if(c=='\"'){
				buffer.append(c);
				while(!queue.isEmpty()){
					var next=queue.pop();
					buffer.append(next);
					if(next=='\\')buffer.append(queue.pop());
					if(next=='\"')break;
				}
				continue;
			}
			if(c=='-'){
				buffer.append(c);
				if(!queue.isEmpty() && queue.peek() == '>'){
					buffer.append(queue.pop());
				}
				continue;
			}
			if(c==','&&depth==0){
				Splitter.advance(buffer, segments);
				buffer=new StringBuilder();
			}
			else{
				buffer.append(c);
				if(c=='<' || c == '(')depth++;
				if(c=='>' || c == ')')depth--;
			}
		}
		Splitter.advance(buffer, segments);
		return new Ok<List<String>, CompileError>(segments);
	}
}
