import magma.api.Tuple;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
struct ParenthesesMatcher implements Locator {
	@Override
String unwrap(){
		return ")";
	}
	@Override
int length(){
		return 1;
	}
	@Override
Stream<Integer> locate(String input){
		var depth=0;
		const var queue=IntStream.range(0, input.length()).mapToObj(index -> new Tuple<>(index, input.charAt(index)))
                .collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
			const var tuple=queue.pop();
			const var i=tuple.left();
			const var c=tuple.right();
			if(c=='\''){
				const var tuple1=queue.pop();
				if(tuple1.right() == '\\'){
					queue.pop();
				}
				queue.pop();
			}
			if(c==')'&&depth==1)return Streams.of(i);
			if(c=='(')depth++;
			if(c==')')depth--;
		}
		return Streams.empty();
	}
}

