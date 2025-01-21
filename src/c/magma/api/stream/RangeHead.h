import java.util.Optional;

public RangeHead(int extent){
	this.extent =extent;
}

@Override
Optional<Integer> next(){
	if(this.counter >= this.extent)return Optional.empty();
	 auto value=this.counter;
	this.counter++;
	return Optional.of(value);
}
struct RangeHead implements Head<Integer> { int extent;int counter=0;
}

