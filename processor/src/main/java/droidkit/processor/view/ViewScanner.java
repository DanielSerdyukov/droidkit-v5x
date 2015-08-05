package droidkit.processor.view;

import com.squareup.javapoet.ClassName;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import droidkit.annotation.InjectView;
import droidkit.processor.ElementScanner;
import droidkit.processor.ProcessingEnv;
import droidkit.processor.ViewInjector;

/**
 * @author Daniel Serdyukov
 */
public class ViewScanner extends ElementScanner {

    private final ViewInjector mViewInjector = new ViewInjector();

    private TypeElement mOriginType;

    public ViewScanner(ProcessingEnv processingEnv) {
        super(processingEnv);
    }

    @Override
    public Void visitType(TypeElement originType, Void aVoid) {
        if (mOriginType == null) {
            mOriginType = originType;
        }
        return super.visitType(originType, aVoid);
    }

    @Override
    public Void visitVariable(VariableElement field, Void aVoid) {
        final InjectView annotation = field.getAnnotation(InjectView.class);
        if (annotation != null) {
            getEnv().<JCTree.JCVariableDecl>getTree(field).mods.flags &= ~Flags.PRIVATE;
            mViewInjector.findById("target.$L = $T.findById(root, $L)", field.getSimpleName(),
                    ClassName.get("droidkit.view", "Views"), annotation.value());
        }
        return super.visitVariable(field, aVoid);
    }

    @Override
    public void visitEnd() {
        mViewInjector.brewJava(getEnv(), mOriginType);
    }

}
