package droidkit.processor;

import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.SQLiteObject;
import droidkit.processor.app.ActivityScanner;
import droidkit.processor.app.FragmentScanner;
import droidkit.processor.content.LoaderCallbacksScanner;
import droidkit.processor.sqlite.SQLiteObjectScanner;
import droidkit.processor.view.ViewScanner;
import rx.functions.Func3;

/**
 * @author Daniel Serdyukov
 */
class ScannerFactory {

    private static final List<FactoryFunc> FUNCTIONS = Arrays.asList(
            new SQLiteObjectFunc(),
            new OnCreateLoaderFunc(),
            new ActivityFunc(),
            new FragmentFunc(),
            new ViewFunc()
    );

    private final ProcessingEnv mProcessingEnv;

    ScannerFactory(ProcessingEnvironment processingEnv) {
        mProcessingEnv = new ProcessingEnv(processingEnv);
    }

    ElementScanner getScanner(final TypeElement annotation, TypeElement element) {
        for (final FactoryFunc func : FUNCTIONS) {
            final ElementScanner scanner = func.call(mProcessingEnv, annotation, element);
            if (scanner != null) {
                return scanner;
            }
        }
        throw new IllegalArgumentException("Unexpected annotation " + annotation);
    }

    //region factory functions
    private interface FactoryFunc extends Func3<ProcessingEnv, TypeElement, TypeElement, ElementScanner> {

    }

    private static class SQLiteObjectFunc implements FactoryFunc {

        @Override
        public ElementScanner call(ProcessingEnv env, TypeElement annotation, TypeElement element) {
            if (env.isSubtype(annotation.asType(), SQLiteObject.class)) {
                return new SQLiteObjectScanner(env);
            }
            return null;
        }

    }

    private static class OnCreateLoaderFunc implements FactoryFunc {

        @Override
        public ElementScanner call(ProcessingEnv env, TypeElement annotation, TypeElement element) {
            if (env.isSubtype(annotation.asType(), OnCreateLoader.class)) {
                return new LoaderCallbacksScanner(env);
            }
            return null;
        }

    }

    private static class ActivityFunc implements FactoryFunc {

        @Override
        public ElementScanner call(ProcessingEnv env, TypeElement annotation, TypeElement element) {
            if (env.isSubtype(element.asType(), "android.app.Activity")) {
                return new ActivityScanner(env);
            }
            return null;
        }

    }

    private static class FragmentFunc implements FactoryFunc {

        @Override
        public ElementScanner call(ProcessingEnv env, TypeElement annotation, TypeElement element) {
            if (env.isSubtype(element.asType(), "android.app.Fragment")) {
                return new FragmentScanner(env);
            }
            return null;
        }

    }

    private static class ViewFunc implements FactoryFunc {

        @Override
        public ElementScanner call(ProcessingEnv env, TypeElement annotation, TypeElement element) {
            if (env.isSubtype(element.asType(), "android.view.View")) {
                return new ViewScanner(env);
            }
            return null;
        }

    }
    //endregion

}
