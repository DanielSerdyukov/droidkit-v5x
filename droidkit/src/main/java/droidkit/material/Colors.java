package droidkit.material;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * @author Daniel Serdyukov
 */
public final class Colors {

    private Colors() {
    }

    @ColorInt
    public static int darker(@ColorInt int color, float factor) {
        final float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int alpha(@ColorInt int color, float factor) {
        return Color.argb(
                Math.round(Color.alpha(color) * factor),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

}
