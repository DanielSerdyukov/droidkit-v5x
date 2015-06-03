package droidkit.annotation.internal;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
class SQLiteGen implements JavaClassMaker {

    private final CodeBlock.Builder mCodeBlock = CodeBlock.builder();

    public void bindTableClass(String fqcn) {
        mCodeBlock.addStatement("SQLite.TABLES.put($L.class, $L._TABLE_)", fqcn, fqcn);
    }

    public void createTable(String fqcn, List<String> columnsDefs) {
        final StringBuilder query = new StringBuilder(" (");
        final Iterator<String> iterator = columnsDefs.iterator();
        while (iterator.hasNext()) {
            query.append(iterator.next());
            if (iterator.hasNext()) {
                query.append(", ");
            }
        }
        query.append(");");
        mCodeBlock.addStatement("SQLite.CREATE.add($S + $L._TABLE_ + $S)",
                "CREATE TABLE IF NOT EXISTS ", fqcn, query.toString());
    }

    @Override
    public void makeJavaFile() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("SQLite$Gen")
                .addModifiers(Modifier.PUBLIC)
                .addStaticBlock(mCodeBlock.build());
        final TypeSpec spec = builder.build();
        final JavaFile javaFile = JavaFile.builder("droidkit.sqlite", spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JCUtils.ENV.getFiler()
                .createSourceFile(javaFile.packageName + "." + spec.name);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
    }

}
