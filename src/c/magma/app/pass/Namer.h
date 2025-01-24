struct Namer{
	struct Table{
		String createUniqueName(){
			var name="_lambda"+counter+"_";
			counter++;
			return name;
		}
	}
	struct Impl{
		int counter=0;
	}
}