#include "magma/app/Node.h"
struct NodeContext(Node node) implements Context{
	String display(){
		return node.display();
	}
}
