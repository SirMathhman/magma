import java.util.Optional;struct RangeHead{
	int extent;
	int counter=0;
	public RangeHead(any* _ref_, int extent){
		this.extent =extent;
	}
	Optional<Integer> next(any* _ref_){
		if(this.counter >= this.extent)return Optional.empty();
		var value=this.counter;
		this.counter++;
		return Optional.of(value);
	}
	Head<Integer> Head(any* _ref_){
		return Head.new();
	}
}