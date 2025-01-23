import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import magma.app.error.context.StringContext;import magma.app.rule.Splitter;import java.util.ArrayList;import java.util.LinkedList;import java.util.List;import java.util.stream.Collectors;import java.util.stream.IntStream;struct ValueDivider implements Divider{
	Divider VALUE_DIVIDER=ValueDivider.new();
	private ValueDivider(){}
	String merge(String current, String value){
		return current+", "+value;
	}
	Result<List<String>, CompileError> divide(String input){
		var segments=ArrayList<String>.new();
		var buffer=StringBuilder.new();
		var depth=0;
		var queue=IntStream.range(0, input.length()).mapToObj(input::charAt).collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
			var c=queue.pop();
			if(c=='\''){
				buffer.append(c);
				if(queue.isEmpty()){
					return Err<>.new();
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
				buffer=StringBuilder.new();
			}
			else{
				buffer.append(c);
				if(c=='<' || c == '(')depth++;
				if(c=='>' || c == ')')depth--;
			}
		}
		Splitter.advance(buffer, segments);
		return Ok<List<String>, CompileError>.new();
	}struct ValueDivider implements Divider new(){struct ValueDivider implements Divider this;return this;}
}