#include <temp.h>
#include <temp.h>
#include <temp.h>
struct HeadedStream<T> {
	Head<T> head;
	struct HeadedStream<T> new(Head<T> head){
		struct HeadedStream<T> this;
		this.head = head;
	}
	Optional<R> foldLeft(){
		return this.head.next().map(mapper).map(initial -> foldLeft(initial, folder));
	}
	R foldLeft(){
		temp = temp;
		temp = temp;
	}
	Stream<R> map(){
		temp = temp;
	}
};