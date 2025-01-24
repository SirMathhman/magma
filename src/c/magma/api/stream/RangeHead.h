import java.util.Optional;struct RangeHead{
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
	}
	Head<Integer> Head(){
		return Head.new();
	}
}