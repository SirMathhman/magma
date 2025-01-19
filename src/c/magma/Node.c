#include <temp.h>
#include <temp.h>
struct Node {
	struct Node new(){
		struct Node this;
		return this;
	}
	Node withNodeList(void* _this_);
	Optional<List<Node>> findNodeList(void* _this_);
	Node withString(void* _this_);
	Optional<String> findString(void* _this_);
	Node withNode(void* _this_);
	Optional<Node> findNode(void* _this_);
};