import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import java.util.Optional;
struct JavaOptionals {
	Option<T> to(Optional<T> optional){
		return optional.<Option<T>>map(Some.new).orElseGet(None.new);
	}
}