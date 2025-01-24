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
struct StatementDivider implements Divider{
	Divider STATEMENT_DIVIDER=new StatementDivider();
	private StatementDivider(){}
	String merge(String current, String value){
		return current+value;
	}
	Result<List<String>, CompileError> divide(String input){
		var segments=new ArrayList<String>();
		var buffer=new StringBuilder();
		var depth=0;
		var queue=IntStream.range(0, input.length()).mapToObj(input::charAt).collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
			var c=queue.pop();
			buffer.append(c);
			if(c=='\''){
				var c1=queue.pop();
				buffer.append(c1);
				if(c1=='\\'){
					buffer.append(queue.pop());
				}
				buffer.append(queue.pop());
				continue;
			}
			if(c=='"'){
				while(!queue.isEmpty()){
					var c1=queue.pop();
					buffer.append(c1);
					if(c1=='"')break;
					if(c1=='\\'){
						buffer.append(queue.pop());
					}
				}
				continue;
			}
			if(c==';'&&depth==0){
				Splitter.advance(buffer, segments);
				buffer=new StringBuilder();
			}
			else if(c=='}'&&depth==1){
				depth--;
				Splitter.advance(buffer, segments);
				buffer=new StringBuilder();
			}
			else{
				if(c=='{' || c == '(')depth++;
				if(c=='}' || c == ')')depth--;
			}
		}
		Splitter.advance(buffer, segments);
		if(depth==0){
			return new Ok<>(segments);
		}
		else{
			return new Err<>(new CompileError("Invalid depth '"+depth+"'", new StringContext(input)));
		}
	}
}
