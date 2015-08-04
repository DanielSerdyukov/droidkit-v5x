package droidkit.processor;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public class JCLiterals {

    private static final JCLiteralImpl IMPL;

    static {
        if (Runtime.class.getPackage().getImplementationVersion().startsWith("1.7")) {
            IMPL = new JCLiteralImplV7();
        } else {
            IMPL = new JCLiteralImplV8();
        }
    }

    public static JCTree.JCLiteral stringValue(TreeMaker maker, String value) {
        return IMPL.valueOf(maker, value);
    }

    private interface JCLiteralImpl {
        JCTree.JCLiteral valueOf(TreeMaker maker, String value);
    }

    private static final class JCLiteralImplV7 implements JCLiteralImpl {
        @Override
        public JCTree.JCLiteral valueOf(TreeMaker maker, String value) {
            try {
                final Class<?> makerClass = maker.getClass();
                final Field classTag = Class.forName("com.sun.tools.javac.code.TypeTags").getDeclaredField("CLASS");
                final Method literal = makerClass.getDeclaredMethod("Literal", int.class, Object.class);
                return (JCTree.JCLiteral) literal.invoke(maker, classTag.get(null), value);
            } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException
                    | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Unsupported compiler version", e);
            }
        }

    }

    private static final class JCLiteralImplV8 implements JCLiteralImpl {

        @Override
        public JCTree.JCLiteral valueOf(TreeMaker maker, String value) {
            try {
                final Class<?> makerClass = maker.getClass();
                final Class<?> typeTag = Class.forName("com.sun.tools.javac.code.TypeTag");
                final Method literal = makerClass.getDeclaredMethod("Literal", typeTag, Object.class);
                return (JCTree.JCLiteral) literal.invoke(maker, getEnumValue(typeTag, "CLASS"), value);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                    | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Unsupported compiler version", e);
            }
        }

        private Object getEnumValue(Class<?> type, String valueName) throws ClassNotFoundException,
                NoSuchFieldException {
            if (type.isEnum()) {
                for (final Object value : type.getEnumConstants()) {
                    if (Objects.equals(valueName, String.valueOf(value))) {
                        return value;
                    }
                }
            }
            throw new NoSuchFieldException(valueName);
        }

    }

}
