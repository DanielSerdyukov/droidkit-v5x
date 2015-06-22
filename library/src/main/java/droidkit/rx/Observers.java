package droidkit.rx;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public final class Observers {

    private static final Observer<?> EMPTY = new Observer<Object>() {
        @Override
        public void onNext(@NonNull Object data) {

        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(@NonNull Throwable e) {
            throw new UnsupportedOperationException();
        }
    };

    private Observers() {
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Observer<T> empty() {
        return (Observer<T>) EMPTY;
    }

    @NonNull
    public static <T> Observer<T> create(@NonNull final Action1<T> onNext) {
        return new Observer<T>() {
            @Override
            public void onNext(@NonNull T data) {
                onNext.call(data);
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(@NonNull Throwable e) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @NonNull
    public static <T> Observer<T> create(@NonNull final Action1<T> onNext, @NonNull final Action1<Throwable> onError) {
        return new Observer<T>() {
            @Override
            public void onNext(@NonNull T data) {
                onNext.call(data);
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(@NonNull Throwable e) {
                onError.call(e);
            }
        };
    }

    @NonNull
    public static <T> Observer<T> create(@NonNull final Action1<T> onNext, @NonNull final Action1<Throwable> onError,
                                         @NonNull final Action0 onComplete) {
        return new Observer<T>() {
            @Override
            public void onNext(@NonNull T data) {
                onNext.call(data);
            }

            @Override
            public void onComplete() {
                onComplete.call();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                onError.call(e);
            }
        };
    }

}
