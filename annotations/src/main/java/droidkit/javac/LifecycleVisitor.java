package droidkit.javac;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementScanner7;
import javax.tools.JavaFileObject;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;
import rx.functions.Action3;

/**
 * @author Daniel Serdyukov
 */
abstract class LifecycleVisitor extends ElementScanner7<Void, Void> {

    static final ClassName ANDROID_VIEW = ClassName.get("android.view", "View");

    static final ClassName ANDROID_BUNDLE = ClassName.get("android.os", "Bundle");

    static final ClassName DROIDKIT_VIEWS = ClassName.get("droidkit.view", "Views");

    private static final Map<Class<? extends Annotation>, FieldInjector> FIELD_INJECTORS = new HashMap<>();

    private static final Map<Class<? extends Annotation>, MethodInjector> METHOD_INJECTORS = new HashMap<>();

    static {
        FIELD_INJECTORS.put(InjectView.class, new ViewInjector());
        METHOD_INJECTORS.put(OnClick.class, new OnClickInjector());
        METHOD_INJECTORS.put(OnActionClick.class, new OnActionClickInjector());
    }

    private final ProcessingEnvironment mProcessingEnv;

    private final TypeElement mElement;

    private final Trees mTrees;

    private final JCTypes mTypes;

    private final String mPackageName;

    LifecycleVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
        mProcessingEnv = processingEnv;
        mElement = element;
        mTrees = Trees.instance(processingEnv);
        mTypes = JCTypes.instance((JavacProcessingEnvironment) processingEnv);
        mPackageName = element.getEnclosingElement().toString();
    }

    @Override
    public Void visitVariable(VariableElement e, Void aVoid) {
        for (final Map.Entry<Class<? extends Annotation>, FieldInjector> entry : FIELD_INJECTORS.entrySet()) {
            final Annotation annotation = e.getAnnotation(entry.getKey());
            if (annotation != null) {
                ((JCTree.JCVariableDecl) mTrees.getTree(e)).mods.flags &= ~Flags.PRIVATE;
                entry.getValue().call(this, e, annotation);
            }
        }
        return super.visitVariable(e, aVoid);
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Void aVoid) {
        for (final Map.Entry<Class<? extends Annotation>, MethodInjector> entry : METHOD_INJECTORS.entrySet()) {
            final Annotation annotation = e.getAnnotation(entry.getKey());
            if (annotation != null) {
                ((JCTree.JCMethodDecl) mTrees.getTree(e)).mods.flags &= ~Flags.PRIVATE;
                entry.getValue().call(this, e, annotation);
            }
        }
        return super.visitExecutable(e, aVoid);
    }

    void brewJavaClass() {
        try {
            final TypeSpec typeSpec = TypeSpec.classBuilder(mElement.getSimpleName() + "$Proxy")
                    .addModifiers(Modifier.ABSTRACT)
                    .superclass(ClassName.get(mElement.getSuperclass()))
                    .addOriginatingElement(mElement)
                    .addFields(fields())
                    .addMethods(methods())
                    .build();
            final JavaFile javaFile = JavaFile.builder(mPackageName, typeSpec)
                    .addFileComment(Utils.AUTO_GENERATED_FILE)
                    .build();
            final JavaFileObject sourceFile = mProcessingEnv.getFiler().createSourceFile(
                    javaFile.packageName + "." + typeSpec.name, mElement);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
            ((JCTree.JCClassDecl) mTrees.getTree(mElement)).extending =
                    mTypes.ident(Arrays.asList(mPackageName, typeSpec.name));
        } catch (IOException e) {
            Utils.error(mProcessingEnv, mElement, e.getMessage());
        }

    }

    protected abstract void injectView(VariableElement field, int viewId);

    protected abstract void injectOnClick(ExecutableElement method, int[] viewIds);

    protected List<MethodSpec> injectOnActionClick(ExecutableElement method, int[] viewIds) {
        final List<MethodSpec> methods = new ArrayList<>(viewIds.length);
        for (final int viewId : viewIds) {
            methods.add(MethodSpec.methodBuilder("setupOnActionClickBy" + viewId)
                    .addCode(putOnActionClickListener(method, viewId))
                    .build());
        }
        return methods;
    }

    protected List<FieldSpec> fields() {
        return Arrays.asList(
                FieldSpec.builder(ParameterizedTypeName.get(
                                ClassName.get("android.support.v4.util", "SimpleArrayMap"),
                                ANDROID_VIEW, ClassName.get("android.view", "View", "OnClickListener")),
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

    protected List<MethodSpec> methods() {
        return Collections.emptyList();
    }

    protected MethodSpec onOptionsItemSelected() {
        return MethodSpec.methodBuilder("onOptionsItemSelected")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
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

    protected CodeBlock putOnClickListener(ExecutableElement method) {
        final CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("final $1T origin = ($1T) this", ClassName.get(mElement));
        codeBlock.add("mOnClick.put(view, new $T() {\n", ClassName.get("android.view", "View", "OnClickListener"));
        codeBlock.indent();
        codeBlock.add("@Override\n");
        codeBlock.add("public void onClick($T clickedView) {\n", ANDROID_VIEW);
        codeBlock.indent();
        final TypeKind returnType = method.getReturnType().getKind();
        Utils.checkArgument(TypeKind.VOID == returnType, mProcessingEnv, method,
                "Unexpected return type (expected=VOID, actual=%s)", returnType);
        final List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            codeBlock.addStatement("origin.$L()", method.getSimpleName());
        } else if (parameters.size() == 1) {
            final VariableElement parameter = parameters.get(0);
            if (Utils.isSubtype(mProcessingEnv, parameter, "android.view.View")) {
                codeBlock.addStatement("origin.$L(clickedView)", method.getSimpleName());
            } else {
                Utils.error(mProcessingEnv, method, "Unexpected parameter type (expected=View, actual=%s)",
                        parameter.asType());
            }
        } else {
            Utils.error(mProcessingEnv, method, "Unexpected parameters count (expected=[0, 1], actual=%d)",
                    parameters.size());
        }
        codeBlock.unindent();
        codeBlock.add("}\n");
        codeBlock.unindent();
        codeBlock.add("});\n");
        return codeBlock.build();
    }

    protected CodeBlock putOnActionClickListener(ExecutableElement method, int actionId) {
        final CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("final $1T origin = ($1T) this", ClassName.get(mElement));
        codeBlock.add("mOnActionClick.put($L, new $T() {\n", actionId,
                ClassName.get("android.view", "MenuItem", "OnMenuItemClickListener"));
        codeBlock.indent();
        codeBlock.add("@Override\n");
        codeBlock.add("public boolean onMenuItemClick($T menuItem) {\n", ClassName.get("android.view", "MenuItem"));
        codeBlock.indent();
        final TypeKind returnType = method.getReturnType().getKind();
        final List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            if (TypeKind.VOID == returnType) {
                codeBlock.addStatement("origin.$L()", method.getSimpleName());
                codeBlock.addStatement("return true");
            } else if (TypeKind.BOOLEAN == returnType) {
                codeBlock.addStatement("return origin.$L()", method.getSimpleName());
            } else {
                Utils.error(mProcessingEnv, method, "Unexpected return type (expected=[VOID | BOOLEAN], actual=%s)",
                        returnType);
            }
        } else if (parameters.size() == 1) {
            final VariableElement parameter = parameters.get(0);
            if (Utils.isSubtype(mProcessingEnv, parameter, "android.view.MenuItem")) {
                if (TypeKind.VOID == returnType) {
                    codeBlock.addStatement("origin.$L(menuItem)", method.getSimpleName());
                    codeBlock.addStatement("return true");
                } else if (TypeKind.BOOLEAN == returnType) {
                    codeBlock.addStatement("return origin.$L(menuItem)", method.getSimpleName());
                } else {
                    Utils.error(mProcessingEnv, method, "Unexpected return type (expected=[VOID | BOOLEAN], actual=%s)",
                            returnType);
                }
            } else {
                Utils.error(mProcessingEnv, method, "Unexpected parameter type (expected=MenuItem, actual=%s)",
                        parameter.asType());
            }
        } else {
            Utils.error(mProcessingEnv, method, "Unexpected parameters count (expected=[0, 1], actual=%d)",
                    parameters.size());
        }
        codeBlock.unindent();
        codeBlock.add("}\n");
        codeBlock.unindent();
        codeBlock.add("});\n");
        return codeBlock.build();
    }

    //region Injectors
    private interface FieldInjector extends Action3<LifecycleVisitor, VariableElement, Annotation> {

    }

    private interface MethodInjector extends Action3<LifecycleVisitor, ExecutableElement, Annotation> {

    }

    private static class ViewInjector implements FieldInjector {

        @Override
        public void call(LifecycleVisitor visitor, VariableElement field, Annotation annotation) {
            visitor.injectView(field, ((InjectView) annotation).value());
        }

    }

    private static class OnClickInjector implements MethodInjector {

        @Override
        public void call(LifecycleVisitor visitor, ExecutableElement method, Annotation annotation) {
            visitor.injectOnClick(method, ((OnClick) annotation).value());
        }
    }

    private static class OnActionClickInjector implements MethodInjector {

        @Override
        public void call(LifecycleVisitor visitor, ExecutableElement method, Annotation annotation) {
            visitor.injectOnActionClick(method, ((OnActionClick) annotation).value());
        }

    }
    //endregion

}
