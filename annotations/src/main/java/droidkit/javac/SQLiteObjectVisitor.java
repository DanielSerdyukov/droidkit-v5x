package droidkit.javac;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectVisitor extends TreeTranslator {

    private final Map<String, String> mColumns = new HashMap<>();

    private final Map<String, String> mSetters = new HashMap<>();

    private final SQLiteObjectMaker mClassMaker;

    private final TreeMaker mTreeMaker;

    private final Names mNames;

    private final JCTypes mTypes;

    private final TypeElement mElement;

    SQLiteObjectVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
        mClassMaker = new SQLiteObjectMaker(processingEnv, element);
        final JavacProcessingEnvironment javacEnv = (JavacProcessingEnvironment) processingEnv;
        mTreeMaker = TreeMaker.instance(javacEnv.getContext());
        mNames = Names.instance(javacEnv.getContext());
        mTypes = JCTypes.instance(javacEnv);
        mElement = element;
    }

    @Override
    public void visitVarDef(final JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if ((jcVariableDecl.mods.flags & Flags.PARAMETER) == 0) {
            jcVariableDecl.accept(new TreeTranslator() {
                @Override
                public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {
                    super.visitAnnotation(jcAnnotation);
                    if (SQLitePk.class.getName().equals(jcAnnotation.type.toString())) {
                        visitPrimaryKey(jcVariableDecl, jcAnnotation);
                    } else if (SQLiteColumn.class.getName().equals(jcAnnotation.type.toString())) {
                        visitColumn(jcVariableDecl, jcAnnotation);
                    }
                }
            });
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        final String fieldName = mSetters.get(jcMethodDecl.name.toString());
        if (!Strings.isNullOrEmpty(fieldName)) {
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
        }
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (Objects.equals(jcClassDecl.getSimpleName(), mElement.getSimpleName())) {
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
            mClassMaker.brewJavaClass();
        }
    }

    private void visitPrimaryKey(JCTree.JCVariableDecl jcVariable, JCTree.JCAnnotation jcAnnotation) {
        jcVariable.mods.flags &= ~Flags.PRIVATE;
        final String fieldName = jcVariable.name.toString();
        mClassMaker.setPrimaryKey(fieldName, jcVariable.vartype.type,
                Utils.getAnnotationValue(jcAnnotation, "value", 5));
        mColumns.put(fieldName, "_id");
        mSetters.put(Utils.getAnnotationValue(jcAnnotation, "setter", getSetterName(fieldName)), fieldName);
    }

    private void visitColumn(JCTree.JCVariableDecl jcVariable, JCTree.JCAnnotation jcAnnotation) {
        jcVariable.mods.flags &= ~Flags.PRIVATE;
        final String fieldName = jcVariable.name.toString();
        final String columnName = Utils.getAnnotationValue(jcAnnotation, "value", getColumnName(fieldName));
        mClassMaker.addColumn(fieldName, jcVariable.vartype.type, columnName);
        mColumns.put(fieldName, columnName);
        mSetters.put(Utils.getAnnotationValue(jcAnnotation, "setter", getSetterName(fieldName)), fieldName);
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

}
