import magma.option.None;
import magma.option.Option;
import magma.option.Some;
struct LengthHead implements Head<Integer> {private final int length;private int counter;public LengthHead(int length) {
        this.length = length;
    }
	Option<Integer> next(){
		if (this.counter >= this.length) {
			return None<>();
		}
		auto value = this.counter;
		this.counter++;
		return Some<>(value);
	}
}