package droidkit.processor;

import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.SQLiteObject;
import droidkit.processor.content.LoaderCallbacksScanner;
import droidkit.processor.sqlite.SQLiteObjectScanner;
import rx.functions.Func2;

/**
 * @author Daniel Serdyukov
 */
class ScannerFactory {

    private static final List<FactoryFunc> FUNCTIONS = Arrays.asList(
            new SQLiteObjectFunc(),
            new OnCreateLoaderFunc()
    );

    private final ProcessingEnv mProcessingEnv;

    ScannerFactory(ProcessingEnvironment processingEnv) {
        mProcessingEnv = new ProcessingEnv(processingEnv);
    }

    ElementScanner getScanner(final TypeElement annotation) {
        for (final FactoryFunc func : FUNCTIONS) {
            final ElementScanner scanner = func.call(mProcessingEnv, annotation);
            if (scanner != null) {
                return scanner;
            }
        }
        return new ElementScanner() {
            @Override
            public Void visitType(TypeElement e, Void aVoid) {
                // FIXME: 04.08.15 change to RuntimeException
                mProcessingEnv.printMessage(Diagnostic.Kind.WARNING, e, "Unhandled annotation " + annotation);
                return super.visitType(e, aVoid);
            }
        };
    }

    private interface FactoryFunc extends Func2<ProcessingEnv, TypeElement, ElementScanner> {

    }

    private static class SQLiteObjectFunc implements FactoryFunc {

        @Override
        public ElementScanner call(ProcessingEnv env, TypeElement element) {
            if (env.isSubtype(element.asType(), SQLiteObject.class)) {
                return new SQLiteObjectScanner(env);
            }
            return null;
        }

    }

    private static class OnCreateLoaderFunc implements FactoryFunc {

        @Override
        public ElementScanner call(ProcessingEnv env, TypeElement element) {
            if (env.isSubtype(element.asType(), OnCreateLoader.class)) {
                return new LoaderCallbacksScanner(env);
            }
            return null;
        }

    }

}
