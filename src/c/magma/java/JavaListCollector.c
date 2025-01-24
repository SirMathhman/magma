#include "../../magma/api/stream/Collector.h"
struct JavaListCollector<T> implements Collector<T, JavaList<T>>{
	JavaList<T> createInitial(){
		return new JavaList<>();
	}
	JavaList<T> fold(JavaList<T> current, T element){
		return current.add(element);
	}
}
