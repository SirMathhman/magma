package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public interface Rule {
    RuleResult<Node, ParseException> parse(String input);

    default RuleResult<Node, ParseException> parseWithTimeout(String input) {
        final var await = Rules.await(() -> parse(input));
        final var error = await.findError();
        if (error.isPresent()) {
            System.out.println("Timed out while parsing input: " + input);
        }
        return await.findValue().orElseThrow();
    }

    RuleResult<String, GenerateException> generate(Node node);
}
