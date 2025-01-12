import magma.option.None;
import magma.option.Option;
import magma.option.Some;
struct FirstLocator(String infix) implements Locator {
	Option<Integer> locate(String input){
		auto index = input.indexOf(infix());
		if (index ==  - 1) {
			return None<>();
		}
		return Some<>(index);
	}
	int sliceLength(){
		return this.infix.length();
	}
}