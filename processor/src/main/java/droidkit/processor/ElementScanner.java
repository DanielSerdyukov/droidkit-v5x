package droidkit.processor;

import javax.lang.model.util.ElementScanner7;

/**
 * @author Daniel Serdyukov
 */
public class ElementScanner extends ElementScanner7<Void, Void> {

    protected static final String AUTO_GENERATED_FILE = "AUTO-GENERATED FILE. DO NOT MODIFY.";

    private final ProcessingEnv mProcessingEnv;

    public ElementScanner(ProcessingEnv processingEnv) {
        super(null);
        mProcessingEnv = processingEnv;
    }

    public void visitStart() {

    }

    public void visitEnd() {

    }

    public ProcessingEnv getEnv() {
        return mProcessingEnv;
    }

}
