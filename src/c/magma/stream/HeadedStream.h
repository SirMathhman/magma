#include <temp.h>
#include <temp.h>
#include <temp.h>
struct HeadedStream<T> {
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