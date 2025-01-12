import magma.option.None;
import magma.option.Option;
import magma.option.Some;
struct LastLocator(String infix) implements Locator {
	Option<Integer> locate(String input){
		auto index = input.lastIndexOf(infix());
		if (index ==  - 1) {
			return None<>();
		}
		return Some<>(index);
	}
	int sliceLength(){
		return this.infix.length();
	}
}