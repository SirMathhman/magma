struct Namer{
	int counter=0;
	String createUniqueName(){
		var name="_lambda"+counter+"_";
		counter++;
		return name;
	}
}
