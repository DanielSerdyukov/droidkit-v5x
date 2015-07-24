package droidkit.dynamic;

import java.util.Locale;

/**
 * @author Daniel Serdyukov
 */
public class DynamicException extends Exception {

    private static final long serialVersionUID = 5891969507323459190L;

    public DynamicException(String format, Object... args) {
        super(String.format(Locale.US, format, args));
    }

    public DynamicException(Throwable throwable) {
        super(throwable);
    }

}
