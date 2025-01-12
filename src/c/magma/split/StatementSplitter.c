import magma.collect.List;
import magma.java.JavaLinkedList;
import magma.java.JavaList;
import magma.java.Strings;
struct StatementSplitter implements Splitter {
	StringBuilder merge(StringBuilder inner, String compiled){
		return inner.append(compiled);
	}
	List<String> split(String input){
		auto segments = JavaList<String>();
		auto buffer = StringBuilder();
		auto depth = 0;
		auto queue = Strings.streamChars(input).collect(JavaLinkedList.collector());
		while (!queue.isEmpty()) {
			auto c = queue.popOrPanic();
			buffer.append(c);
			if (c == '\'') {
				auto popped = queue.popOrPanic();
				buffer.append(popped);
				if (popped == '\\') {
					buffer.append(queue.popOrPanic());
				}
				buffer.append(queue.popOrPanic());
				continue;
			}
			if (c == '"') {
				while (!queue.isEmpty()) {
					auto next = queue.popOrPanic();
					buffer.append(next);
					if (next == '"') {
						break;
					}
					if (next == '\\') {
						buffer.append(queue.popOrPanic());
					}
				}
			}
			if (c == ';' && depth == 0) {
				Splitter.advance(segments, buffer);
				buffer = new StringBuilder();
			}
			else {
				if (c == '}' && depth == 1) {
					depth--;
					Splitter.advance(segments, buffer);
					buffer = new StringBuilder();
				}
			}
			else {
				if (c == '{' || c == '(') {
					depth++;
				}
				if (c == '}' || c == ')') {
					depth--;
				}
			}
		}
		Splitter.advance(segments, buffer);
		return segments;
	}
}