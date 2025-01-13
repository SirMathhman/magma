#include "temp.h"
struct Tuple<A, B>(A left, B right) {
	Tuple<R, B> mergeIntoLeft(BiFunction<A, B, R> merger){
		return temp();
	}
};