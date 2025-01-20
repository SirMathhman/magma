import java.util.Optional;
public final struct RangeHead implements Head<Integer> {
	private final int extent;
	private int counter=0;
	public RangeHead=public RangeHead(int extent){
		this.extent =extent;
	};
	Optional<Integer> next=Optional<Integer> next(){
		if(this.counter >= this.extent)return Optional.empty();
		final var value=this.counter;
		this.counter++;
		return Optional.of(value);
	};
}