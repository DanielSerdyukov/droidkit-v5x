package droidkit.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public final class AppInfo {

    private AppInfo() {
    }

    @NonNull
    public static Bundle getMetaData(@NonNull Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException ignored) {
            return Bundle.EMPTY;
        }
    }

}
