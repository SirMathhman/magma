package com.meti.app.compile.transform;

import com.meti.app.compile.MapNode;
import com.meti.app.compile.Node;
import com.meti.app.compile.attribute.Attribute;
import com.meti.app.compile.attribute.ExtractAttribute;
import com.meti.app.compile.attribute.StringAttribute;
import com.meti.core.Option;
import com.meti.java.JavaMap;
import com.meti.java.JavaString;
import com.meti.java.Map;
import com.meti.java.String_;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtractorTest {
    private static Option<Map<String_, Attribute>> extractImpl(Node left, Node right) {
        return new Extractor(left, right).extract();
    }

    @Test
    void invalid() {
        var left = MapNode.create("a").complete();
        var right = MapNode.create("b").complete();

        var extract = extractImpl(left, right);
        assertFalse(extract.isPresent());
    }

    @Test
    void sameType() {
        var left = MapNode.create("a").complete();
        var right = MapNode.create("a").complete();

        var extract = extractImpl(left, right);
        assertTrue(extract.isPresent());
    }

    @Test
    void sameAttribute() {
        var left = MapNode.create("a")
                .withString("test", "foo")
                .complete();

        var right = MapNode.create("a")
                .withAttribute("test", ExtractAttribute.Extract)
                .complete();

        var extract = extractImpl(left, right)
                .unwrapOrElse(JavaMap.empty())
                .applyOptionally(JavaString.fromSlice("test"))
                .$();

        assertTrue(extract.equalsTo(new StringAttribute(JavaString.fromSlice("foo"))));
    }

    @Test
    void differentAttribute() {
        var left = MapNode.create("a")
                .withString("test", "foo")
                .complete();

        var right = MapNode.create("a")
                .withAttribute("testa", ExtractAttribute.Extract)
                .complete();

        assertFalse(extractImpl(left, right).isPresent());
    }
}