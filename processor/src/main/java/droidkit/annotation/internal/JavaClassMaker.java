package droidkit.annotation.internal;

import java.io.IOException;

/**
 * @author Daniel Serdyukov
 */
interface JavaClassMaker {

    String AUTO_GENERATED = "AUTO-GENERATED FILE. DO NOT MODIFY.";

    void makeJavaFile() throws IOException;

}
