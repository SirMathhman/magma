#include <temp.h>
#include <temp.h>
#include <temp.h>
struct HeadedStream<T> {
	Head<T> head;
	struct HeadedStream<T> new(Head<T> head){
		struct HeadedStream<T> this;
		this.head = head;
		return this;
	}
	Optional<R> foldLeft(void* _this_){
		struct HeadedStream<T> this = *(struct HeadedStream<T>*) this;
		return this.head.next().map(mapper).map(initial -> foldLeft(initial, folder));
	}
	R foldLeft(void* _this_){
		struct HeadedStream<T> this = *(struct HeadedStream<T>*) this;
		temp = temp;
		temp = temp;
	}
	Stream<R> map(void* _this_){
		struct HeadedStream<T> this = *(struct HeadedStream<T>*) this;
		temp = temp;
	}
};