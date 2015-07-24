package droidkit.sqlite;

import java.util.Locale;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteException extends RuntimeException {

    private static final long serialVersionUID = -5084244482543335389L;

    public SQLiteException(String format, Object... args) {
        super(String.format(Locale.US, format, args));
    }

    public SQLiteException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SQLiteException(Throwable throwable) {
        super(throwable);
    }

}
