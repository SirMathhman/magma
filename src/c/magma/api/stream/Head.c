import java.util.Optional;struct Head<Capture, T>{
	struct VTable{
		(() => Optional<T>) next;
	}
	struct VTable vtable;
	struct Head new(struct VTable table){
		struct Head this;
		this.table=table;
		return this;
	}
}