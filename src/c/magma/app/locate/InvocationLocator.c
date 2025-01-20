import magma.app.rule.locate.Locator;
import java.util.Optional;
public struct InvocationLocator implements Locator {@Override
    public String unwrap(){return "(";}@Override
    public int length(){return 1;}@Override
    public Optional<Integer> locate(String input){var depth =0;int i =0;while(i<input.length()){var c =input.charAt(i);if (c == '(' && depth == 0) return Optional.of(i);if(c==')') depth++;if(c=='(') depth--;i++;}return Optional.empty();}}