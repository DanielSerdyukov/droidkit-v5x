package droidkit.annotation.internal;

import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
class FragmentProcessor implements IProcessor {

    private final ViewInjector mViewInjector;

    private final TypeElement mOriginElement;

    public FragmentProcessor(TypeElement originElement) {
        mViewInjector = new ViewInjector(originElement);
        mOriginElement = originElement;
    }

    @Override
    public void process() {

    }

    @Override
    public boolean finishProcessing() {

        return false;
    }

}
