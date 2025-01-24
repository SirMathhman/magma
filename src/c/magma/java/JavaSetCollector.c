#include "../../magma/api/stream/Collector.h"
struct JavaSetCollector<T> implements Collector<T, JavaSet<T>>{
	JavaSet<T> createInitial(){
		return new JavaSet<>();
	}
	JavaSet<T> fold(JavaSet<T> current, T element){
		return current.add(element);
	}
}
