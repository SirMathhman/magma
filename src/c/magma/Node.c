#include "temp.h";
struct Node  {
	String value;
	Optional<String> type;
	void Node(void* __ref__, Optional<String> type, String value){
		struct Node  this = *(struct Node *) __ref__;
		this.value = value;
		this.type = type;
	}
	void Node(void* __ref__, String value){
		struct Node  this = *(struct Node *) __ref__;
		caller();
	}
	void Node(void* __ref__, String type, String value){
		struct Node  this = *(struct Node *) __ref__;
		caller();
	}
	void value(void* __ref__){
		struct Node  this = *(struct Node *) __ref__;
		return this.value;
	}
	void is(void* __ref__, String type){
		struct Node  this = *(struct Node *) __ref__;
		return {
			void __caller__ = this.type.isPresent() && this.type.get().equals;
			__caller__(__caller__, type)
		};
	}
};