import java.util.Optional;struct Head<T>{
	struct VTable{
		(() => Optional<T>) next;
	}
	struct Head new(){
		struct Head this;
		return this;
	}
}