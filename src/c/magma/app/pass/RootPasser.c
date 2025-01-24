#include "../../../magma/api/result/Ok.h"
#include "../../../magma/api/result/Result.h"
#include "../../../magma/app/MapNode.h"
#include "../../../magma/app/Node.h"
#include "../../../magma/app/error/CompileError.h"
#include "../../../java/util/ArrayList.h"
#include "../../../java/util/Collections.h"
#include "../../../java/util/List.h"
#include "../../../java/util/Optional.h"
#include "../../../java/util/stream/IntStream.h"
#include "../../../java/util/stream/Stream.h"
struct RootPasser implements Passer{
	PassUnit<Node> resolveImport(PassUnit<Node> unit){
		var fileNamespace=unit.findNamespace();
		unit.withValue(unit.value().retype("include").mapNodeList("namespace", namespaceNodes -> {
            var oldNamespace=namespaceNodes.stream().map(()->node.findString("value")).flatMap(Optional::stream).toList();
            final var newNamespace = new ArrayList<String>();
            IntStream.range(0, fileNamespace.size()).forEach(_ -> {
                newNamespace.add("..");
            });
            newNamespace.addAll(oldNamespace);
            return newNamespace.stream().map(()->new MapNode("segment").withString("value", value)).toList();
        }));
	}
	List<Node> removeFunctionalImports(List<Node> children){
		return children.stream().flatMap(RootPasser::removeFunctionalImport).toList();
	}
	Stream<Node> removeFunctionalImport(Node child){
		if(!child.is("import"))return Stream.of(child);
		var namespace=child.findNodeList("namespace").orElse(Collections.emptyList()).stream().map(()->node.findString("value")).flatMap(Optional::stream).toList();
		if(!namespace.equals(List.of("java", "util", "function", "Function")))return Stream.of(child);
		return Stream.empty();
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), ()->node.retype("struct")).or(()->unit.filter(Passer.by("import")).map(RootPasser::resolveImport)).or(()->unit.filterAndMapToValue(Passer.by("root"), ()->{
			return node.mapNodeList("children", RootPasser::removeFunctionalImports);
		})).orElse(unit));
	}
}
