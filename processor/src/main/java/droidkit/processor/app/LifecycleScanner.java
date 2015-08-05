package droidkit.processor.app;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import droidkit.processor.ElementScanner;
import droidkit.processor.ProcessingEnv;
import droidkit.processor.ViewInjector;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
abstract class LifecycleScanner extends ElementScanner {

    private final ViewInjector mViewInjector = new ViewInjector();

    private final List<MethodSpec> mOnClick = new ArrayList<>();

    private final List<MethodSpec> mOnActionClick = new ArrayList<>();

    private final TreeMaker mTreeMaker;

    private final Names mNames;

    private TypeElement mOriginType;

    LifecycleScanner(ProcessingEnv env) {
        super(env);
        mTreeMaker = TreeMaker.instance(env.getJavacEnv().getContext());
        mNames = Names.instance(env.getJavacEnv().getContext());
    }

    @Override
    public Void visitType(TypeElement e, Void aVoid) {
        if (mOriginType == null) {
            mOriginType = e;
        }
        return super.visitType(e, aVoid);
    }

    @Override
    public Void visitVariable(VariableElement field, Void aVoid) {
        for (final FieldVisitor visitor : FieldVisitor.SUPPORTED) {
            final Annotation annotation = visitor.getAnnotation(getEnv(), field);
            if (annotation != null) {
                getEnv().<JCTree.JCVariableDecl>getTree(field).mods.flags &= ~Flags.PRIVATE;
                visitor.visit(this, field, annotation);
            }
        }
        return super.visitVariable(field, aVoid);
    }

    @Override
    public Void visitExecutable(ExecutableElement method, Void aVoid) {
        for (final MethodVisitor visitor : MethodVisitor.SUPPORTED) {
            final Annotation annotation = visitor.getAnnotation(getEnv(), method);
            if (annotation != null) {
                getEnv().<JCTree.JCMethodDecl>getTree(method).mods.flags &= ~Flags.PRIVATE;
                visitor.visit(this, method, annotation);
            }
        }
        return super.visitExecutable(method, aVoid);
    }

    @Override
    public void visitEnd() {
        final ClassName injector = mViewInjector.brewJava(getEnv(), mOriginType);
        final TypeSpec typeSpec = TypeSpec.classBuilder(mOriginType.getSimpleName() + "$Proxy")
                .superclass(ClassName.get(mOriginType.getSuperclass()))
                .addOriginatingElement(mOriginType)
                .addFields(fields())
                .addMethods(methods(mOriginType, injector))
                .addMethods(mOnClick)
                .addMethods(mOnActionClick)
                .build();
        final JavaFile javaFile = JavaFile.builder(mOriginType.getEnclosingElement().toString(), typeSpec)
                .addFileComment(AUTO_GENERATED_FILE)
                .build();
        System.out.println(javaFile);
        try {
            final JavaFileObject sourceFile = getEnv().createSourceFile(
                    javaFile.packageName + "." + typeSpec.name, mOriginType);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
            JCTree.JCExpression selector = mTreeMaker.Ident(mNames.fromString(javaFile.packageName));
            selector = mTreeMaker.Select(selector, mNames.fromString(typeSpec.name));
            getEnv().<JCTree.JCClassDecl>getTree(mOriginType).extending = selector;
        } catch (IOException e) {
            Logger.getGlobal().throwing(LifecycleScanner.class.getName(), "visitEnd", e);
        }
    }

    protected ViewInjector views() {
        return mViewInjector;
    }

    protected List<MethodSpec> onClick() {
        return mOnClick;
    }

    protected List<MethodSpec> onActionClick() {
        return mOnActionClick;
    }

    protected abstract Func1<Integer, CodeBlock> viewFinder();

    //region implementation
    protected List<FieldSpec> fields() {
        return Arrays.asList(
                FieldSpec.builder(ParameterizedTypeName.get(
                                ClassName.get("android.support.v4.util", "SimpleArrayMap"),
                                ClassName.get("android.view", "View"),
                                ClassName.get("android.view", "View", "OnClickListener")),
                        "mOnClick", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", ClassName.get("android.support.v4.util", "SimpleArrayMap"))
                        .build(),
                FieldSpec.builder(ParameterizedTypeName.get(
                                ClassName.get("android.util", "SparseArray"),
                                ClassName.get("android.view", "MenuItem", "OnMenuItemClickListener")),
                        "mOnActionClick", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", ClassName.get("android.util", "SparseArray"))
                        .build()
        );
    }

    protected List<MethodSpec> methods(TypeElement originType, ClassName viewInjector) {
        return Collections.emptyList();
    }

    protected MethodSpec onOptionsItemSelected(Modifier... modifiers) {
        return MethodSpec.methodBuilder("onOptionsItemSelected")
                .addAnnotation(Override.class)
                .addModifiers(modifiers)
                .returns(TypeName.BOOLEAN)
                .addParameter(ClassName.get("android.view", "MenuItem"), "menuItem")
                .addStatement("final $T listener = mOnActionClick.get(menuItem.getItemId())",
                        ClassName.get("android.view", "MenuItem", "OnMenuItemClickListener"))
                .beginControlFlow("if (listener != null)")
                .addStatement("return listener.onMenuItemClick(menuItem)")
                .endControlFlow()
                .addStatement("return super.onOptionsItemSelected(menuItem)")
                .build();
    }

    protected MethodSpec onResume(Modifier... modifiers) {
        return MethodSpec.methodBuilder("onResume")
                .addAnnotation(Override.class)
                .addModifiers(modifiers)
                .addStatement("super.onResume()")
                .beginControlFlow("for (int i = 0; i < mOnClick.size(); ++i)")
                .addStatement("mOnClick.keyAt(i).setOnClickListener(mOnClick.valueAt(i))")
                .endControlFlow()
                .build();
    }

    protected MethodSpec onPause(Modifier... modifiers) {
        return MethodSpec.methodBuilder("onPause")
                .addAnnotation(Override.class)
                .addModifiers(modifiers)
                .beginControlFlow("for (int i = 0; i < mOnClick.size(); ++i)")
                .addStatement("mOnClick.keyAt(i).setOnClickListener(null)")
                .endControlFlow()
                .addStatement("super.onPause()")
                .build();
    }

    protected MethodSpec onDestroy(Modifier... modifiers) {
        return MethodSpec.methodBuilder("onDestroy")
                .addAnnotation(Override.class)
                .addModifiers(modifiers)
                .addStatement("mOnClick.clear()")
                .addStatement("mOnActionClick.clear()")
                .addStatement("super.onDestroy()")
                .build();
    }
    //endregion

}
