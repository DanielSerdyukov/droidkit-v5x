package droidkit.test;

import android.support.annotation.NonNull;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

import droidkit.BuildConfig;

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
        config = new Config.Implementation(ensureSdkLevel(
                config.emulateSdk()),
                config.manifest(),
                config.qualifiers(),
                config.resourceDir(),
                config.assetDir(),
                ensureSdkLevel(config.reportSdk()),
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

    private int ensureSdkLevel(int sdkLevel) {
        if (sdkLevel > MAX_SDK_LEVEL) {
            return MAX_SDK_LEVEL;
        }
        if (sdkLevel <= 0) {
            return MAX_SDK_LEVEL;
        }
        return sdkLevel;
    }

}
