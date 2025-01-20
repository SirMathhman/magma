import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.ApplicationError;
import magma.app.error.CompileError;
import magma.app.error.JavaError;
import magma.app.locate.InvocationLocator;
import magma.app.rule.ExactRule;
import magma.app.rule.FilterRule;
import magma.app.rule.InfixRule;
import magma.app.rule.LazyRule;
import magma.app.rule.NodeRule;
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
import magma.app.rule.locate.LocateTypeSeparator;
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
public struct Main {
	public static final Path SOURCE_DIRECTORY =Paths.get(".", "src", "java");
	public static final Path TARGET_DIRECTORY =Paths.get(".", "src", "c");
	public static final String DEFAULT_VALUE ="value";
	public static final String IMPORT_BEFORE ="before";
	public static final String IMPORT_AFTER ="after";
	public static final String ROOT_TYPE ="root";
	public static final String RECORD_TYPE ="record";
	public static final String CLASS_TYPE ="class";
	public static final String INTERFACE_TYPE ="interface";
	public static final String BEFORE_STRUCT_SEGMENT ="before-struct-segment";
	public static final String STRUCT_TYPE ="struct";
	public static final String WHITESPACE_TYPE ="whitespace";
	public static final String STRUCT_AFTER_CHILDREN ="struct-after-children";
	public static final String BLOCK_AFTER_CHILDREN ="block-after-children";
	public static final String BLOCK ="block";
	public static final String CONTENT_BEFORE_CHILD ="before-child";
	public static final String PARENT ="caller";
	public static final String GENERIC_CHILDREN ="children";
	public static final String FUNCTIONAL_TYPE ="functional";
	public static void main(String[] args){
		collect().mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(Function.identity(), Optional::of)
                .ifPresent(error ->System.err.println(error.display()));
	}
	private static Result<Set<Path>, IOException> collect(){
		return JavaFiles.walkWrapped(SOURCE_DIRECTORY).mapValue(paths -> paths.stream()
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
 .collect(Collectors.toSet()));
	}
	private static Optional<ApplicationError> runWithSources(Set<Path> sources){
		return sources.stream()
                .map(Main::runWithSource)
                .flatMap(Optional::stream)
 .findFirst();
	}
	private static Optional<ApplicationError> runWithSource(Path source){
		final var relative =SOURCE_DIRECTORY.relativize(source);
		final var parent =relative.getParent();
		final var namespace =IntStream.range(0, parent.getNameCount())
                .mapToObj(parent::getName)
                .map(Path::toString)
                .toList();
		if(namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))){
		return Optional.empty();
	}
		final var nameWithExt =relative.getFileName().toString();
		final var name =nameWithExt.substring(0, nameWithExt.indexOf('.''));
		final var targetParent =TARGET_DIRECTORY.resolve(parent);
		if(!Files.exists(targetParent)){
		final var directoriesError =JavaFiles.createDirectoriesWrapped(targetParent);
		if (directoriesError.isPresent()) return directoriesError.map(JavaError::new).map(ApplicationError::new);
	}
		return JavaFiles.readStringWrapped(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .flatMapValue(input -> createJavaRootRule().parse(input).mapErr(ApplicationError::new))
                .flatMapValue(root -> pass(root).mapErr(ApplicationError::new))
                .flatMapValue(root -> createCRootRule().generate(root).mapErr(ApplicationError::new))
                .mapValue(output -> writeOutput(output, targetParent, name)).match(Function.identity(), Optional::of);
	}
	private static Rule createCRootRule(){
		return new TypeRule(ROOT_TYPE, createContentRule(createCRootSegmentRule()));
	}
	private static OrRule createCRootSegmentRule(){
		return new OrRule(List.of(
                createNamespacedRule("import", "import "),
                createJavaCompoundRule(STRUCT_TYPE, "struct "),
                createWhitespaceRule()
 ));
	}
	private static Result<Node, CompileError> pass(Node root){
		return beforePass(root).orElse(new Ok<>(root))
                .flatMapValue(Main::passNodes)
                .flatMapValue(Main::passNodeLists)
                .flatMapValue(inner ->afterPass(inner).orElse(new Ok<>(inner)));
	}
	private static Optional<Result<Node, CompileError>> beforePass(Node node){
		if(node.is(CLASS_TYPE) || node.is(RECORD_TYPE) || node.is(INTERFACE_TYPE)){
		return Optional.of(new Ok<>(node.retype(STRUCT_TYPE)));
	}
		return Optional.empty();
	}
	private static Result<Node, CompileError> passNodeLists(Node previous){
		return previous.streamNodeLists().foldLeftToResult(previous, Main::passNodeList);
	}
	private static Result<Node, CompileError> passNodeList(Node node,  Tuple<String, List<Node>> tuple){
		return Streams.from(tuple.right())
                .map(Main::pass)
                .foldLeftToResult(new ArrayList<>(), Main::foldElementIntoList)
                .mapValue(list -> node.withNodeList(tuple.left(), list));
	}
	private static Result<List<Node>, CompileError> foldElementIntoList(List<Node> currentNodes,  Result<Node, CompileError> node){
		return node.mapValue(currentNewElement -> merge(currentNodes, currentNewElement));
	}
	private static Result<Node, CompileError> passNodes(Node root){
		return root.streamNodes().foldLeftToResult(root, (node, tuple) -> pass(tuple.right()).mapValue(passed -> node.withNode(tuple.left(), passed)));
	}
	private static List<Node> merge(List<Node> nodes,  Node result){
		final var copy =new ArrayList<>(nodes);
		copy.add(result);
		return copy;
	}
	private static Optional<Result<Node, CompileError>> afterPass(Node node){
		if(node.is("generic")){
		final var parent =node.findString(PARENT).orElse("");
		final var children =node.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());
		if(parent.equals("BiFunction")){
		final var paramType =children.get(0);
		final var paramType1 =children.get(1);
		final var returnType =children.get(2);
		return Optional.of(new Ok<>(new MapNode(FUNCTIONAL_TYPE).withNodeList("params", List.of(paramType, paramType1))
                        .withNode("return", returnType)));
	}
		if(parent.equals("Function")){
		final var paramType =children.get(0);
		final var returnType =children.get(1);
		return Optional.of(new Ok<>(new MapNode(FUNCTIONAL_TYPE).withNodeList("params", List.of(paramType))
                        .withNode("return", returnType)));
	}
		if(parent.equals("Supplier")){
		final var returnType =children.getFirst();
		return Optional.of(new Ok<>(new MapNode(FUNCTIONAL_TYPE).withNodeList("params", Collections.emptyList())
                        .withNode("return", returnType)));
	}
	}
		if(node.is(BLOCK)){
		final var newNode =node.mapNodeList(GENERIC_CHILDREN, children -> {
                return children.stream()
                        .map(child -> child.withString(CONTENT_BEFORE_CHILD, "\n\t\t"))
                        .toList();
            });
		return Optional.of(new Ok<>(newNode.withString(BLOCK_AFTER_CHILDREN, "\n\t")));
	}
		if(node.is(Main.STRUCT_TYPE)){
		final var newChildren =node.findNodeList(GENERIC_CHILDREN).orElse(new ArrayList<>())
                    .stream()
                    .filter(child -> !child.is(WHITESPACE_TYPE))
                    .map(child -> child.withString(BEFORE_STRUCT_SEGMENT, "\n\t"))
                    .toList();
		return Optional.of(new Ok<>(node.withString(STRUCT_AFTER_CHILDREN, "\n")
                    .withNodeList(GENERIC_CHILDREN, newChildren)));
	}
		if(node.is("import")){
		return Optional.of(new Ok<>(node.withString(IMPORT_AFTER, "\n")));
	}
		if(node.is(ROOT_TYPE)){
		final var children =node.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());
		final var newChildren =children.stream()
                    .filter(child -> !child.is("package"))
                    .toList();
		return Optional.of(new Ok<>(node.withNodeList(GENERIC_CHILDREN, newChildren)));
	}
		return Optional.empty();
	}
	private static Rule createJavaRootRule(){
		return new TypeRule(ROOT_TYPE, createContentRule(createJavaRootSegmentRule()));
	}
	private static Optional<ApplicationError> writeOutput(String output,  Path targetParent,  String name){
		final var target =targetParent.resolve(name+".c");
		final var header =targetParent.resolve(name+".h");
		return JavaFiles.writeStringWrapped(target, output)
                .or(() -> JavaFiles.writeStringWrapped(header, output))
                .map(JavaError::new)
 .map(ApplicationError::new);
	}
	private static OrRule createJavaRootSegmentRule(){
		return new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import "),
                createJavaCompoundRule(CLASS_TYPE, "class "),
                createJavaCompoundRule(RECORD_TYPE, "record "),
                createJavaCompoundRule(INTERFACE_TYPE, "interface "),
                createWhitespaceRule()
 ));
	}
	private static Rule createNamespacedRule(String type,  String prefix){
		return new TypeRule(type, new StripRule(new PrefixRule(prefix, new SuffixRule(new StringRule("namespace"), ";")), IMPORT_BEFORE, IMPORT_AFTER));
	}
	private static Rule createJavaCompoundRule(String type,  String infix){
		return createCompoundRule(type, infix, createStructSegmentRule());
	}
	private static Rule createCompoundRule(String type,  String infix,  Rule segmentRule){
		final var infixRule =new InfixRule(new StringRule("modifiers"), new FirstLocator(infix), new InfixRule(new StringRule("name"), new FirstLocator("{"), new StripRule(new SuffixRule(new StripRule(createContentRule(segmentRule), "", STRUCT_AFTER_CHILDREN), "}"))));
		return new TypeRule(type, infixRule);
	}
	private static Rule createStructSegmentRule(){
		final var statement =createStatementRule();
		return new StripRule(new OrRule(List.of(
                createMethodRule(statement),
                createInitializationRule(statement),
                createDefinitionStatementRule(),
                createWhitespaceRule()
        )), BEFORE_STRUCT_SEGMENT, "");
	}
	private static SuffixRule createDefinitionStatementRule(){
		return new SuffixRule(createDefinitionRule(), ";");
	}
	private static Rule createInitializationRule(Rule statement){
		final var infixRule =new InfixRule(new NodeRule("definition", createDefinitionRule()), new FirstLocator("="), new StripRule(new SuffixRule(new NodeRule("value", createValueRule(statement)), ";")));
		return new TypeRule("initialization", infixRule);
	}
	private static Rule createMethodRule(Rule statement){
		final var orRule =new OrRule(List.of(new NodeRule("child", createBlockRule(statement)), new ExactRule(";")));
		final var definition =createDefinitionRule();
		final var definitionProperty =new NodeRule("definition", definition);
		final var params =new OrRule(List.of(new DivideRule("params", ValueDivider.VALUE_DIVIDER, definition), new ExactRule("")));
		final var infixRule =new InfixRule(definitionProperty, new FirstLocator("("), new InfixRule(params, new FirstLocator(")"), orRule));
		return new TypeRule("method", infixRule);
	}
	private static TypeRule createBlockRule(Rule statement){
		return new TypeRule(BLOCK, new StripRule(new PrefixRule("{", new SuffixRule(new StripRule(createContentRule(statement), "", BLOCK_AFTER_CHILDREN), "}"))));
	}
	private static Rule createContentRule(Rule rule){
		return new DivideRule(GENERIC_CHILDREN, StatementDivider.STATEMENT_DIVIDER, new StripRule(rule, CONTENT_BEFORE_CHILD, ""));
	}
	private static Rule createStatementRule();
	private static TypeRule createKeywordRule(){
		return new TypeRule("continue", new ExactRule("continue;"));
	}
	private static TypeRule createElseRule(LazyRule statement){
		return new TypeRule("else", new StripRule(new PrefixRule("else ", new OrRule(List.of(
                new NodeRule("child", createBlockRule(statement)),
                new NodeRule("value", statement)
 )))));
	}
	private static TypeRule createConditionalRule(LazyRule statement,  String type);
	private static TypeRule createWhitespaceRule(){
		return new TypeRule(WHITESPACE_TYPE, new StripRule(new ExactRule("")));
	}
	private static Rule createPostfixRule(String type,  String operator,  Rule value){
		return new TypeRule(type, new SuffixRule(new NodeRule("value", value), operator+";"));
	}
	private static Rule createAssignmentRule(Rule value){
		final var destination =new NodeRule("destination", value);
		final var source =new NodeRule("source", value);
		return new TypeRule("assignment", new SuffixRule(new InfixRule(destination, new FirstLocator("="), source), ";"));
	}
	private static Rule createInvocationStatementRule(Rule value){
		return new SuffixRule(createInvocationRule(value), ";");
	}
	private static TypeRule createInvocationRule(Rule value);
	private static Rule createReturnRule(Rule value){
		return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule("value", value), ";"))));
	}
	private static Rule createValueRule(Rule statement);
	private static TypeRule createLambdaRule(Rule statement,  LazyRule value){
		return new TypeRule("lambda", new InfixRule(new StringRule("args"), new FirstLocator("->"), new OrRule(List.of(
                new NodeRule("child", createBlockRule(statement)),
                new NodeRule("child", value)
 ))));
	}
	private static TypeRule createStringRule(){
		final var value =new PrefixRule("\"", new SuffixRule(new StringRule("value"), "\""));
		return new TypeRule("string", new StripRule(value));
	}
	private static TypeRule createTernaryRule(LazyRule value){
		return new TypeRule("ternary", new InfixRule(new NodeRule("condition", value), new FirstLocator("?"), new InfixRule(new NodeRule("ifTrue", value), new FirstLocator(":"), new NodeRule("ifElse", value))));
	}
	private static TypeRule createCharRule(){
		return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule("value"), "'"))));
	}
	private static TypeRule createNumberRule(){
		return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule("value"))));
	}
	private static TypeRule createOperatorRule(String type,  String operator,  LazyRule value){
		return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));
	}
	private static TypeRule createNotRule(LazyRule value){
		return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule("value", value))));
	}
	private static TypeRule createConstructionRule(LazyRule value){
		final var type =new StringRule("type");
		final var arguments =new DivideRule("arguments", ValueDivider.VALUE_DIVIDER, value);
		final var childRule =new InfixRule(type, new FirstLocator("("), new StripRule(new SuffixRule(arguments, ")")));
		return new TypeRule("construction", new StripRule(new PrefixRule("new ", childRule)));
	}
	private static Rule createSymbolRule(){
		return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(DEFAULT_VALUE))));
	}
	private static Rule createDataAccessRule(final Rule value){
		final var rule =new InfixRule(new NodeRule("ref", value), new LastLocator("."), new StringRule("property"));
		return new TypeRule("data-access", rule);
	}
	private static Rule createDefinitionRule(){
		final var modifiers =createModifiers();
		final var type =new NodeRule("type", createTypeRule());
		final var name =new StringRule("name");
		final var maybeModifiers =new OrRule(List.of());
		final var annotation =new TypeRule("annotation", new StringRule("value"));
		final var annotations =new DivideRule("annotations", new SimpleDivider("\n"), annotation);
		final var maybeAnnotations =new OrRule(List.of(new InfixRule(annotations, new LastLocator("\n"), maybeModifiers), maybeModifiers));
		final var rule =new InfixRule(maybeAnnotations, new LastLocator(" "), name);
		return new TypeRule("definition", rule);
	}
	private static DivideRule createModifiers(){
		return new DivideRule("modifiers", new SimpleDivider(" "), new StripRule(new StringRule("value")));
	}
	private static Rule createTypeRule();
	private static TypeRule createFunctionalType(Rule type){
		final var leftRule =new PrefixRule("(", new SuffixRule(new DivideRule("params", ValueDivider.VALUE_DIVIDER, type), ")"));
		final var rule =new InfixRule(leftRule, new FirstLocator(" => "), new NodeRule("return", type));
		return new TypeRule(FUNCTIONAL_TYPE, new PrefixRule("(", new SuffixRule(rule, ")")));
	}
	private static TypeRule createArrayRule(LazyRule type){
		return new TypeRule("array", new SuffixRule(new NodeRule("child", type), "[]"));
	}
	private static TypeRule createVarArgsRule(LazyRule type){
		return new TypeRule("var-args", new SuffixRule(new NodeRule("child", type), "..."));
	}
	private static TypeRule createGenericRule(LazyRule type){
		return new TypeRule("generic", new InfixRule(new StripRule(new StringRule(PARENT)), new FirstLocator("<"), new SuffixRule(new DivideRule(GENERIC_CHILDREN, ValueDivider.VALUE_DIVIDER, type), ">")));
	}
}