import magma.Tuple;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Collector;
import java.util.LinkedList;
struct JavaLinkedList<T> {private final LinkedList<T> list;public JavaLinkedList() {
        this(new LinkedList<>());
    }public JavaLinkedList(LinkedList<T> list) {
        this.list = list;
    }
	JavaLinkedList<T>> collector(){return new Collector<T, JavaLinkedList<T>>() {
            @Override
            public JavaLinkedList<T> createInitial() {
                return new JavaLinkedList<>();
            }

            @Override
            public JavaLinkedList<T> fold(JavaLinkedList<T> current, T next) {
                return current.add(next);
            }
        };
	}
	JavaLinkedList<T> add(T next){
		this.list.add(next);
		return this;
	}
	boolean isEmpty(){
		return this.list.isEmpty();
	}
	JavaLinkedList<T>>> pop(){
		return isEmpty() ? new None<>() :  new Some<>(Tuple<>(this.list.pop(), this));
	}
	Option<T> peek(){
		return isEmpty() ? new None<>() :  new Some<>(this.list.peek());
	}
}