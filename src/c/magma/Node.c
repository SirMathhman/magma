#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Node {
	struct Node Node_new(){
		struct Node this;
		return this;
	}
	Node Node_withNodeList(void* _this_);
	Optional<List<Node>> Node_findNodeList(void* _this_);
	Node Node_withString(void* _this_);
	Optional<String> Node_findString(void* _this_);
	Node Node_withNode(void* _this_);
	Optional<Node> Node_findNode(void* _this_);
	Node Node_mapString(void* _this_);
};