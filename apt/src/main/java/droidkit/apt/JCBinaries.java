package droidkit.apt;

import com.google.common.base.Objects;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Daniel Serdyukov
 */
class JCBinaries {

    private static final JCBinaryImpl IMPL;

    static {
        if (Runtime.class.getPackage().getImplementationVersion().startsWith("1.7")) {
            IMPL = new JCBinaryImplV7();
        } else {
            IMPL = new JCBinaryImplV8();
        }
    }

    static JCTree.JCBinary notNull(TreeMaker maker, JCTree.JCExpression expression) {
        return IMPL.notNull(maker, expression);
    }

    private interface JCBinaryImpl {
        JCTree.JCBinary notNull(TreeMaker maker, JCTree.JCExpression expression);
    }

    private static final class JCBinaryImplV7 implements JCBinaryImpl {

        @Override
        public JCTree.JCBinary notNull(TreeMaker maker, JCTree.JCExpression expression) {
            try {
                final Class<?> makerClass = maker.getClass();
                final Field ne = JCTree.class.getDeclaredField("NE");
                final Field bot = Class.forName("com.sun.tools.javac.code.TypeTags").getDeclaredField("BOT");

                final Method literal = makerClass.getDeclaredMethod("Literal", int.class, Object.class);
                final Method binary = makerClass.getDeclaredMethod("Binary", int.class,
                        JCTree.JCExpression.class, JCTree.JCExpression.class);

                return (JCTree.JCBinary) binary.invoke(maker, ne.get(null), expression,
                        literal.invoke(maker, bot.get(null), null));
            } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException
                    | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Unsupported compiler version", e);
            }
        }

    }

    private static final class JCBinaryImplV8 implements JCBinaryImpl {

        @Override
        public JCTree.JCBinary notNull(TreeMaker maker, JCTree.JCExpression expression) {
            try {
                final Class<? extends TreeMaker> makerClass = maker.getClass();
                final Class<?> treeTag = Class.forName("com.sun.tools.javac.tree.JCTree$Tag");
                final Object ne = getEnumValue(treeTag, "NE");
                final Class<?> typeTag = Class.forName("com.sun.tools.javac.code.TypeTag");
                final Object bot = getEnumValue(typeTag, "BOT");

                final Method literal = makerClass.getDeclaredMethod("Literal", typeTag, Object.class);
                final Method binary = makerClass.getDeclaredMethod("Binary", treeTag,
                        JCTree.JCExpression.class, JCTree.JCExpression.class);
                return (JCTree.JCBinary) binary.invoke(maker, ne, expression, literal.invoke(maker, bot, null));
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                    | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Unsupported compiler version", e);
            }
        }

        private Object getEnumValue(Class<?> type, String valueName) throws ClassNotFoundException,
                NoSuchFieldException {
            if (type.isEnum()) {
                for (final Object value : type.getEnumConstants()) {
                    if (Objects.equal(valueName, String.valueOf(value))) {
                        return value;
                    }
                }
            }
            throw new NoSuchFieldException(valueName);
        }

    }

}
