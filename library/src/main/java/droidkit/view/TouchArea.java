package droidkit.view;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.TouchDelegate;
import android.view.View;

/**
 * @author Daniel Serdyukov
 */
public final class TouchArea {

    public static final int EXPAND_DIP = 16;

    private TouchArea() {
    }

    public static void expand(@NonNull final View view) {
        expand(view, EXPAND_DIP, EXPAND_DIP, EXPAND_DIP, EXPAND_DIP);
    }

    public static void expand(@NonNull final View view, int wh) {
        expand(view, wh, wh, wh, wh);
    }

    public static void expand(@NonNull final View view, int w, int h) {
        expand(view, w, h, w, h);
    }

    public static void expand(@NonNull final View view, final int start, final int top, final int end,
                              final int bottom) {
        final View parent = (View) view.getParent();
        if (parent != null) {
            final DisplayMetrics metrics = view.getResources().getDisplayMetrics();
            parent.post(new Runnable() {
                @Override
                public void run() {
                    parent.removeCallbacks(this);
                    final Rect hitRect = new Rect();
                    hitRect.left -= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, start, metrics);
                    hitRect.top -= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, top, metrics);
                    hitRect.right += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, end, metrics);
                    hitRect.bottom += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottom, metrics);
                    view.getHitRect(hitRect);
                    parent.setTouchDelegate(new TouchDelegate(hitRect, view));
                }
            });
        }
    }

}
