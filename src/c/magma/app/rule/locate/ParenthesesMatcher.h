import magma.api.Tuple;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public struct ParenthesesMatcher implements Locator {
	(() => String) unwrap=String unwrap(){
		return ")";
	};
	(() => int) length=int length(){
		return 1;
	};
	((String) => Stream<Integer>) locate=Stream<Integer> locate(String input){
		var depth=0;
		final var queue=IntStream.range(0, input.length()).mapToObj(index -> new Tuple<>(index, input.charAt(index)))
                .collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
		final var tuple=queue.pop();
		final var i=tuple.left();
		final var c=tuple.right();
		if(c=='\''){
		final var tuple1=queue.pop();
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
	};
}