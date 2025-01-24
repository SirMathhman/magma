struct JoiningCollector implements Collector<String, Option<String>>{
	String slice;
	public JoiningCollector(String slice){
		this.slice = slice;
	}
	Option<String> createInitial(){
		return new None<>();
	}
	Option<String> fold(Option<String> current, String element){
		if(current.isEmpty())return new Some<>(element);
		return current.map(()->inner+this.slice + element);
	}
}
