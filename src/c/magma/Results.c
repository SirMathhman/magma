#include "temp.h"
struct Results {
	T writeErr(String message, String root, T segments){
		return write(System.err, message, root, segments);
	}
	T write(PrintStream stream, String message, String rootSegment, T value){
		stream.println(message + ": " + rootSegment);
		return value;
	}
};