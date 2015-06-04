package droidkit.processor;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import droidkit.annotation.OnClick;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
class OnClickInjector {

    private static final ClassName VIEW = ClassName.get("android.view", "View");

    private static final ClassName LISTENER = ClassName.get("android.view", "View", "OnClickListener");

    private final List<MethodSpec> mEmitters = new ArrayList<>();

    private final TypeName mTargetType;

    private final TypeName mRootType;

    OnClickInjector(TypeName targetType, TypeName rootType) {
        mTargetType = targetType;
        mRootType = rootType;
    }

    void tryInject(ExecutableElement element, OnClick onClick) {
        if (onClick != null) {
            JCUtils.<JCTree.JCMethodDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            for (final int viewId : onClick.value()) {
                final CodeBlock.Builder codeBlock = CodeBlock.builder()
                        .add("mOnClick.put(view, new $T() {\n", LISTENER)
                        .indent()
                        .add("@Override\n")
                        .add("public void onClick($T clickedView) {\n", VIEW)
                        .indent();
                final List<? extends VariableElement> params = element.getParameters();
                if (params.isEmpty()) {
                    codeBlock.addStatement("target.$L()", element.getSimpleName());
                } else if (params.size() == 1 && JCUtils.isSubtype(params.get(0), "android.view.View")) {
                    codeBlock.addStatement("target.$L(clickedView)", element.getSimpleName());
                } else {
                    JCUtils.error("Unexpected method signature", element);
                }
                codeBlock.unindent()
                        .add("}\n")
                        .unindent()
                        .add("});\n");
                mEmitters.add(MethodSpec.methodBuilder("setupOnClickListener" + viewId)
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(mTargetType, "target", Modifier.FINAL)
                        .addParameter(mRootType, "root", Modifier.FINAL)
                        .addStatement("final View view = $T.findById(root, $L)", ViewInjector.DK_VIEWS, viewId)
                        .beginControlFlow("if (view != null)")
                        .addCode(codeBlock.build())
                        .endControlFlow()
                        .build());
            }
        }
    }

    Iterable<FieldSpec> fields() {
        final ClassName simpleArrayMap = ClassName.get("android.support.v4.util", "SimpleArrayMap");

        return Collections.singletonList(
                FieldSpec.builder(ParameterizedTypeName.get(simpleArrayMap, VIEW, LISTENER), "mOnClick")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", simpleArrayMap)
                        .build()
        );
    }

    CodeBlock resumeBlock() {
        return CodeBlock.builder()
                .beginControlFlow("for (int i = 0; i < mOnClick.size(); ++i)")
                .addStatement("mOnClick.keyAt(i).setOnClickListener(mOnClick.valueAt(i))")
                .endControlFlow()
                .build();
    }

    CodeBlock pauseBlock() {
        return CodeBlock.builder()
                .beginControlFlow("for (int i = 0; i < mOnClick.size(); ++i)")
                .addStatement("mOnClick.keyAt(i).setOnClickListener(null)")
                .endControlFlow()
                .build();
    }

    CodeBlock destroyBlock() {
        return CodeBlock.builder().addStatement("mOnClick.clear()").build();
    }

    Iterable<MethodSpec> setupMethods() {
        return mEmitters;
    }

}
