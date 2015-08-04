package droidkit.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public final class AppInfo {

    private AppInfo() {
    }

    @NonNull
    public static Bundle getMetaData(@NonNull Context context) {
        try {
            return Objects.requireNonNull(context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData, Bundle.EMPTY);
        } catch (PackageManager.NameNotFoundException ignored) {
            return Bundle.EMPTY;
        }
    }

}
