#include <temp.h>
struct NodeContext {
	Node node;
	struct NodeContext NodeContext_new(Node node){
		struct NodeContext this;
		this.node = node;
		return this;
	}
	String NodeContext_display(void* _this_){
		struct NodeContext this = *(struct NodeContext*) this;
		return node.display();
	}
};