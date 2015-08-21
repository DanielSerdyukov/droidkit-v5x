package droidkit.crypto;

import java.security.GeneralSecurityException;

/**
 * @author Daniel Serdyukov
 */
public class DigestException extends GeneralSecurityException {

    private static final long serialVersionUID = 3096943443464723619L;

    public DigestException(Throwable throwable) {
        super(throwable);
    }

}
