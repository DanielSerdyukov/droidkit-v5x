package droidkit.apt;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * @author Daniel Serdyukov
 */
final class JCModifier {

    private static final Map<Modifier, Integer> MODIFIERS = new HashMap<>();

    static {
        MODIFIERS.put(Modifier.PUBLIC, Flags.PUBLIC);
        MODIFIERS.put(Modifier.PROTECTED, Flags.PROTECTED);
        MODIFIERS.put(Modifier.PRIVATE, Flags.PRIVATE);
        MODIFIERS.put(Modifier.STATIC, Flags.STATIC);
        MODIFIERS.put(Modifier.ABSTRACT, Flags.ABSTRACT);
        MODIFIERS.put(Modifier.NATIVE, Flags.NATIVE);
        MODIFIERS.put(Modifier.VOLATILE, Flags.VOLATILE);
        MODIFIERS.put(Modifier.FINAL, Flags.FINAL);
    }

    private JCModifier() {
    }

    public static JCTree.JCModifiers get(Modifier... modifiers) {
        return get(Arrays.asList(modifiers));
    }

    public static JCTree.JCModifiers get(long flags, Modifier... modifiers) {
        return get(flags, Arrays.asList(modifiers));
    }

    public static JCTree.JCModifiers get(Iterable<Modifier> modifiers) {
        return get(0, modifiers);
    }

    public static JCTree.JCModifiers get(long flags, Iterable<Modifier> modifiers) {
        long jcModifiers = flags;
        for (final Modifier modifier : modifiers) {
            final Integer flag = MODIFIERS.get(modifier);
            if (flag == null) {
                throw new AssertionError("Unknown modifier: " + modifier);
            }
            jcModifiers |= flag;
        }
        return JavacEnv.get().maker().Modifiers(jcModifiers);
    }

}
