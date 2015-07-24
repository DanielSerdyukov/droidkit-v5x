package droidkit.javac;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;
import rx.functions.Action3;
import rx.functions.Action4;
import rx.functions.Func0;
import rx.functions.Func2;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectVisitor extends ElementScanner {

    private static final String ROWID = "_id";

    private static final String PRIMARY_KEY = "INTEGER PRIMARY KEY";

    private static final ClassName CURSORS = ClassName.get("droidkit.util", "Cursors");

    private static final List<TypeConversion> CONVERSIONS = Arrays.asList(
            new LongConversion(),
            new IntConversion(),
            new ShortConversion(),
            new DoubleConversion(),
            new FloatConversion(),
            new BooleanConversion(),
            new StringConversion(),
            new BigIntegerConversion(),
            new BigDecimalConversion(),
            new EnumConversion(),
            new DateTimeConversion(),
            new BlobConversion()
    );

    private static final List<ConflictResolution> CONFLICT_RESOLUTIONS = Arrays.asList(
            new OnConflictNone(),
            new OnConflictRollback(),
            new OnConflictAbort(),
            new OnConflictFail(),
            new OnConflictIgnore(),
            new OnConflictReplace()
    );

    private static final ConflictResolution DEFAULT_RESOLUTION = new OnConflictReplace();

    private final Map<Class<? extends Annotation>, Visitor> mVisitors = new LinkedHashMap<>();

    private final Map<String, String> mFields = new LinkedHashMap<>();

    private final Map<String, String> mColumns = new LinkedHashMap<>();

    private final Map<String, String> mSetters = new HashMap<>();

    private final CodeBlock.Builder mInitBlock = CodeBlock.builder();

    private final Trees mTrees;

    private TypeElement mOriginElement;

    private String mTableName;

    public SQLiteObjectVisitor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        mTrees = Trees.instance(processingEnv);
        mVisitors.put(SQLitePk.class, new SQLitePkVisitor());
        mVisitors.put(SQLiteColumn.class, new SQLiteColumnVisitor());
        mColumns.put(ROWID, PRIMARY_KEY + DEFAULT_RESOLUTION.call());
    }

    static void brewSchema(ProcessingEnvironment processingEnv) {

    }

    @Override
    public Void visitType(TypeElement element, Void aVoid) {
        if (mOriginElement == null) {
            mOriginElement = element;
            final SQLiteObject annotation = element.getAnnotation(SQLiteObject.class);
            mTableName = annotation.value();
        }
        return super.visitType(element, aVoid);
    }

    @Override
    public Void visitVariable(VariableElement field, Void aVoid) {
        for (final Map.Entry<Class<? extends Annotation>, Visitor> entry : mVisitors.entrySet()) {
            final Annotation annotation = field.getAnnotation(entry.getKey());
            if (annotation != null) {
                entry.getValue().call(this, field, annotation);
            }
        }
        return super.visitVariable(field, aVoid);
    }

    @Override
    void brewJava() {
        if (ElementKind.PACKAGE == mOriginElement.getEnclosingElement().getKind()) {
            printMessage(Diagnostic.Kind.NOTE, "%s", mColumns.keySet());
            final TypeSpec typeSpec = TypeSpec.classBuilder(mOriginElement.getSimpleName() + "$SQLite")
                    .addModifiers(Modifier.PUBLIC)
                    .addOriginatingElement(mOriginElement)
                    .addField(table())
                    .addMethod(instantiate())
                    .build();
            printMessage(Diagnostic.Kind.NOTE, "%s", typeSpec);
            final JavaFile javaFile = JavaFile.builder(mOriginElement.getEnclosingElement().toString(), typeSpec)
                    .addFileComment(AUTO_GENERATED_FILE)
                    .build();
            try {
                final JavaFileObject sourceFile = createSourceFile(javaFile.packageName + "." + typeSpec.name,
                        mOriginElement);
                try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                    javaFile.writeTo(writer);
                }
            } catch (IOException e) {
                printMessage(Diagnostic.Kind.ERROR, mOriginElement, e.getMessage());
                Logger.getGlobal().throwing("SQLiteObjectVisitor", "brewJava", e);
            }
        }
    }

    private void addColumn(String fieldName, String columnName, String columnDef) {
        mColumns.put(columnName, columnDef);
        mFields.put(fieldName, columnName);
    }

    private void addSetter(String fieldName, String setterName) {
        mSetters.put(setterName, fieldName);
    }

    private void addInitStatement(CodeBlock block) {
        mInitBlock.add(block);
    }

    private FieldSpec table() {
        return FieldSpec.builder(String.class, "TABLE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", mTableName)
                .build();
    }

    private MethodSpec instantiate() {
        return MethodSpec.methodBuilder("instantiate")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
                .returns(ClassName.get(mOriginElement))
                .addStatement("final $1T object = new $1T()", ClassName.get(mOriginElement))
                .addCode(mInitBlock.build())
                .addStatement("return object")
                .build();
    }

    private interface ConflictResolution extends Func0<String> {

    }

    private interface TypeConversion extends Func2<SQLiteObjectVisitor, VariableElement, Boolean>,
            Action4<SQLiteObjectVisitor, String, String, ConflictResolution> {
    }

    //region type conversions
    private static class LongConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return TypeKind.LONG == field.asType().getKind();
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " INTEGER" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getLong(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class IntConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return TypeKind.INT == field.asType().getKind();
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " INTEGER" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getInt(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class ShortConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return TypeKind.SHORT == field.asType().getKind();
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " INTEGER" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getShort(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class DoubleConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return TypeKind.DOUBLE == field.asType().getKind();
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " REAL" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getDouble(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class FloatConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return TypeKind.FLOAT == field.asType().getKind();
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " REAL" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getFloat(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class BooleanConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return TypeKind.BOOLEAN == field.asType().getKind();
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " INTEGER" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getBoolean(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class StringConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return visitor.isSubtype(field.asType(), String.class);
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " TEXT" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getString(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class BigIntegerConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return visitor.isSubtype(field.asType(), BigInteger.class);
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " INTEGER" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getBigInt(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class BigDecimalConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return visitor.isSubtype(field.asType(), BigDecimal.class);
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " REAL" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getBigDec(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class EnumConversion implements TypeConversion {

        private ClassName mEnumType;

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            if (visitor.isTypeOfKind(ElementKind.ENUM, field.asType())) {
                mEnumType = ClassName.get((TypeElement) visitor.asElement(field.asType()));
                return true;
            }
            return false;
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " TEXT" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getEnum(cursor, $S, $T.class)",
                            fieldName, CURSORS, columnName, mEnumType)
                    .build());
        }

    }

    private static class DateTimeConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return visitor.isSubtype(field.asType(), "org.joda.time.DateTime");
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " INTEGER" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getDateTime(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }

    private static class BlobConversion implements TypeConversion {

        @Override
        public Boolean call(SQLiteObjectVisitor visitor, VariableElement field) {
            return TypeKind.ARRAY == field.asType().getKind()
                    && "byte[]".equals(field.asType().toString());
        }

        @Override
        public void call(SQLiteObjectVisitor visitor, String fieldName, String columnName,
                         ConflictResolution conflictResolution) {
            visitor.addColumn(fieldName, columnName, " BLOB" + conflictResolution.call());
            visitor.addInitStatement(CodeBlock.builder()
                    .addStatement("object.$L = $T.getBlob(cursor, $S)", fieldName, CURSORS, columnName)
                    .build());
        }

    }
    //endregion

    //region field visitors
    private static class Visitor implements Action3<SQLiteObjectVisitor, VariableElement, Annotation> {
        @Override
        public void call(SQLiteObjectVisitor visitor, VariableElement field, Annotation annotation) {
            ((JCTree.JCVariableDecl) visitor.mTrees.getTree(field)).mods.flags &= ~Flags.PRIVATE;
        }

        protected String getColumnName(String fieldName) {
            if (fieldName.startsWith("m")) {
                return Strings.toUnderScope(fieldName.substring(1));
            }
            return Strings.toUnderScope(fieldName);
        }

        protected String getSetterName(String fieldName) {
            if (fieldName.startsWith("m")) {
                return "set" + fieldName.substring(1);
            }
            return "set" + Strings.capitalize(fieldName);
        }
    }

    private static class SQLitePkVisitor extends Visitor {
        @Override
        public void call(SQLiteObjectVisitor visitor, VariableElement field, Annotation annotation) {
            super.call(visitor, field, annotation);
            if (TypeKind.LONG == field.asType().getKind()) {
                final SQLitePk column = (SQLitePk) annotation;
                final String fieldName = String.valueOf(field.getSimpleName());
                visitor.addSetter(fieldName, Strings.nonEmpty(column.setter(), getSetterName(fieldName)));
                visitor.addColumn(fieldName, ROWID, PRIMARY_KEY + CONFLICT_RESOLUTIONS.get(column.value()).call());
                visitor.addInitStatement(CodeBlock.builder()
                        .addStatement("object.$L = $T.getLong(cursor, $S)", fieldName, CURSORS, ROWID)
                        .build());
            } else {
                visitor.printMessage(Diagnostic.Kind.ERROR, field, "Unexpected primary key type (expected 'long')");
            }
        }
    }

    private static class SQLiteColumnVisitor extends Visitor {
        @Override
        public void call(SQLiteObjectVisitor visitor, VariableElement field, Annotation annotation) {
            super.call(visitor, field, annotation);
            final SQLiteColumn column = (SQLiteColumn) annotation;
            final String fieldName = String.valueOf(field.getSimpleName());
            visitor.addSetter(fieldName, Strings.nonEmpty(column.setter(), getSetterName(fieldName)));
            for (final TypeConversion conversion : CONVERSIONS) {
                if (conversion.call(visitor, field)) {
                    conversion.call(visitor, fieldName, getColumnName(Strings.nonEmpty(column.value(), fieldName)),
                            CONFLICT_RESOLUTIONS.get(0));
                    return;
                }
            }
            visitor.printMessage(Diagnostic.Kind.ERROR, field, "Unsupported type conversion");
        }
    }
    //endregion

    //region conflict resolutions
    private static class OnConflictNone implements ConflictResolution {
        @Override
        public String call() {
            return "";
        }
    }

    private static class OnConflictRollback implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT ROLLBACK";
        }
    }

    private static class OnConflictAbort implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT ABORT";
        }
    }

    private static class OnConflictFail implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT FAIL";
        }
    }

    private static class OnConflictIgnore implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT IGNORE";
        }
    }

    private static class OnConflictReplace implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT REPLACE";
        }
    }
    //endregion

}
