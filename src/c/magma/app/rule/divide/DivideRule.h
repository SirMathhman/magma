#include "../../../../magma/api/result/Err.h"
#include "../../../../magma/api/result/Ok.h"
#include "../../../../magma/api/result/Result.h"
#include "../../../../magma/api/stream/Streams.h"
#include "../../../../magma/app/MapNode.h"
#include "../../../../magma/app/Node.h"
#include "../../../../magma/app/error/CompileError.h"
#include "../../../../magma/app/error/context/NodeContext.h"
#include "../../../../magma/app/rule/Rule.h"
#include "../../../../java/util/ArrayList.h"
#include "../../../../java/util/List.h"
#include "../../../../java/util/Optional.h"
#include "../../../../java/util/function/BiFunction.h"
#include "../../../../java/util/function/Function.h"
struct DivideRule implements Rule{
	String propertyKey;
	Divider divider;
	Rule childRule;
	public DivideRule(String propertyKey, Divider divider, Rule childRule){
		this.divider = divider;
		this.childRule = childRule;
		this.propertyKey = propertyKey;
	}
	<T, R>Result<List<R>, CompileError> compileAll(List<T> segments, Function<T, Result<R, CompileError>> mapper, BiFunction<T, R, Result<R, CompileError>> validator){
		return Streams.fromNativeList(segments).foldLeftToResult(new ArrayList<>(), (rs, t) -> {
            return mapper.apply(t).flatMapValue(()->validator.apply(t, inner)).mapValue(inner -> {
                        rs.add(inner);
                        return rs;
                    });
        });
	}
	Result<Node, CompileError> validateNode(String text, Node result){
		if(result.hasType()){
			return new Ok<>(result);
		}
		else{
			return new Err<>(new CompileError("Node has no type assigned", new NodeContext(result)));
		}
	}
	Result<Node, CompileError> parse(String input){
		return this.divider.divide(input).flatMapValue(()->compileAll(segments, this.childRule::parse, DivideRule::validateNode)).mapValue(()->{
			var node=new MapNode();
			return segments.isEmpty() ? node : node.withNodeList(this.propertyKey, segments);
		});
	}
	Result<String, CompileError> generate(Node node){
		return node.findNodeList(this.propertyKey).flatMap(()->list.isEmpty() ? Optional.empty() : Optional.of(list)).map(list -> compileAll(list, this.childRule::generate, (_, result) -> new Ok<>(result)))
                .map(()->result.mapValue(this::merge)).orElseGet(()->new Err<>(new CompileError("Node list '"+this.propertyKey + "' not present", new NodeContext(node))));
	}
	String merge(List<String> elements){
		return Streams.fromNativeList(elements).foldLeft(this.divider::merge).orElse("");
	}
}
