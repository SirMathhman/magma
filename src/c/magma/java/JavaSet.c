#include "../../magma/api/stream/Collector.h"
#include "../../magma/api/stream/Stream.h"
#include "../../magma/api/stream/Streams.h"
#include "../../java/util/HashSet.h"
#include "../../java/util/Set.h"
struct JavaSet<T>(Set<T> set){
	public JavaSet(){
		this(new HashSet<>());
	}
	<T>Collector<T, JavaSet<T>> collect(){
		return new JavaSetCollector<>();
	}
	JavaSet<T> add(T element){
		var copy=new HashSet<>(this.set);
		copy.add(element);
		return new JavaSet<>(copy);
	}
	Stream<T> stream(){
		return Streams.from(this.set);
	}
}
