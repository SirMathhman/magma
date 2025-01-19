#include <temp.h>
#include <temp.h>
#include <temp.h>
struct HeadedStream<T> {
	Head<T> head;
	struct HeadedStream<T> HeadedStream<T>_new(Head<T> head){
		struct HeadedStream<T> this;
		this.head = head;
		return this;
	}
	Optional<R> HeadedStream<T>_foldLeft(void* _this_){
		struct HeadedStream<T> this = *(struct HeadedStream<T>*) this;
		return this.head.next().map(mapper).map(initial -> foldLeft(initial, folder));
	}
	R HeadedStream<T>_foldLeft(void* _this_){
		struct HeadedStream<T> this = *(struct HeadedStream<T>*) this;
		temp = temp;
		temp = temp;
	}
	Stream<R> HeadedStream<T>_map(void* _this_){
		struct HeadedStream<T> this = *(struct HeadedStream<T>*) this;
		temp = temp;
	}
};