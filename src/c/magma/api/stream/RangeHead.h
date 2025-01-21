import java.util.Optional;
final struct RangeHead implements Head<Integer> {
	const int extent;
	int counter=0;
	public RangeHead(int extent){
		this.extent =extent;
	}
	@Override
Optional<Integer> next(){
		if(this.counter >= this.extent)return Optional.empty();
		const auto value=this.counter;
		this.counter++;
		return Optional.of(value);
	}
}

