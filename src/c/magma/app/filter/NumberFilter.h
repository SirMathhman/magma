package magma.app.filter;import java.util.function.Predicate;import java.util.stream.IntStream;public class NumberFilter implements Predicate<String> {@Override
    public boolean test(String input){return IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .allMatch(Character::isDigit);}}