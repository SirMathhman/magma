import java.util.Optional;struct Head<T>{
	struct VTable{
		Optional<T> (*)(void*) next;
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