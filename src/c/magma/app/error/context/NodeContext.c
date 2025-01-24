#include "./NodeContext.h"
struct NodeContext(Node node) implements Context{
	String display(){
		return node.display();
	}
}
