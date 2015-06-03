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
class ActivityProcessor implements IProcessor, JavaClassMaker {

    private final ViewInjector mViewInjector;

    private final TypeElement mOriginElement;

    private final ClassName mOriginClass;

    private boolean mDone;

    public ActivityProcessor(TypeElement originElement) {
        mViewInjector = new ViewInjector(originElement);
        mOriginElement = originElement;
        mOriginClass = ClassName.get(originElement);
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
                .addMethod(makeSetContentView1())
                .addMethod(makeSetContentView2())
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

    /*private FieldSpec makeDelegateField() {
        return FieldSpec.builder(ClassName.get(mOriginElement), "mDelegate", ).build();
    }*/

    private MethodSpec makeSetContentView1() {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "layoutResId")
                .addStatement("super.setContentView(layoutResId)");
        if (!mViewInjector.isEmpty()) {
            method.addStatement("$T$$ViewInjector.inject(($T) this)", mOriginClass, mOriginClass);
        }
        return method.build();
    }

    private MethodSpec makeSetContentView2() {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addStatement("super.setContentView(view)");
        if (!mViewInjector.isEmpty()) {
            method.addStatement("$T$$ViewInjector.inject(($T) this)", mOriginClass, mOriginClass);
        }
        return method.build();
    }

}
