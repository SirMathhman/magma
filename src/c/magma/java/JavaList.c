#include "../../magma/api/option/None.h"
#include "../../magma/api/option/Option.h"
#include "../../magma/api/option/Some.h"
#include "../../magma/api/stream/Stream.h"
#include "../../magma/api/stream/Streams.h"
#include "../../java/util/ArrayList.h"
#include "../../java/util/Arrays.h"
#include "../../java/util/Collections.h"
#include "../../java/util/List.h"
struct JavaList<T>(List<T> list){
	public JavaList(){
		this(Collections.emptyList());
	}
	<T>JavaList<T> of(T... values){
		return new JavaList<>(Arrays.asList(values));
	}
	Option<JavaList<T>> subList(int from, int to){
		if(from>=0&&from<this.list.size() && to >= 0 && to < this.list.size() && from <= to){
			return new Some<>(new JavaList<>(this.list.subList(from, to)));
		}
		return new None<>();
	}
	JavaList<T> add(T element){
		var copy=new ArrayList<>(this.list);
		copy.add(element);
		return new JavaList<>(copy);
	}
	Stream<T> stream(){
		return Streams.fromNativeList(this.list);
	}
	List<T> unwrap(){
		return this.list;
	}
}
