package droidkit.apt;

import java.util.Iterator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Daniel Serdyukov
 */
final class Utils {

    private Utils() {
    }

    public static void checkArgument(boolean condition, String format, Object... args) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(format, args));
        }
    }

    public static String join(String separator, Iterable<Object> parts) {
        final Iterator<Object> iterator = parts.iterator();
        final StringBuilder result = new StringBuilder();
        while (iterator.hasNext()) {
            result.append(String.valueOf(iterator.next()));
            if (iterator.hasNext()) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    public static boolean isClassOrInterface(Element e) {
        return e.getKind().isClass() || e.getKind().isInterface();
    }

    public static PackageElement getPackage(Element type) {
        while (type.getKind() != ElementKind.PACKAGE) {
            type = type.getEnclosingElement();
        }
        return (PackageElement) type;
    }

    public static boolean isSubtype(Element element, String baseType) {
        return isSubtype(element.asType(), baseType);
    }

    public static boolean isSubtype(TypeMirror mirror, String baseType) {
        final JavacEnv env = JavacEnv.get();
        return env.isSubtype(env.getElement(mirror), env.getElement(baseType));
    }

}
