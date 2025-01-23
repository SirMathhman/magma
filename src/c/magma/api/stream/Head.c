import java.util.Optional;struct Head<T>{
	struct VTable{
		((void*) => Optional<T>) next;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Head new(Box<void*> ref, struct VTable vtable){
		struct Head this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}