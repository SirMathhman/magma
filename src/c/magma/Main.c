struct Main{
	Path SOURCE_DIRECTORY=JavaPaths.get(".", "src", "java");
	Path TARGET_DIRECTORY=JavaPaths.get(".", "src", "c");
	void main(String[] args){
		collect().mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(Main::runWithSources).match(Function.identity(), Some::new).ifPresent(()->System.err.println(error.display()));
	}
	Result<JavaSet<Path>, IOException> collect(){
		return SOURCE_DIRECTORY.walkWrapped().mapValue(()->paths.stream().filter(JavaFiles::isRegularFile).filter(()->path.format().endsWith(".java")).collect(JavaSet.collect()));
	}
	Option<ApplicationError> runWithSources(JavaSet<Path> sources){
		return sources.stream().map(Main::runWithSource).flatMap(Streams::fromOption).next();
	}
	Option<ApplicationError> runWithSource(Path source){
		var relative=SOURCE_DIRECTORY.relativize(source);
		return relative.findParent().flatMap(()->runWithRelative(source, parent, relative));
	}
	Option<ApplicationError> runWithRelative(Path source, Path parent, Path relative){
		var namespace=parent.stream().map(Path::format).collect(new JavaListCollector<String>());
		if(shouldSkip(namespace)){
			return new None<>();
		}
		var nameWithExt=relative.getFileName().format();
		var name=nameWithExt.substring(0, nameWithExt.indexOf('.''));
		var copy=namespace.add(name);
		var joined=copy.stream().collect(new JoiningCollector(".")).orElse("");
		System.out.println("Compiling source: "+joined);
		var targetParent=TARGET_DIRECTORY.resolvePath(parent);
		if(!targetParent.isExists()){
			var directoriesError=targetParent.createAsDirectories();
			if(directoriesError.isPresent())return directoriesError.map(JavaError::new).map(ApplicationError::new);
		}
		return source.readStrings().mapErr(JavaError::new).mapErr(ApplicationError::new).flatMapValue(()->compile(input, namespace).mapErr(ApplicationError::new)).mapValue(()->writeOutput(output, targetParent, name)).match(Function.identity(), Some::new);
	}
	boolean shouldSkip(JavaList<String> namespace){
		return namespace.subList(0, 2).filter(()->slice.equals(JavaList.of("magma", "java"))).isPresent();
	}
	Result<Map<String, String>, CompileError> compile(String input, JavaList<String> namespace){
		return JavaLang.createJavaRootRule().parse(input).flatMapValue(()->pass(new RootPasser(), namespace, root)).flatMapValue(()->pass(new CFormatter(), namespace, root)).flatMapValue(()->root.streamNodes().foldLeftToResult(new HashMap<String, String>(), Main::generateTarget));
	}
	Result<Node, CompileError> pass(Passer passer, JavaList<String> namespace, Node root){
		var unit=new InlinePassUnit<>(root, namespace);
		return new TreePassingStage(passer).pass(unit).mapValue(PassUnit::value);
	}
	Result<Map<String, String>, CompileError> generateTarget(Map<String, String> map, Tuple<String, Node> tuple){
		var key=tuple.left();
		var root=tuple.right();
		return CLang.createCRootRule().generate(root).mapValue(()->{
			map.put(key, generated);
			return map;
		});
	}
	Option<ApplicationError> writeOutput(Map<String, String> output, Path targetParent, String name){
		var target=targetParent.resolveChild(name+".c");
		var header=targetParent.resolveChild(name+".h");
		return target.writeString(output.get("source")).or(()->header.writeString(output.get("header"))).map(JavaError::new).map(ApplicationError::new);
	}
}
