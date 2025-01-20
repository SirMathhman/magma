import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.ApplicationError;
import magma.app.error.CompileError;
import magma.app.error.JavaError;
import magma.app.locate.BackwardsLocator;
import magma.app.locate.InvocationLocator;
import magma.app.rule.ContextRule;
import magma.app.rule.ExactRule;
import magma.app.rule.FilterRule;
import magma.app.rule.InfixRule;
import magma.app.rule.LazyRule;
import magma.app.rule.NodeRule;
import magma.app.rule.OptionalNodeListRule;
import magma.app.rule.OrRule;
import magma.app.rule.PrefixRule;
import magma.app.rule.Rule;
import magma.app.rule.StringRule;
import magma.app.rule.StripRule;
import magma.app.rule.SuffixRule;
import magma.app.rule.TypeRule;
import magma.app.rule.divide.DivideRule;
import magma.app.rule.divide.SimpleDivider;
import magma.app.rule.divide.StatementDivider;
import magma.app.rule.divide.ValueDivider;
import magma.app.rule.filter.NumberFilter;
import magma.app.rule.filter.SymbolFilter;
import magma.app.rule.locate.FirstLocator;
import magma.app.rule.locate.LastLocator;
import magma.app.rule.locate.ParenthesesMatcher;
import magma.java.JavaFiles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static magma.app.rule.divide.ValueDivider.VALUE_DIVIDER;
public struct Main {
	public static final Path SOURCE_DIRECTORY=Paths.get(".", "src", "java");
	public static final Path TARGET_DIRECTORY=Paths.get(".", "src", "c");
	public static final String IMPORT_BEFORE="before";
	public static final String IMPORT_AFTER="after";
	public static final String ROOT_TYPE="root";
	public static final String RECORD_TYPE="record";
	public static final String CLASS_TYPE="class";
	public static final String INTERFACE_TYPE="interface";
	public static final String BEFORE_STRUCT_SEGMENT="before-struct-segment";
	public static final String STRUCT_TYPE="struct";
	public static final String WHITESPACE_TYPE="whitespace";
	public static final String STRUCT_AFTER_CHILDREN="struct-after-children";
	public static final String BLOCK_AFTER_CHILDREN="block-after-children";
	public static final String BLOCK="block";
	public static final String CONTENT_BEFORE_CHILD="before-child";
	public static final String PARENT="caller";
	public static final String GENERIC_CHILDREN="children";
	public static final String FUNCTIONAL_TYPE="functional";
	public static final String METHOD_CHILD="child";
	public static final String DEFINITION_ANNOTATIONS="annotations";
	public static final String DEFINITION_MODIFIERS="modifiers";
	public static final String METHOD_TYPE="method";
	public static final String INITIALIZATION_TYPE="initialization";
	public static final String METHOD_DEFINITION=Main.INITIALIZATION_TYPE;
	public static final String INITIALIZATION_VALUE="value";
	public static final String INITIALIZATION_DEFINITION="definition";
	public static final String DEFINITION_TYPE="definition";
	public static final String TUPLE_TYPE="tuple";
	public static final String TUPLE_CHILDREN="children";
	((String[]) => void) main=void main(String[] args){
		collect().mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(Main::runWithSources).match(Function.identity(), Optional::of).ifPresent(error ->System.err.println(error.display()));
	};
	(() => Result<Set<Path>, IOException>) collect=Result<Set<Path>, IOException> collect(){
		return JavaFiles.walkWrapped(SOURCE_DIRECTORY).mapValue(paths ->paths.stream().filter(Files::isRegularFile).filter(path ->path.toString().endsWith(".java")).collect(Collectors.toSet()));
	};
	((Set<Path>) => Optional<ApplicationError>) runWithSources=Optional<ApplicationError> runWithSources(Set<Path> sources){
		return sources.stream().map(Main::runWithSource).flatMap(Optional::stream).findFirst();
	};
	((Path) => Optional<ApplicationError>) runWithSource=Optional<ApplicationError> runWithSource(Path source){
		final var relative=SOURCE_DIRECTORY.relativize(source);
		final var parent=relative.getParent();
		final var namespace=IntStream.range(0, parent.getNameCount()).mapToObj(parent::getName).map(Path::toString).toList();
		if(namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))){
		return Optional.empty();
	}
		final var nameWithExt=relative.getFileName().toString();
		final var name=nameWithExt.substring(0, nameWithExt.indexOf('.''));
		final var copy=new ArrayList<>(namespace);
		copy.add(name);
		System.out.println("Compiling source: "+String.join(".", copy));
		final var targetParent=TARGET_DIRECTORY.resolve(parent);
		if(!Files.exists(targetParent)){
		final var directoriesError=JavaFiles.createDirectoriesWrapped(targetParent);
		if(directoriesError.isPresent())return directoriesError.map(JavaError::new).map(ApplicationError::new);
	}
		return JavaFiles.readStringWrapped(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .flatMapValue(input ->createJavaRootRule().parse(input).mapErr(ApplicationError::new))
                .flatMapValue(root ->pass(root).mapErr(ApplicationError::new)).flatMapValue(root ->createCRootRule().generate(root).mapErr(ApplicationError::new)).mapValue(output -> writeOutput(output, targetParent, name)).match(Function.identity(), Optional::of);
	};
	(() => Rule) createCRootRule=Rule createCRootRule(){
		return new TypeRule(ROOT_TYPE, createContentRule(createCRootSegmentRule()));
	};
	(() => OrRule) createCRootSegmentRule=OrRule createCRootSegmentRule(){
		return new OrRule(List.of(createNamespacedRule("import", "import "), createJavaCompoundRule(STRUCT_TYPE, "struct "), createWhitespaceRule()));
	};
	((Node) => Result<Node, CompileError>) pass=Result<Node, CompileError> pass(Node root){
		return beforePass(root).orElse(new Ok<>(root)).flatMapValue(Main::passNodes).flatMapValue(Main::passNodeLists).flatMapValue(inner ->afterPass(inner).orElse(new Ok<>(inner)));
	};
	((Node) => Optional<Result<Node, CompileError>>) beforePass=Optional<Result<Node, CompileError>> beforePass(Node node){
		if(node.is(CLASS_TYPE) || node.is(RECORD_TYPE) || node.is(INTERFACE_TYPE)){
		return Optional.of(new Ok<>(node.retype(STRUCT_TYPE)));
	}
		return Optional.empty();
	};
	((Node) => Result<Node, CompileError>) passNodeLists=Result<Node, CompileError> passNodeLists(Node previous){
		return previous.streamNodeLists().foldLeftToResult(previous, Main::passNodeList);
	};
	((Node, [String, List<Node>]) => Result<Node, CompileError>) passNodeList=Result<Node, CompileError> passNodeList(Node node, [String, List<Node>] tuple){
		return Streams.from(tuple.right()).map(Main::pass).foldLeftToResult(new ArrayList<>(), Main::foldElementIntoList).mapValue(list -> node.withNodeList(tuple.left(), list));
	};
	((List<Node>, Result<Node, CompileError>) => Result<List<Node>, CompileError>) foldElementIntoList=Result<List<Node>, CompileError> foldElementIntoList(List<Node> currentNodes, Result<Node, CompileError> node){
		return node.mapValue(currentNewElement -> merge(currentNodes, currentNewElement));
	};
	((Node) => Result<Node, CompileError>) passNodes=Result<Node, CompileError> passNodes(Node root){
		return root.streamNodes().foldLeftToResult(root, (node, tuple) -> pass(tuple.right()).mapValue(passed -> node.withNode(tuple.left(), passed)));
	};
	((List<Node>, Node) => List<Node>) merge=List<Node> merge(List<Node> nodes, Node result){
		final var copy=new ArrayList<>(nodes);
		copy.add(result);
		return copy;
	};
	((Node) => Optional<Result<Node, CompileError>>) afterPass=Optional<Result<Node, CompileError>> afterPass(Node node){
		if(node.is(METHOD_TYPE)){
		final var cleaned=removeParams(node).mapNode(METHOD_DEFINITION,  definition ->{
		return definition.removeNodeList(DEFINITION_ANNOTATIONS).removeNodeList(DEFINITION_MODIFIERS);
	});
		final var maybeValue=cleaned.findNode(METHOD_CHILD);
		final var paramTypes=cleaned.findNodeList("params").orElse(Collections.emptyList()).stream().map(param ->param.findNode("type")).flatMap(Optional::stream).toList();
		final var definition=cleaned.findNode(METHOD_DEFINITION).orElse(new MapNode()).mapNode("type", type -> {
                        final var node2 = new MapNode("functional").withNode("return", type);

                        if (paramTypes.isEmpty()) {
                            return node2;
                        } else {
                            return node2.withNodeList("params", paramTypes);
                        }
                    });
		final Node node1;
		if(maybeValue.isPresent()){
		node1=new MapNode(INITIALIZATION_TYPE).withNode(INITIALIZATION_DEFINITION, definition).withNode(INITIALIZATION_VALUE, cleaned);
	}
		else {
		node1=definition;
	}
		return Optional.of(new Ok<>(node1));
	}
		if(node.is("generic")){
		final var parent=node.findString(PARENT).orElse("");
		final var children=node.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());
		if(parent.equals("BiFunction")){
		final var paramType=children.get(0);
		final var paramType1=children.get(1);
		final var returnType=children.get(2);
		return Optional.of(new Ok<>(new MapNode(FUNCTIONAL_TYPE).withNodeList("params", List.of(paramType, paramType1)).withNode("return", returnType)));
	}
		if(parent.equals("Function")){
		final var paramType=children.get(0);
		final var returnType=children.get(1);
		return Optional.of(new Ok<>(new MapNode(FUNCTIONAL_TYPE).withNodeList("params", List.of(paramType)).withNode("return", returnType)));
	}
		if(parent.equals("Supplier")){
		final var returnType=children.getFirst();
		return Optional.of(new Ok<>(new MapNode(FUNCTIONAL_TYPE).withNode("return", returnType)));
	}
		if(parent.equals("Tuple")){
		return Optional.of(new Ok<>(new MapNode(TUPLE_TYPE).withNodeList(TUPLE_CHILDREN, children)));
	}
	}
		if(node.is(BLOCK)){
		final var newNode=node.mapNodeList(GENERIC_CHILDREN, children -> {
                return children.stream().map(child -> child.withString(CONTENT_BEFORE_CHILD, "\n\t\t"))
                        .toList();
            });
		return Optional.of(new Ok<>(newNode.withString(BLOCK_AFTER_CHILDREN, "\n\t")));
	}
		if(node.is(Main.STRUCT_TYPE)){
		final var newChildren=node.findNodeList(GENERIC_CHILDREN).orElse(new ArrayList<>()).stream().filter(child ->!child.is(WHITESPACE_TYPE)).map(child -> child.withString(BEFORE_STRUCT_SEGMENT, "\n\t"))
                    .toList();
		return Optional.of(new Ok<>(node.withString(STRUCT_AFTER_CHILDREN, "\n").withNodeList(GENERIC_CHILDREN, newChildren)));
	}
		if(node.is("import")){
		return Optional.of(new Ok<>(node.withString(IMPORT_AFTER, "\n")));
	}
		if(node.is(ROOT_TYPE)){
		final var children=node.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());
		final var newChildren=children.stream().filter(child ->!child.is("package")).toList();
		return Optional.of(new Ok<>(node.withNodeList(GENERIC_CHILDREN, newChildren)));
	}
		return Optional.empty();
	};
	((Node) => Node) removeParams=Node removeParams(Node node){
		final var params=node.findNodeList("params").orElse(Collections.emptyList());
		final Node pruneParams;
		if(params.isEmpty()){
		pruneParams=node.removeNodeList("params");
	}
		else {
		pruneParams=node;
	}
		return pruneParams;
	};
	(() => Rule) createJavaRootRule=Rule createJavaRootRule(){
		return new TypeRule(ROOT_TYPE, createContentRule(createJavaRootSegmentRule()));
	};
	((String, Path, String) => Optional<ApplicationError>) writeOutput=Optional<ApplicationError> writeOutput(String output, Path targetParent, String name){
		final var target=targetParent.resolve(name+".c");
		final var header=targetParent.resolve(name+".h");
		return JavaFiles.writeStringWrapped(target, output)
                .or(() ->JavaFiles.writeStringWrapped(header, output))
                .map(JavaError::new).map(ApplicationError::new);
	};
	(() => OrRule) createJavaRootSegmentRule=OrRule createJavaRootSegmentRule(){
		return new OrRule(List.of(createNamespacedRule("package", "package "), createNamespacedRule("import", "import "), createJavaCompoundRule(CLASS_TYPE, "class "), createJavaCompoundRule(RECORD_TYPE, "record "), createJavaCompoundRule(INTERFACE_TYPE, "interface "), createWhitespaceRule()));
	};
	((String, String) => Rule) createNamespacedRule=Rule createNamespacedRule(String type, String prefix){
		return new TypeRule(type, new StripRule(new PrefixRule(prefix, new SuffixRule(new StringRule("namespace"), ";")), IMPORT_BEFORE, IMPORT_AFTER));
	};
	((String, String) => Rule) createJavaCompoundRule=Rule createJavaCompoundRule(String type, String infix){
		return createCompoundRule(type, infix, createStructSegmentRule());
	};
	((String, String, Rule) => Rule) createCompoundRule=Rule createCompoundRule(String type, String infix, Rule segmentRule){
		final var infixRule=new InfixRule(new StringRule(DEFINITION_MODIFIERS), new FirstLocator(infix), new InfixRule(new StringRule("name"), new FirstLocator("{"), new StripRule(new SuffixRule(new StripRule(createContentRule(segmentRule), "", STRUCT_AFTER_CHILDREN), "}"))));
		return new TypeRule(type, infixRule);
	};
	(() => Rule) createStructSegmentRule=Rule createStructSegmentRule(){
		final var function=new LazyRule();
		final var statement=createStatementRule(function);
		function.set(createMethodRule(statement));
		return new StripRule(new OrRule(List.of(function, createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createWhitespaceRule())), BEFORE_STRUCT_SEGMENT, "");
	};
	(() => SuffixRule) createDefinitionStatementRule=SuffixRule createDefinitionStatementRule(){
		return new SuffixRule(createDefinitionRule(), ";");
	};
	((Rule) => Rule) createInitializationRule=Rule createInitializationRule(Rule value){
		final var definition=new NodeRule(INITIALIZATION_DEFINITION, createDefinitionRule());
		final var valueRule=new NodeRule(INITIALIZATION_VALUE, value);
		final var infixRule=new InfixRule(definition, new FirstLocator("="), new StripRule(new SuffixRule(valueRule, ";")));
		return new TypeRule(INITIALIZATION_TYPE, infixRule);
	};
	((Rule) => Rule) createMethodRule=Rule createMethodRule(Rule statement){
		final var orRule=new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new ExactRule(";")));
		final var definition=createDefinitionRule();
		final var definitionProperty=new NodeRule(METHOD_DEFINITION, definition);
		final var params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, definition), new ExactRule(""));
		final var infixRule=new InfixRule(definitionProperty, new FirstLocator("("), new InfixRule(params, new FirstLocator(")"), orRule));
		return new TypeRule(METHOD_TYPE, infixRule);
	};
	((Rule) => TypeRule) createBlockRule=TypeRule createBlockRule(Rule statement){
		return new TypeRule(BLOCK, new StripRule(new PrefixRule("{", new SuffixRule(new StripRule(createContentRule(statement), "", BLOCK_AFTER_CHILDREN), "}"))));
	};
	((Rule) => Rule) createContentRule=Rule createContentRule(Rule rule){
		return new DivideRule(GENERIC_CHILDREN, StatementDivider.STATEMENT_DIVIDER, new StripRule(rule, CONTENT_BEFORE_CHILD, ""));
	};
	((Rule) => Rule) createStatementRule=Rule createStatementRule(Rule function){
		final var statement=new LazyRule();
		final var valueRule=createValueRule(statement, function);
		statement.set(new OrRule(List.of(createKeywordRule("continue"), createKeywordRule("break"), createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createConditionalRule(statement, "if", createValueRule(statement, function)), createConditionalRule(statement, "while", createValueRule(statement, function)), createElseRule(statement), createInvocationStatementRule(valueRule), createReturnRule(valueRule), createAssignmentRule(valueRule), createPostfixRule("post-increment", "++", valueRule), createPostfixRule("post-decrement", "--", valueRule), createWhitespaceRule())));
		return statement;
	};
	((String) => TypeRule) createKeywordRule=TypeRule createKeywordRule(String keyword){
		return new TypeRule(keyword, new StripRule(new ExactRule(keyword+";")));
	};
	((LazyRule) => TypeRule) createElseRule=TypeRule createElseRule(LazyRule statement){
		return new TypeRule("else", new StripRule(new PrefixRule("else ", new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(INITIALIZATION_VALUE, statement))))));
	};
	((LazyRule, String, Rule) => TypeRule) createConditionalRule=TypeRule createConditionalRule(LazyRule statement, String type, Rule value){
		final var leftRule=new StripRule(new PrefixRule("(", new NodeRule("condition", value)));
		final var blockRule=new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, statement)));
		return new TypeRule(type, new PrefixRule(type, new InfixRule(leftRule, new ParenthesesMatcher(), blockRule)));
	};
	(() => TypeRule) createWhitespaceRule=TypeRule createWhitespaceRule(){
		return new TypeRule(WHITESPACE_TYPE, new StripRule(new ExactRule("")));
	};
	((String, String, Rule) => Rule) createPostfixRule=Rule createPostfixRule(String type, String operator, Rule value){
		return new TypeRule(type, new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), operator+";"));
	};
	((Rule) => Rule) createAssignmentRule=Rule createAssignmentRule(Rule value){
		final var destination=new NodeRule("destination", value);
		final var source=new NodeRule("source", value);
		return new TypeRule("assignment", new SuffixRule(new InfixRule(destination, new FirstLocator("="), source), ";"));
	};
	((Rule) => Rule) createInvocationStatementRule=Rule createInvocationStatementRule(Rule value){
		return new SuffixRule(createInvocationRule(value), ";");
	};
	((Rule) => TypeRule) createInvocationRule=TypeRule createInvocationRule(Rule value){
		final var caller=new NodeRule("caller", value);
		final var children=new OrRule(List.of(new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, value), new ExactRule("")));
		final var suffixRule=new StripRule(new SuffixRule(new InfixRule(caller, new InvocationLocator(), children), ")"));
		return new TypeRule("invocation", suffixRule);
	};
	((Rule) => Rule) createReturnRule=Rule createReturnRule(Rule value){
		return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), ";"))));
	};
	((Rule, Rule) => Rule) createValueRule=Rule createValueRule(Rule statement, Rule function){
		final var value=new LazyRule();
		value.set(new OrRule(List.of(function, createConstructionRule(value), createInvocationRule(value), createAccessRule("data-access", ".", value), createAccessRule("method-access", "::", value), createSymbolRule(), createNumberRule(), createNotRule(value), createOperatorRule("greater-equals", ">=", value), createOperatorRule("less", "<", value), createOperatorRule("equals", "==", value), createOperatorRule("and", "&&", value), createOperatorRule("add", "+", value), createCharRule(), createStringRule(), createTernaryRule(value), createLambdaRule(statement, value))));
		return value;
	};
	((Rule, LazyRule) => TypeRule) createLambdaRule=TypeRule createLambdaRule(Rule statement, LazyRule value){
		return new TypeRule("lambda", new InfixRule(new StringRule("args"), new FirstLocator("->"), new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, value)))));
	};
	(() => TypeRule) createStringRule=TypeRule createStringRule(){
		final var value=new PrefixRule("\"", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "\""));
		return new TypeRule("string", new StripRule(value));
	};
	((LazyRule) => TypeRule) createTernaryRule=TypeRule createTernaryRule(LazyRule value){
		return new TypeRule("ternary", new InfixRule(new NodeRule("condition", value), new FirstLocator("?"), new InfixRule(new NodeRule("ifTrue", value), new FirstLocator(":"), new NodeRule("ifElse", value))));
	};
	(() => TypeRule) createCharRule=TypeRule createCharRule(){
		return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "'"))));
	};
	(() => TypeRule) createNumberRule=TypeRule createNumberRule(){
		return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule(INITIALIZATION_VALUE))));
	};
	((String, String, LazyRule) => TypeRule) createOperatorRule=TypeRule createOperatorRule(String type, String operator, LazyRule value){
		return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));
	};
	((LazyRule) => TypeRule) createNotRule=TypeRule createNotRule(LazyRule value){
		return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule(INITIALIZATION_VALUE, value))));
	};
	((LazyRule) => TypeRule) createConstructionRule=TypeRule createConstructionRule(LazyRule value){
		final var type=new StringRule("type");
		final var arguments=new OrRule(List.of(new DivideRule("arguments", VALUE_DIVIDER, value), new ExactRule("")));
		final var childRule=new InfixRule(type, new FirstLocator("("), new StripRule(new SuffixRule(arguments, ")")));
		return new TypeRule("construction", new StripRule(new PrefixRule("new ", childRule)));
	};
	(() => Rule) createSymbolRule=Rule createSymbolRule(){
		return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("value"))));
	};
	((String, String, Rule) => Rule) createAccessRule=Rule createAccessRule(String type, String infix, final Rule value){
		final var rule=new InfixRule(new NodeRule("ref", value), new LastLocator(infix), new StringRule("property"));
		return new TypeRule(type, rule);
	};
	(() => Rule) createDefinitionRule=Rule createDefinitionRule(){
		final var name=new FilterRule(new SymbolFilter(), new StringRule("name"));
		final var typeProperty=new NodeRule("type", createTypeRule());
		final var typeAndName=new StripRule(new InfixRule(typeProperty, new LastLocator(" "), name));
		final var modifierRule=new TypeRule("modifier", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(INITIALIZATION_VALUE))));
		final var modifiers=new DivideRule(DEFINITION_MODIFIERS, new SimpleDivider(" "), modifierRule);
		final var typeParams=new StringRule("type-params");
		final var maybeTypeParams=new OrRule(List.of(new ContextRule("With type params", new InfixRule(new StripRule(new PrefixRule("<", typeParams)), new FirstLocator(">"), new StripRule(typeAndName))), new ContextRule("Without type params", typeAndName)));
		final var withModifiers=new OrRule(List.of(new ContextRule("With modifiers", new StripRule(new InfixRule(modifiers, new BackwardsLocator(" "), maybeTypeParams))), new ContextRule("Without modifiers", maybeTypeParams)));
		final var annotation=new TypeRule("annotation", new StripRule(new PrefixRule("@", new StringRule(INITIALIZATION_VALUE))));
		final var annotations=new DivideRule(DEFINITION_ANNOTATIONS, new SimpleDivider("\n"), annotation);
		return new TypeRule(DEFINITION_TYPE, new OrRule(List.of(new ContextRule("With annotations", new InfixRule(annotations, new LastLocator("\n"), withModifiers)), new ContextRule("Without annotations", withModifiers))));
	};
	(() => Rule) createTypeRule=Rule createTypeRule(){
		final var type=new LazyRule();
		type.set(new OrRule(List.of(createSymbolRule(), createGenericRule(type), createVarArgsRule(type), createArrayRule(type), createFunctionalType(type), new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(new DivideRule(TUPLE_CHILDREN, VALUE_DIVIDER, type), "]"))))));
		return type;
	};
	((Rule) => TypeRule) createFunctionalType=TypeRule createFunctionalType(Rule type){
		final var params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, type), new ExactRule(""));
		final var leftRule=new PrefixRule("(", new SuffixRule(params, ")"));
		final var rule=new InfixRule(leftRule, new FirstLocator(" => "), new NodeRule("return", type));
		return new TypeRule(FUNCTIONAL_TYPE, new PrefixRule("(", new SuffixRule(rule, ")")));
	};
	((LazyRule) => TypeRule) createArrayRule=TypeRule createArrayRule(LazyRule type){
		return new TypeRule("array", new SuffixRule(new NodeRule(METHOD_CHILD, type), "[]"));
	};
	((LazyRule) => TypeRule) createVarArgsRule=TypeRule createVarArgsRule(LazyRule type){
		return new TypeRule("var-args", new SuffixRule(new NodeRule(METHOD_CHILD, type), "..."));
	};
	((LazyRule) => TypeRule) createGenericRule=TypeRule createGenericRule(LazyRule type){
		return new TypeRule("generic", new InfixRule(new StripRule(new StringRule(PARENT)), new FirstLocator("<"), new SuffixRule(new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, type), ">")));
	};
}