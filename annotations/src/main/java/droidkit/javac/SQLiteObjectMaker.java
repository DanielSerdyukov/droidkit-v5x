package droidkit.javac;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Type;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import droidkit.annotation.SQLiteObject;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectMaker {

    private static final String ROW_ID = "_id";

    private static final CodeBlock.Builder SCHEMA = CodeBlock.builder();

    private static final ClassName DB_UTILS = ClassName.get("droidkit.database", "DatabaseUtils");

    private static final Set<TypeConversion> TYPE_CONVERSIONS = ImmutableSet.of(
            new IntConversion(),
            new LongConversion(),
            new DoubleConversion(),
            new StringConversion(),
            new DateTimeConversion(),
            new FloatConversion(),
            new EnumConversion(),
            new BigIntegerConversion(),
            new BigDecimalConversion(),
            new ByteArrayConversion(),
            new ShortConversion()
    );

    private static final String[] CONFLICT_VALUES = new String[]{
            "",
            " ON CONFLICT ROLLBACK",
            " ON CONFLICT ABORT",
            " ON CONFLICT FAIL",
            " ON CONFLICT IGNORE",
            " ON CONFLICT REPLACE"
    };

    private final List<String> mColumnsDef = new ArrayList<>();

    private final Map<String, String> mColumnsToFields = new LinkedHashMap<>();

    private final ProcessingEnvironment mProcessingEnv;

    private final TypeElement mElement;

    private final String mPackageName;

    private final CodeBlock.Builder mInstantiateBlock = CodeBlock.builder();

    private final String mTableName;

    private String mPrimaryKey;

    SQLiteObjectMaker(ProcessingEnvironment processingEnv, TypeElement element) {
        mProcessingEnv = processingEnv;
        mElement = element;
        mPackageName = mElement.getEnclosingElement().toString();
        mTableName = element.getAnnotation(SQLiteObject.class).value();
    }

    static void brewSchemaClass(ProcessingEnvironment processingEnv) {
        try {
            final TypeSpec typeSpec = TypeSpec.classBuilder("SQLiteSchema")
                    .addModifiers(Modifier.PUBLIC)
                    .addStaticBlock(SCHEMA.build())
                    .build();
            final JavaFile javaFile = JavaFile.builder("droidkit.sqlite", typeSpec)
                    .addFileComment("AUTO-GENERATED FILE. DO NOT MODIFY.")
                    .build();
            final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                    javaFile.packageName + "." + typeSpec.name);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            Utils.error(processingEnv, e.getMessage());
        }
    }

    void setPrimaryKey(String fieldName, Type type, int conflictClause) {
        Utils.checkArgument(TypeKind.LONG == type.getKind(), mProcessingEnv, mElement,
                "@SQLitePk %s must be 'long'", fieldName);
        Utils.checkArgument((0 <= conflictClause && conflictClause < CONFLICT_VALUES.length), mProcessingEnv, mElement,
                "@SQLitePk(value) %s must be in [%d, %d)", fieldName, 0, CONFLICT_VALUES.length);
        mPrimaryKey = fieldName;
        mColumnsDef.add("_id INTEGER PRIMARY KEY" + CONFLICT_VALUES[conflictClause]);
        mInstantiateBlock.addStatement("object.$L = $T.getLong(cursor, $S)", mPrimaryKey, DB_UTILS, ROW_ID);
        mColumnsToFields.put(ROW_ID, fieldName);
    }

    void addColumn(String fieldName, Type type, String columnName) {
        for (final TypeConversion conversion : TYPE_CONVERSIONS) {
            if (conversion.isAcceptable(mProcessingEnv, type)) {
                conversion.accept(mColumnsDef, mInstantiateBlock, fieldName, columnName, type);
                mColumnsToFields.put(columnName, fieldName);
                return;
            }
        }
        Utils.error(mProcessingEnv, mElement, "%s: Invalid %s -> sqlite type conversion.", fieldName,
                type.asElement().asType());
    }

    void brewJavaClass() {
        try {
            final TypeSpec typeSpec = TypeSpec.classBuilder(getClassName())
                    .addModifiers(Modifier.PUBLIC)
                    .addOriginatingElement(mElement)
                    .addFields(fields())
                    .addMethods(methods())
                    .build();
            final JavaFile javaFile = JavaFile.builder(mPackageName, typeSpec)
                    .addFileComment("AUTO-GENERATED FILE. DO NOT MODIFY.")
                    .build();
            final JavaFileObject sourceFile = mProcessingEnv.getFiler().createSourceFile(
                    javaFile.packageName + "." + typeSpec.name, mElement);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            Utils.error(mProcessingEnv, mElement, e.getMessage());
        }
        SCHEMA.addStatement("SQLiteProvider.SCHEMA.putIfAbsent($S, \"($L)\")", mTableName,
                Joiner.on(", ").join(mColumnsDef));
    }

    String getPackageName() {
        return mPackageName;
    }

    String getClassName() {
        return mElement.getSimpleName() + "$SQLite";
    }

    String getPrimaryKey() {
        return mPrimaryKey;
    }

    private List<FieldSpec> fields() {
        return ImmutableList.of(
                FieldSpec.builder(String.class, "TABLE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", mTableName)
                        .build()
        );
    }

    private List<MethodSpec> methods() {
        return ImmutableList.of(instantiate(), insert(), update());
    }

    private MethodSpec instantiate() {
        return MethodSpec.methodBuilder("instantiate")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(mElement))
                .addParameter(ClassName.get("droidkit.sqlite", "SQLiteClient"), "client")
                .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
                .addStatement("final $1T object = new $1T()", ClassName.get(mElement))
                .addCode(mInstantiateBlock.build())
                .addStatement("object.mClientRef = new $T<>(client)", ClassName.get(WeakReference.class))
                .addStatement("return object")
                .build();
    }

    private MethodSpec insert() {
        final Joiner joiner = Joiner.on(", ");
        return MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.LONG)
                .addParameter(ClassName.get("droidkit.sqlite", "SQLiteClient"), "client")
                .addParameter(ClassName.get(mElement), "object")
                .beginControlFlow("if (object.$L > 0)", mPrimaryKey)
                .addStatement("client.executeInsert(\"INSERT INTO $L($L) VALUES($L);\", $L)",
                        mTableName, joiner.join(mColumnsToFields.keySet()),
                        joiner.join(Collections.nCopies(mColumnsToFields.size(), "?")),
                        joiner.join(Iterables.transform(mColumnsToFields.values(), new FieldFunction())))
                .nextControlFlow("else")
                .addStatement("object.$L = client.executeInsert(\"INSERT INTO $L($L) VALUES($L);\", $L)",
                        mPrimaryKey, mTableName, joiner.join(Utils.slice(mColumnsToFields.keySet(), 1)),
                        joiner.join(Collections.nCopies(mColumnsToFields.size() - 1, "?")),
                        joiner.join(Iterables.transform(Utils.slice(mColumnsToFields.values(), 1),
                                new FieldFunction())))
                .endControlFlow()
                .addStatement("object.mClientRef = new $T<>(client)", ClassName.get(WeakReference.class))
                .addStatement("return object.$L", mPrimaryKey)
                .build();
    }

    private MethodSpec update() {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.INT)
                .addParameter(ParameterizedTypeName.get(
                        ClassName.get(Reference.class),
                        ClassName.get("droidkit.sqlite", "SQLiteClient")
                ), "clientRef")
                .addParameter(String.class, "column")
                .addParameter(Object.class, "value")
                .addParameter(TypeName.LONG, "id")
                .beginControlFlow("if (clientRef != null)")
                .addStatement("SQLiteClient client = clientRef.get()")
                .beginControlFlow("if (client != null)")
                .addStatement("final int affectedRows = client.executeUpdateDelete(\"UPDATE $L SET \" + " +
                        "column + \" = ? WHERE _id = ?;\", $L, $L)", mTableName, "value", "id")
                .beginControlFlow("if (affectedRows > 0)")
                .addStatement("$T.notifyChange($T.class)", ClassName.get("droidkit.sqlite", "SQLite"),
                        ClassName.get(mElement))
                .endControlFlow()
                .addStatement("return affectedRows")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return 0")
                .build();
    }

    //region Java <-> SQLite type conversions
    private interface TypeConversion {

        boolean isAcceptable(ProcessingEnvironment processingEnv, Type type);

        void accept(List<String> columns, CodeBlock.Builder instantiate,
                    String fieldName, String columnName, Type type);

    }

    private static class IntConversion implements TypeConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return TypeKind.INT == type.getKind();
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " INTEGER");
            instantiate.addStatement("object.$L = $T.getInt(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class LongConversion extends IntConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return TypeKind.LONG == type.getKind();
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " INTEGER");
            instantiate.addStatement("object.$L = $T.getLong(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class DoubleConversion extends IntConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return TypeKind.DOUBLE == type.getKind();
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " REAL");
            instantiate.addStatement("object.$L = $T.getDouble(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class StringConversion implements TypeConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return Utils.isSubtype(processingEnv, type, String.class);
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " TEXT");
            instantiate.addStatement("object.$L = $T.getString(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class DateTimeConversion implements TypeConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            final TypeElement jodaTime = processingEnv.getElementUtils().getTypeElement("org.joda.time.DateTime");
            return jodaTime != null && processingEnv.getTypeUtils().isSubtype(type, jodaTime.asType());
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " INTEGER");
            instantiate.addStatement("object.$L = $T.getDateTime(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class EnumConversion implements TypeConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return ElementKind.ENUM == type.asElement().getKind();
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " TEXT");
            instantiate.addStatement("object.$L = $T.getEnum(cursor, $S, $T.class)", fieldName, DB_UTILS, columnName,
                    ClassName.get(type));
        }

    }

    private static class BigIntegerConversion implements TypeConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return Utils.isSubtype(processingEnv, type, BigInteger.class);
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " INTEGER");
            instantiate.addStatement("object.$L = $T.getBigInt(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class BigDecimalConversion implements TypeConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return Utils.isSubtype(processingEnv, type, BigDecimal.class);
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " REAL");
            instantiate.addStatement("object.$L = $T.getBigDec(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class FloatConversion extends IntConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return TypeKind.FLOAT == type.getKind();
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " REAL");
            instantiate.addStatement("object.$L = $T.getFloat(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class ByteArrayConversion implements TypeConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return TypeKind.ARRAY == type.getKind() && "byte[]".equals(type.toString());
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " BLOB");
            instantiate.addStatement("object.$L = $T.getBlob(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }

    private static class ShortConversion extends IntConversion {

        @Override
        public boolean isAcceptable(ProcessingEnvironment processingEnv, Type type) {
            return TypeKind.SHORT == type.getKind();
        }

        @Override
        public void accept(List<String> columns, CodeBlock.Builder instantiate,
                           String fieldName, String columnName, Type type) {
            columns.add(columnName + " INTEGER");
            instantiate.addStatement("object.$L = $T.getShort(cursor, $S)", fieldName, DB_UTILS, columnName);
        }

    }
    //endregion

    private static class FieldFunction implements Function<String, String> {
        @Override
        public String apply(String fieldName) {
            return "object." + fieldName;
        }
    }

}
