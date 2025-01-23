import java.util.Optional;struct RangeHead implements Head<Integer>{
	int extent;
	int counter=0;
	public RangeHead(int extent){
		this.extent =extent;
	}
	Optional<Integer> next(){
		if(this.counter >= this.extent)return Optional.empty();
		var value=this.counter;
		this.counter++;
		return Optional.of(value);
	}struct RangeHead new(){struct RangeHead this;return this;}
}