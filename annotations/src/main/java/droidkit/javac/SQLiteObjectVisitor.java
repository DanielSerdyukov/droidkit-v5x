package droidkit.javac;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner7;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLitePk;
import rx.functions.Action3;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectVisitor extends ElementScanner7<Void, Void> {

    private static final Map<Class<? extends Annotation>, FieldVisitor> VISITORS = new LinkedHashMap<>();

    static {
        VISITORS.put(SQLitePk.class, new SQLitePkVisitor());
        VISITORS.put(SQLiteColumn.class, new SQLiteColumnVisitor());
    }

    private final Map<String, String> mColumns = new HashMap<>();

    private final Map<String, String> mSetters = new HashMap<>();

    private final SQLiteObjectMaker mClassMaker;

    private final TreeMaker mTreeMaker;

    private final Trees mTrees;

    private final Names mNames;

    private final JCTypes mTypes;

    private final TypeElement mElement;

    SQLiteObjectVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
        mClassMaker = new SQLiteObjectMaker(processingEnv, element);
        final JavacProcessingEnvironment javacEnv = (JavacProcessingEnvironment) processingEnv;
        mTreeMaker = TreeMaker.instance(javacEnv.getContext());
        mTrees = Trees.instance(processingEnv);
        mNames = Names.instance(javacEnv.getContext());
        mTypes = JCTypes.instance(javacEnv);
        mElement = element;
    }

    @Override
    public Void visitVariable(VariableElement field, Void aVoid) {
        for (final Map.Entry<Class<? extends Annotation>, FieldVisitor> entry : VISITORS.entrySet()) {
            final Annotation annotation = field.getAnnotation(entry.getKey());
            if (annotation != null) {
                entry.getValue().call(this, field, annotation);
            }
        }
        return super.visitVariable(field, aVoid);
    }

    @Override
    public Void visitExecutable(ExecutableElement method, Void aVoid) {
        final String fieldName = mSetters.get(String.valueOf(method.getSimpleName()));
        if (!Strings.isNullOrEmpty(fieldName)) {
            ((JCTree) mTrees.getTree(method)).accept(new TreeTranslator() {
                @Override
                public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                    super.visitMethodDef(jcMethodDecl);
                    jcMethodDecl.body.stats = List.<JCTree.JCStatement>of(mTreeMaker.Try(
                            mTreeMaker.Block(0, jcMethodDecl.body.stats),
                            List.<JCTree.JCCatch>nil(),
                            mTreeMaker.Block(0, List.<JCTree.JCStatement>of(
                                    mTreeMaker.Exec(mTreeMaker.Apply(
                                            List.<JCTree.JCExpression>nil(),
                                            mTypes.ident(Arrays.asList(
                                                    mClassMaker.getPackageName(),
                                                    mClassMaker.getClassName(),
                                                    "update"
                                            )),
                                            List.of(
                                                    mTypes.thisIdent("mClientRef"),
                                                    JCLiterals.valueOf(mTreeMaker, mColumns.get(fieldName)),
                                                    mTypes.thisIdent(fieldName),
                                                    mTypes.thisIdent(mClassMaker.getPrimaryKey())
                                            )
                                    ))
                            ))
                    ));
                    this.result = jcMethodDecl;
                    System.out.println(jcMethodDecl);
                }
            });
        }
        return super.visitExecutable(method, aVoid);
    }

    @Override
    public Void visitType(TypeElement element, Void aVoid) {
        if (mElement.equals(element)) {
            ((JCTree) mTrees.getTree(element)).accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    super.visitClassDef(jcClassDecl);
                    final ListBuffer<JCTree> defs = new ListBuffer<>();
                    defs.addAll(jcClassDecl.defs);
                    defs.add(mTreeMaker.VarDef(
                            mTreeMaker.Modifiers(0),
                            mNames.fromString("mClientRef"),
                            mTreeMaker.TypeApply(
                                    mTypes.getClass(Reference.class),
                                    List.of(mTypes.ident(Arrays.asList("droidkit.sqlite", "SQLiteClient")))),
                            null
                    ));
                    jcClassDecl.defs = defs.toList();
                    this.result = jcClassDecl;
                }
            });
        }
        return super.visitType(element, aVoid);
    }

    void brewJavaClass() {
        mClassMaker.brewJavaClass();
    }

    private void visitPrimaryKey(VariableElement field, SQLitePk annotation) {
        final JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) mTrees.getTree(field);
        jcVariable.mods.flags &= ~Flags.PRIVATE;
        final String fieldName = String.valueOf(field.getSimpleName());
        mClassMaker.setPrimaryKey(fieldName, jcVariable.vartype.type, annotation.value());
        mColumns.put(fieldName, "_id");
        mSetters.put(Strings.nonEmpty(annotation.setter(), getSetterName(fieldName)), fieldName);
    }

    private void visitSQLiteColumn(VariableElement field, SQLiteColumn annotation) {
        final JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) mTrees.getTree(field);
        jcVariable.mods.flags &= ~Flags.PRIVATE;
        final String fieldName = String.valueOf(field.getSimpleName());
        final String columnName = Strings.nonEmpty(annotation.value(), getColumnName(fieldName));
        mClassMaker.addColumn(fieldName, jcVariable.vartype.type, columnName);
        mColumns.put(fieldName, columnName);
        mSetters.put(Strings.nonEmpty(annotation.setter(), getSetterName(fieldName)), fieldName);
    }

    private String getColumnName(String fieldName) {
        if (fieldName.startsWith("m")) {
            return Strings.toUnderScope(fieldName.substring(1));
        }
        return Strings.toUnderScope(fieldName);
    }

    private String getSetterName(String fieldName) {
        if (fieldName.startsWith("m")) {
            return "set" + fieldName.substring(1);
        }
        return "set" + Strings.capitalize(fieldName);
    }

    //region Visitors
    private interface FieldVisitor extends Action3<SQLiteObjectVisitor, VariableElement, Annotation> {

    }

    private static class SQLitePkVisitor implements FieldVisitor {

        @Override
        public void call(SQLiteObjectVisitor visitor, VariableElement field, Annotation annotation) {
            visitor.visitPrimaryKey(field, (SQLitePk) annotation);
        }

    }

    private static class SQLiteColumnVisitor implements FieldVisitor {

        @Override
        public void call(SQLiteObjectVisitor visitor, VariableElement field, Annotation annotation) {
            visitor.visitSQLiteColumn(field, (SQLiteColumn) annotation);
        }

    }
    //endregion

}
