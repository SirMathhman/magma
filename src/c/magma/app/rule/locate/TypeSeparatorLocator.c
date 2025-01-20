import java.util.Optional;
public struct TypeSeparatorLocator implements Locator {
	private final int index;
	public TypeSeparatorLocator(int index){
		this.index =index;
	}
	@Override
public String unwrap(){
		return " ";
	}
	@Override
public int length(){
		return 1;
	}
	@Override
public Optional<Integer> locate(String input){
		var depth =0;
		int counter =0;
		int i =input.length() - 1;
		while(i>=0){
		var c =input.charAt(i);
		if(c==' '&&depth==0){
		if(this.index == counter){
		return Optional.of(i);
	}
		else {
		counter++;
	}
	}
		if(c=='>') depth++;
		if(c=='<') depth--;
		i--;
	}
		return Optional.empty();
	}
}