import java.util.Optional;struct RangeHead implements Head<Integer>{
	struct Table{
		public RangeHead(int extent){
			this.extent =extent;
		}
		Optional<Integer> next(){
			if(this.counter >= this.extent)return Optional.empty();
			var value=this.counter;
			this.counter++;
			return Optional.of(value);
		}
	}
	struct Impl{
		int extent;
		int counter=0;
	}
	struct Table table;
	struct Impl impl;
}