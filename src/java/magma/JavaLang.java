package magma;

import magma.rule.*;

import java.util.List;

public class JavaLang {
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";
    public static final String BLOCK_EMPTY = " {}";
    public static final String CLASS_TYPE = "class";
    public static final String PACKAGE_TYPE = "package";
    public static final String STATIC_KEYWORD_WITH_SPACE = "static ";
    public static final String IMPORT_STATIC_TYPE = "import-static";

    public static Rule createClassRule() {
        return new TypeRule(CLASS_TYPE, new PrefixRule(CLASS_KEYWORD_WITH_SPACE, new SuffixRule(new StringRule(), BLOCK_EMPTY)));
    }

    public static Rule createPackageRule() {
        return new TypeRule(PACKAGE_TYPE, new PrefixRule(PACKAGE_KEYWORD_WITH_SPACE, CommonLang.createNamespaceRule()));
    }

    public static Rule createStaticImportRule() {
        return CommonLang.createImportRule(IMPORT_STATIC_TYPE, new PrefixRule(STATIC_KEYWORD_WITH_SPACE, CommonLang.createNamespaceRule()));
    }

    static SplitRule createRootJavaRule() {
        return new SplitRule(CommonLang.ROOT_CHILDREN, new OrRule(List.of(
                createPackageRule(),
                createStaticImportRule(),
                CommonLang.createInstanceImportRule(),
                createClassRule()
        )));
    }
}
