package droidkit;

import android.support.annotation.NonNull;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.Method;

/**
 * @author Daniel Serdyukov
 */
public class DroidkitTestRunner extends RobolectricGradleTestRunner {

    private static final int MAX_SDK_LEVEL = 21;

    private static final String BUILD_OUTPUT = "build/intermediates/bundles";

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

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        final String buildType = getBuildType(config);
        final String applicationId = getApplicationId(config);

        final FileFsFile res = FileFsFile.from(BUILD_OUTPUT, buildType, "res");
        final FileFsFile assets = FileFsFile.from(BUILD_OUTPUT, buildType, "assets");
        final FileFsFile manifest = FileFsFile.from(BUILD_OUTPUT, buildType, "AndroidManifest.xml");

        return new AndroidManifest(manifest, res, assets, applicationId);
    }

    private String getBuildType(Config config) {
        try {
            return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
        } catch (Throwable e) {
            return null;
        }
    }

    private String getApplicationId(Config config) {
        try {
            return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
        } catch (Throwable e) {
            return null;
        }
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
