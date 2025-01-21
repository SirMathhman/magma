import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.rule.Splitter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
struct ValueDivider implements Divider {
	 Divider VALUE_DIVIDER=new ValueDivider();
	private ValueDivider(){
	}
	@Override
String merge(String current, String value){
		return current+", "+value;
	}
	@Override
Result<List<String>, CompileError> divide(String input){
		 auto segments=new ArrayList<String>();
		auto buffer=new StringBuilder();
		auto depth=0;
		 auto queue=IntStream.range(0, input.length()).mapToObj(input.charAt).collect(Collectors.toCollection(LinkedList.new));
		while(!queue.isEmpty()){
			 auto c=queue.pop();
			if(c=='\''){
				buffer.append(c);
				if(queue.isEmpty()){
					return new Err<>(new CompileError("Malformed chars", new StringContext(input)));
				}
				 auto c1=queue.pop();
				buffer.append(c1);
				if(c1=='\\'){
					buffer.append(queue.pop());
				}
				buffer.append(queue.pop());
			}
			if(c=='\"'){
				buffer.append(c);
				while(!queue.isEmpty()){
					 auto next=queue.pop();
					buffer.append(next);
					if(next=='\\')buffer.append(queue.pop());
					if(next=='\"')break;
				}
				continue;
			}
			if(c==','&&depth==0){
				Splitter.advance(buffer, segments);
				buffer=new StringBuilder();
			}
			else {
				buffer.append(c);
				if(c=='<' || c == '(')depth++;
				if(c=='>' || c == ')')depth--;
			}
		}
		Splitter.advance(buffer, segments);
		return new Ok<List<String>, CompileError>(segments);
	}
}
