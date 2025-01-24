#include "./State.h"
struct State(Stack<List<Node>> frames){
	public State(){
		this(new Stack<>());
		this.frames.push(new ArrayList<>());
	}
	Optional<Node> findInFrame(String value, List<Node> frame){
		return frame.stream().filter(definition->definition.findString("name").orElse("").equals(value)).findFirst();
	}
	State exit(){
		this.frames.pop();
		return this;
	}
	State enter(){
		this.frames.push(new ArrayList<>());
		return this;
	}
	State pushAll(List<Node> definitions){
		this.frames.peek().addAll(definitions);
		return this;
	}
	int depth(){
		return this.frames.size()-1;
	}
	Option<Node> find(String value){
		return JavaOptions.fromNative(this.frames.stream().map(frame->findInFrame(value, frame)).flatMap(Optional::stream).findFirst());
	}
}
