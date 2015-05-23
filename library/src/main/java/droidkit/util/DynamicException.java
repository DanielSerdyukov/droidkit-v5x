package droidkit.util;

/**
 * @author Daniel Serdyukov
 */
public class DynamicException extends Exception {

    private static final long serialVersionUID = -3763813265935821957L;

    public DynamicException() {
    }

    public DynamicException(String detailMessage) {
        super(detailMessage);
    }

    public DynamicException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DynamicException(Throwable throwable) {
        super(throwable);
    }

}
