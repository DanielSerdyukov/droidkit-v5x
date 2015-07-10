package droidkit.javac;

import com.google.common.collect.ImmutableList;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner7;

import droidkit.annotation.InjectView;

/**
 * @author Daniel Serdyukov
 */
class LifecycleVisitor extends ElementScanner7<Void, Void> {

    private final TypeElement mElement;

    private final Trees mTrees;

    private final JCTypes mTypes;

    private final LifecycleProxyMaker mClassMaker;

    LifecycleVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
        mElement = element;
        mTrees = Trees.instance(processingEnv);
        mTypes = JCTypes.instance((JavacProcessingEnvironment) processingEnv);
        if (Utils.isSubtype(processingEnv, element, "android.app.Activity")) {
            mClassMaker = new ActivityProxyMaker(processingEnv, element);
        } else {
            mClassMaker = new FragmentProxyMaker(processingEnv, element);
        }
    }

    @Override
    public Void visitVariable(VariableElement e, Void aVoid) {
        final InjectView annotation = e.getAnnotation(InjectView.class);
        if (annotation != null) {
            ((JCTree.JCVariableDecl) mTrees.getTree(e)).mods.flags &= ~Flags.PRIVATE;
            mClassMaker.injectView(e.getSimpleName().toString(), annotation.value());
        }
        return super.visitVariable(e, aVoid);
    }

    void brewJavaClass() {
        mClassMaker.brewJavaClass();
        ((JCTree.JCClassDecl) mTrees.getTree(mElement)).extending =
                mTypes.ident(ImmutableList.of(mClassMaker.getPackageName(), mClassMaker.getClassName()));
    }

    /*@Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (Objects.equal(jcClassDecl.getSimpleName(), mElement.getSimpleName())) {
            mClassMaker.brewJavaClass();

        }
    }*/

    /*@Override
    public void visitVarDef(final JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if ((jcVariableDecl.mods.flags & Flags.PARAMETER) == 0) {
            jcVariableDecl.accept(new TreeTranslator() {
                @Override
                public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {
                    super.visitAnnotation(jcAnnotation);
                    if (InjectView.class.getName().equals(jcAnnotation.type.toString())) {
                        jcVariableDecl.mods.flags &= ~Flags.PRIVATE;
                        //System.out.println(jcVariableDecl.type);
                    }
                }
            });
        }
    }*/

}
