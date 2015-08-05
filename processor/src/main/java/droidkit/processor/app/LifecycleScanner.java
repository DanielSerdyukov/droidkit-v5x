package droidkit.processor.app;

import javax.lang.model.element.TypeElement;

import droidkit.processor.ElementScanner;
import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class LifecycleScanner extends ElementScanner {

    private final ProcessingEnv mProcessingEnv;

    private TypeElement mOriginType;

    LifecycleScanner(ProcessingEnv env) {
        super();
        mProcessingEnv = env;
    }

}
