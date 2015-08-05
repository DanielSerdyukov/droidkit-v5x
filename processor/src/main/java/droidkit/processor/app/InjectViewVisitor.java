package droidkit.processor.app;

import com.squareup.javapoet.ClassName;

import java.lang.annotation.Annotation;

import javax.lang.model.element.VariableElement;

import droidkit.annotation.InjectView;
import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class InjectViewVisitor implements FieldVisitor {

    @Override
    public Annotation getAnnotation(ProcessingEnv env, VariableElement field) {
        return field.getAnnotation(InjectView.class);
    }

    @Override
    public void visit(LifecycleScanner scanner, VariableElement field, Annotation annotation) {
        scanner.views().findById("target.$L = $T.findById(root, $L)", field.getSimpleName(),
                ClassName.get("droidkit.view", "Views"), ((InjectView) annotation).value());
    }

}
