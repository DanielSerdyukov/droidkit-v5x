package droidkit.processor.sqlite;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.VariableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
interface FieldVisitor {

    List<FieldVisitor> SUPPORTED = Arrays.asList(
            new SQLitePkVisitor(),
            new SQLiteColumnVisitor()
    );

    Annotation getAnnotation(ProcessingEnv processingEnv, VariableElement field);

    void visit(SQLiteObjectScanner scanner, ProcessingEnv processingEnv, VariableElement field,
               Annotation annotation);

}
