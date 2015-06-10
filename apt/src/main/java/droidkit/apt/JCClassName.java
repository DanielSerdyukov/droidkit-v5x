package droidkit.apt;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import java.util.ArrayList;
import java.util.Collections;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.NestingKind.MEMBER;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;

/**
 * @author Daniel Serdyukov
 */
class JCClassName extends JCTypeName {

    private final java.util.List<String> mNames;

    private JCClassName(TypeTag typeTag, java.util.List<String> names) {
        super(typeTag);
        for (int i = 1; i < names.size(); i++) {
            Utils.checkArgument(SourceVersion.isName(names.get(i)), "part '%s' is keyword", names.get(i));
        }
        mNames = Collections.unmodifiableList(names);
    }

    public static JCClassName get(Class<?> clazz) {
        Utils.checkArgument(!clazz.isPrimitive(), "primitive types cannot be represented as a JCClassName");
        Utils.checkArgument(!void.class.equals(clazz), "'void' type cannot be represented as a JCClassName");
        Utils.checkArgument(!clazz.isArray(), "array types cannot be represented as a JCClassName");
        java.util.List<String> names = new ArrayList<>();
        for (Class<?> c = clazz; c != null; c = c.getEnclosingClass()) {
            names.add(c.getSimpleName());
        }
        if (clazz.getPackage() != null) {
            names.add(clazz.getPackage().getName());
        }
        Collections.reverse(names);
        return new JCClassName(TypeTag.CLASS, names);
    }

    public static JCClassName get(TypeElement element) {
        java.util.List<String> names = new ArrayList<>();
        for (Element e = element; Utils.isClassOrInterface(e); e = e.getEnclosingElement()) {
            Utils.checkArgument(element.getNestingKind() == TOP_LEVEL
                    || element.getNestingKind() == MEMBER, "unexpected type testing");
            names.add(e.getSimpleName().toString());
        }
        names.add(Utils.getPackage(element).getQualifiedName().toString());
        Collections.reverse(names);
        return new JCClassName(TypeTag.CLASS, names);
    }

    public static JCClassName get(String packageName, String simpleName, String... simpleNames) {
        final java.util.List<String> names = new ArrayList<>();
        names.add(packageName);
        names.add(simpleName);
        Collections.addAll(names, simpleNames);
        return new JCClassName(TypeTag.CLASS, names);
    }

    @Override
    public JCTree.JCExpression getIdent() {
        return JCSelector.get(mNames).getIdent();
    }

    public JCTree.JCExpression newInstance(JCTree.JCExpression... args) {
        return JavacEnv.get().maker().NewClass(
                null, // enclosing
                List.<JCTree.JCExpression>nil(), // generic types
                getIdent(), // type
                List.from(args), // args
                null
        );
    }

}
