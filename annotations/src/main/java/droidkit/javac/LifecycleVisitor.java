package droidkit.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
class LifecycleVisitor extends TreeTranslator {

    LifecycleVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        System.out.println("LifecycleVisitor.visitClassDef: " + jcClassDecl.name);
    }

}
