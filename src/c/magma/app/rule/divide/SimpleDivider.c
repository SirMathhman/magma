struct SimpleDivider implements Divider{
	String delimiter;
	public SimpleDivider(String delimiter){
		this.delimiter = delimiter;
	}
	String merge(String current, String value){
		return current+this.delimiter + value;
	}
	Result<List<String>, CompileError> divide(String input){
		return new Ok<>(Arrays.stream(input.split(Pattern.quote(this.delimiter))).toList());
	}
}
