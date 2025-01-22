import magma.api.stream.Stream;import magma.api.stream.Streams;import magma.app.rule.locate.Locator;import java.util.ArrayList;import java.util.List;struct BackwardsLocator implements Locator {String infix;public BackwardsLocator(String infix){
	this.infix =infix;}String unwrap(){
	return this.infix;}int length(){
	return this.infix.length();}Stream<Integer> locate(String input){
	return Streams.from(searchForIndices(input));}List<Integer> searchForIndices(String input){
	List<Integer> indices=new ArrayList<>();
	int index=input.lastIndexOf(this.infix);
	while(index>=0){
	indices.add(index);
	index=input.lastIndexOf(this.infix, index - 1);}
	return indices;}}