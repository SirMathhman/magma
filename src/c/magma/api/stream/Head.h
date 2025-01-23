import java.util.Optional;struct Head<T>{
	struct VTable{
		((Any) => Optional<T>) next;
	}
	struct VTable vtable;
	struct Head new(struct VTable table){
		struct Head this;
		this.table=table;
		return this;
	}
}