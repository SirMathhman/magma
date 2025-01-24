struct SymbolFilter implements Predicate<String>{
	boolean test(String input){
		if(input.isEmpty())return false;
		IntStream.range(0, input.length()).allMatch(index -> {
            final var c=input.charAt(index);
            return Character.isLetter(c) || c == '_' || (index != 0 && Character.isDigit(c));
        });
	}
}
