import java.util.Optional;struct Head<T>{
	struct VTable{
		((Any) => Optional<T>) next;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Head new(Box<Any> ref, struct VTable vtable){
		struct Head this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}