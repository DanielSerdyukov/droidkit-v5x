package droidkit.annotation.internal;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import droidkit.annotation.InjectView;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Daniel Serdyukov
 */
class ViewInjector implements JavaClassMaker {

    static final ClassName DK_VIEWS = ClassName.get("droidkit.view", "Views");

    private static final String TARGET = "target";

    private static final String ROOT = "root";

    private final CodeBlock.Builder mCodeBlock = CodeBlock.builder();

    private final TypeElement mOriginElement;

    private final TypeName mRootViewType;

    private TypeSpec mTypeSpec;

    public ViewInjector(TypeElement originElement, TypeName rootViewType) {
        mOriginElement = originElement;
        mRootViewType = rootViewType;
    }

    private static boolean checkView(Element element) {
        if (!JCUtils.isSubtype(element, "android.view.View")) {
            JCUtils.error("Unexpected field type. Expected subtype of android.view.View", element);
            return false;
        }
        return true;
    }

    void tryInject(VariableElement element, InjectView injectView) {
        if (injectView != null && checkView(element)) {
            JCUtils.<JCTree.JCVariableDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            mCodeBlock.addStatement("$L.$L = $T.findById($L, $L)",
                    TARGET, element.getSimpleName(),
                    DK_VIEWS, ROOT, injectView.value());
        }
    }

    boolean isEmpty() {
        return mCodeBlock.build().isEmpty();
    }

    @Override
    public void makeJavaFile() throws IOException {
        final CodeBlock codeBlock = mCodeBlock.build();
        if (!codeBlock.isEmpty()) {
            mTypeSpec = TypeSpec.classBuilder(mOriginElement.getSimpleName() + "$ViewInjector")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(MethodSpec.methodBuilder("inject")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addParameter(ClassName.get(mOriginElement), TARGET)
                            .addParameter(mRootViewType, ROOT)
                            .addCode(codeBlock)
                            .build())
                    .build();
            final JavaFile javaFile = JavaFile.builder(mOriginElement.getEnclosingElement().toString(), mTypeSpec)
                    .addFileComment(AUTO_GENERATED)
                    .build();
            final JavaFileObject sourceFile = JCUtils.ENV.getFiler()
                    .createSourceFile(javaFile.packageName + "." + mTypeSpec.name);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        }
    }

}
