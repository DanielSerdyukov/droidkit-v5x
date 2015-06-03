package droidkit.annotation.internal;

import com.squareup.javapoet.*;
import droidkit.annotation.OnActionClick;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
class OnActionClickInjector {

    private static final ClassName LISTENER = ClassName.get("android.view", "MenuItem", "OnMenuItemClickListener");

    private final List<MethodSpec> mEmitters = new ArrayList<>();

    private final TypeName mTargetType;

    OnActionClickInjector(TypeName targetType) {
        mTargetType = targetType;
    }

    void tryInject(ExecutableElement element, OnActionClick onActionClick) {
        if (onActionClick != null) {
            final int[] viewIds = onActionClick.value();
            for (final int viewId : viewIds) {
                final CodeBlock.Builder codeBlock = CodeBlock.builder()
                        .add("mOnActionClick.put($L, new $T() {\n", viewId, LISTENER)
                        .indent()
                        .add("@Override\n")
                        .add("public boolean onMenuItemClick($T menuItem) {\n",
                                ClassName.get("android.view", "MenuItem"))
                        .indent();
                final TypeKind kind = element.getReturnType().getKind();
                if (TypeKind.VOID != kind && TypeKind.BOOLEAN != kind) {
                    JCUtils.error("Unexpected method signature", element);
                }
                final List<? extends VariableElement> params = element.getParameters();
                if (params.isEmpty()) {
                    if (TypeKind.VOID == kind) {
                        codeBlock.addStatement("target.$L()", element.getSimpleName());
                        codeBlock.addStatement("return true");
                    } else {
                        codeBlock.addStatement("return target.$L()", element.getSimpleName());
                    }
                } else if (params.size() == 1 && JCUtils.isSubtype(params.get(0), "android.view.MenuItem")) {
                    if (TypeKind.VOID == kind) {
                        codeBlock.addStatement("target.$L(menuItem)", element.getSimpleName());
                        codeBlock.addStatement("return true");
                    } else {
                        codeBlock.addStatement("return target.$L(menuItem)", element.getSimpleName());
                    }
                } else {
                    JCUtils.error("Unexpected method signature", element);
                }
                codeBlock.unindent()
                        .add("}\n")
                        .unindent()
                        .add("});\n");
                mEmitters.add(MethodSpec.methodBuilder("setupOnActionClickListener" + viewId)
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(mTargetType, "target", Modifier.FINAL)
                        .addCode(codeBlock.build())
                        .build());
            }
        }
    }

    Iterable<FieldSpec> fields() {
        final ClassName sparseArray = ClassName.get("android.util", "SparseArray");
        return Collections.singletonList(
                FieldSpec.builder(ParameterizedTypeName.get(sparseArray, LISTENER), "mOnActionClick")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", sparseArray)
                        .build()
        );
    }

    CodeBlock resumeBlock() {
        return CodeBlock.builder().build();
    }

    CodeBlock pauseBlock() {
        return CodeBlock.builder().build();
    }

    CodeBlock destroyBlock() {
        return CodeBlock.builder().addStatement("mOnActionClick.clear()").build();
    }

    CodeBlock handleClick(String itemVar) {
        return CodeBlock.builder()
                .addStatement("final $T listener = mOnActionClick.get($L.getItemId())", LISTENER, itemVar)
                .beginControlFlow("if (listener != null)")
                .addStatement("return listener.onMenuItemClick($L)", itemVar)
                .endControlFlow()
                .build();
    }

    Iterable<MethodSpec> setupMethods() {
        return mEmitters;
    }

}
