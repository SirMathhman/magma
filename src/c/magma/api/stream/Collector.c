#include "./Collector.h"
struct Collector<T, C>{
	C createInitial();
	C fold(C current, T element);
}