package droidkit.apt;

import java.io.IOException;

import javax.annotation.processing.RoundEnvironment;

/**
 * @author Daniel Serdyukov
 */
interface Apt {

    String AUTO_GENERATED = "AUTO-GENERATED FILE. DO NOT MODIFY.";

    void process(RoundEnvironment roundEnv);

    void finishProcessing() throws IOException;

}
