package droidkit.apt;

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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;

/**
 * @author Daniel Serdyukov
 */
class LifecycleApt implements Apt {

    static final ClassName VIEW = ClassName.get("android.view", "View");

    static final ClassName ON_CLICK_LISTENER = ClassName.get("android.view", "View", "OnClickListener");

    static final ClassName MENU_ITEM = ClassName.get("android.view", "MenuItem");

    static final ClassName ON_MENU_ITEM_CLICK_LISTENER = ClassName.get("android.view", "MenuItem",
            "OnMenuItemClickListener");

    static final ClassName DK_VIEWS = ClassName.get("droidkit.view", "Views");

    private final AtomicBoolean mProcessSingle = new AtomicBoolean();

    private final List<MethodSpec> mOnActionClick = new ArrayList<>();

    private final TypeElement mElement;

    public LifecycleApt(TypeElement element) {
        mElement = element;
    }

    @Override
    public void process(RoundEnvironment roundEnv) {
        if (mProcessSingle.compareAndSet(false, true)) {
            final List<? extends Element> elements = mElement.getEnclosedElements();
            for (final Element element : elements) {
                if (ElementKind.FIELD == element.getKind()) {
                    tryInjectView(element, element.getAnnotation(InjectView.class));
                } else if (ElementKind.METHOD == element.getKind()) {
                    tryInjectOnClick((ExecutableElement) element, element.getAnnotation(OnClick.class));
                    tryInjectOnActionClick((ExecutableElement) element, element.getAnnotation(OnActionClick.class));
                }
            }
        }
    }

    @Override
    public void finishProcessing() throws IOException {
        final TypeSpec spec = TypeSpec.classBuilder(mElement.getSimpleName() + "$Proxy")
                .superclass(TypeName.get(mElement.getSuperclass()))
                .addFields(fields())
                .addMethods(methods())
                .build();
        final JavaFile javaFile = JavaFile.builder(mElement.getEnclosingElement().toString(), spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JavacEnv.get().createSourceFile(javaFile, spec, mElement);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
        JavacEnv.get().<JCTree.JCClassDecl>getTree(mElement).extending = JCSelector.get(spec.name).getIdent();
    }

    protected void injectView(TypeElement clazz, Element view, int viewId) {

    }

    protected void injectOnClick(TypeElement clazz, ExecutableElement method, int viewId) {

    }

    protected MethodSpec onOptionsItemSelected() {
        return MethodSpec.methodBuilder("onOptionsItemSelected")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(MENU_ITEM, "item")
                .addStatement("final $T listener = mOnActionClick.get(item.getItemId())", ON_MENU_ITEM_CLICK_LISTENER)
                .beginControlFlow("if (listener != null)")
                .addStatement("return listener.onMenuItemClick(item)")
                .endControlFlow()
                .addStatement("return super.onOptionsItemSelected(item)")
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
                .addStatement("super.onDestroy()")
                .build();
    }

    protected Collection<FieldSpec> fields() {
        final ClassName simpleArrayMap = ClassName.get("android.support.v4.util", "SimpleArrayMap");
        final ClassName sparseArray = ClassName.get("android.util", "SparseArray");
        return Arrays.asList(
                FieldSpec.builder(ParameterizedTypeName.get(simpleArrayMap, VIEW, ON_CLICK_LISTENER), "mOnClick")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", simpleArrayMap)
                        .build(),
                FieldSpec.builder(ParameterizedTypeName.get(sparseArray, ON_MENU_ITEM_CLICK_LISTENER), "mOnActionClick")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", sparseArray)
                        .build()
        );
    }

    protected Collection<MethodSpec> methods() {
        return Collections.emptyList();
    }

    protected CodeBlock putNewOnClickListener(ExecutableElement method) {
        final CodeBlock.Builder codeBlock = CodeBlock.builder()
                .addStatement("final $T target = ($T) this", mElement, mElement)
                .add("mOnClick.put(view, new $T() {\n", ON_CLICK_LISTENER).indent()
                .add("@Override\n")
                .add("public void onClick($T clickedView) {\n", VIEW).indent();
        final List<? extends VariableElement> params = method.getParameters();
        if (params.isEmpty()) {
            codeBlock.addStatement("target.$L()", method.getSimpleName());
        } else if (params.size() == 1 && Utils.isSubtype(params.get(0), "android.view.View")) {
            codeBlock.addStatement("target.$L(clickedView)", method.getSimpleName());
        } else {
            JavacEnv.get().logE(method, "Unexpected method signature");
        }
        return codeBlock.unindent().add("}\n").unindent().add("});\n").build();
    }

    protected Collection<MethodSpec> setupOnActionClickMethods() {
        return Collections.unmodifiableList(mOnActionClick);
    }

    protected CodeBlock callSetupOnActionClickMethods() {
        final CodeBlock.Builder codeBlock = CodeBlock.builder();
        for (final MethodSpec method : mOnActionClick) {
            codeBlock.addStatement("$N()", method);
        }
        return codeBlock.build();
    }

    private void tryInjectView(Element element, InjectView injectView) {
        if (injectView != null) {
            JavacEnv.get().<JCTree.JCVariableDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            injectView(mElement, element, injectView.value());
        }
    }

    private void tryInjectOnClick(ExecutableElement element, OnClick onClick) {
        if (onClick != null) {
            JavacEnv.get().<JCTree.JCMethodDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            for (final int viewId : onClick.value()) {
                injectOnClick(mElement, element, viewId);
            }
        }
    }

    private void tryInjectOnActionClick(ExecutableElement element, OnActionClick onActionClick) {
        if (onActionClick != null) {
            JavacEnv.get().<JCTree.JCMethodDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            for (final int viewId : onActionClick.value()) {
                final CodeBlock.Builder codeBlock = CodeBlock.builder()
                        .addStatement("final $T target = ($T) this", mElement, mElement)
                        .add("mOnActionClick.put($L, new $T() {\n", viewId, ON_MENU_ITEM_CLICK_LISTENER).indent()
                        .add("@Override\n")
                        .add("public boolean onMenuItemClick($T menuItem) {\n", MENU_ITEM).indent();
                final TypeKind kind = element.getReturnType().getKind();
                if (TypeKind.VOID != kind && TypeKind.BOOLEAN != kind) {
                    JavacEnv.get().logE(element, "Unexpected method signature");
                }
                final List<? extends VariableElement> params = element.getParameters();
                if (params.isEmpty()) {
                    if (TypeKind.VOID == kind) {
                        codeBlock.addStatement("target.$L()", element.getSimpleName());
                        codeBlock.addStatement("return true");
                    } else {
                        codeBlock.addStatement("return target.$L()", element.getSimpleName());
                    }
                } else if (params.size() == 1 && Utils.isSubtype(params.get(0), "android.view.MenuItem")) {
                    if (TypeKind.VOID == kind) {
                        codeBlock.addStatement("target.$L(menuItem)", element.getSimpleName());
                        codeBlock.addStatement("return true");
                    } else {
                        codeBlock.addStatement("return target.$L(menuItem)", element.getSimpleName());
                    }
                } else {
                    JavacEnv.get().logE(element, "Unexpected method signature");
                }
                codeBlock.unindent().add("}\n").unindent().add("});\n");
                mOnActionClick.add(MethodSpec.methodBuilder("setupOnActionClickListener" + viewId)
                        .addModifiers(Modifier.PRIVATE)
                        .addCode(codeBlock.build())
                        .build());
            }
        }
    }

}
