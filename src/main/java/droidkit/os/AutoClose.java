package droidkit.os;

import java.io.Closeable;

import droidkit.io.IOUtils;
import rx.functions.Action1;

/**
 * @author Daniel Serdyukov
 */
public class AutoClose<T extends Closeable> implements Action1<T> {

    @Override
    public void call(T t) {
        IOUtils.closeQuietly(t);
    }

}
