package magma.app.compile.pass;

import magma.app.compile.Node;

import java.util.List;
import java.util.Optional;

import static magma.app.compile.lang.CommonLang.CHILDREN;
import static magma.app.compile.lang.CommonLang.ROOT_TYPE;
import static magma.app.compile.lang.JavaLang.PACKAGE_TYPE;

public class PackageRemover implements Passer {
    private static List<Node> removePackages(List<Node> children) {
        return children.stream()
                .filter(child -> !child.is(PACKAGE_TYPE))
                .toList();
    }

    @Override
    public Optional<Node> beforePass(Node node) {
        if (!node.is(ROOT_TYPE)) return Optional.empty();
        return node.mapNodeList(CHILDREN, PackageRemover::removePackages);
    }
}
