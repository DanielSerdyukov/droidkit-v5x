package droidkit.sqlite;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteException extends RuntimeException {

    private static final long serialVersionUID = -5084244482543335389L;

    public SQLiteException() {
    }

    public SQLiteException(String detailMessage) {
        super(detailMessage);
    }

    public SQLiteException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SQLiteException(Throwable throwable) {
        super(throwable);
    }

}
