package droidkit.annotation.internal;

import com.squareup.javapoet.*;
import com.sun.tools.javac.tree.JCTree;
import droidkit.annotation.InjectView;

import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
class FragmentProcessor implements IProcessor, JavaClassMaker {

    private final ViewInjector mViewInjector;

    private final TypeElement mOriginElement;

    private final ClassName mOriginClass;

    private boolean mDone;

    public FragmentProcessor(TypeElement originElement) {
        mOriginElement = originElement;
        mOriginClass = ClassName.get(originElement);
        mViewInjector = new ViewInjector(originElement, ClassName.get("android.view", "View"));
    }

    @Override
    public void process() {
        if (!mDone) {
            final List<? extends Element> elements = mOriginElement.getEnclosedElements();
            for (final Element element : elements) {
                if (ElementKind.FIELD == element.getKind()) {
                    mViewInjector.tryInject((VariableElement) element, element.getAnnotation(InjectView.class));
                }
            }
        }
        mDone = true;
    }

    @Override
    public boolean finishProcessing() {
        try {
            mViewInjector.makeJavaFile();
            makeJavaFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void makeJavaFile() throws IOException {
        final TypeSpec spec = TypeSpec.classBuilder(mOriginElement.getSimpleName() + "$Proxy")
                .superclass(TypeName.get(mOriginElement.getSuperclass()))
                .addMethod(makeOnViewCreated())
                .build();
        final JavaFile javaFile = JavaFile.builder(mOriginElement.getEnclosingElement().toString(), spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JCUtils.ENV.getFiler()
                .createSourceFile(javaFile.packageName + "." + spec.name);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
        JCUtils.<JCTree.JCClassDecl>getTree(mOriginElement).extending = JCUtils.ident(spec.name);
    }

    private MethodSpec makeOnViewCreated() {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("onViewCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onViewCreated(view, savedInstanceState)");
        if (!mViewInjector.isEmpty()) {
            method.addStatement("$T$$ViewInjector.inject(($T) this, view)", mOriginClass, mOriginClass);
        }
        return method.build();
    }

}
