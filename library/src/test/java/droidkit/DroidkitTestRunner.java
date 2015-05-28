package droidkit;

import android.support.annotation.NonNull;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

/**
 * @author Daniel Serdyukov
 */
public class DroidkitTestRunner extends RobolectricGradleTestRunner {

    private static final int MAX_SDK_LEVEL = 21;

    public DroidkitTestRunner(@NonNull Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    public Config getConfig(Method method) {
        Config config = super.getConfig(method);
        config = new Config.Implementation(
                ensureSdkLevel(config.sdk()),
                config.manifest(),
                config.qualifiers(),
                config.packageName(),
                config.resourceDir(),
                config.assetDir(),
                config.shadows(),
                config.application(),
                config.libraries(),
                ensureBuildConfig(config.constants()));

        return config;
    }

    private Class<?> ensureBuildConfig(Class<?> constants) {
        if (constants == Void.class) {
            return BuildConfig.class;
        }
        return constants;
    }

    private int[] ensureSdkLevel(int[] sdkLevel) {
        if (sdkLevel.length == 0 || sdkLevel[0] > MAX_SDK_LEVEL) {
            return new int[]{MAX_SDK_LEVEL};
        }
        return sdkLevel;
    }

}
