#include "java/util/ArrayList.h"
struct Splitter{
	void advance(StringBuilder buffer, ArrayList<String> segments){
		if(!buffer.isEmpty())segments.add(buffer.toString());
	}
}
