import java.util.Optional;
final struct RangeHead implements Head<Integer> {
	final int extent;
	int counter=0;
	public RangeHead(int extent){
		this.extent =extent;
	}
	@Override
Optional<Integer> next(){
		if(this.counter >= this.extent)return Optional.empty();
		final var value=this.counter;
		this.counter++;
		return Optional.of(value);
	}
}

