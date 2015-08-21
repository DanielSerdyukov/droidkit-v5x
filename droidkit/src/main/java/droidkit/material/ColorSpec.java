package droidkit.material;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * @author Daniel Serdyukov
 */
public final class ColorSpec {

    public static final ColorSpec MATERIAL_DARK = new ColorSpec(Color.BLACK, 0.12f, 0.38f, 0.54f, 0.87f);

    public static final ColorSpec MATERIAL_LIGHT = new ColorSpec(Color.WHITE, 0.12f, 0.30f, 0.70f, 1.00f);

    private final int mColor;

    private final float mDividerAlpha;

    private final float mHintAlpha;

    private final float mSecondaryAlpha;

    private final float mPrimaryAlpha;

    public ColorSpec(@ColorInt int color, float dividerAlpha, float hintAlpha,
                     float secondaryAlpha, float primaryAlpha) {
        mColor = color;
        mDividerAlpha = dividerAlpha;
        mHintAlpha = hintAlpha;
        mSecondaryAlpha = secondaryAlpha;
        mPrimaryAlpha = primaryAlpha;
    }

    @ColorInt
    public int primaryText() {
        return Colors.alpha(mColor, mPrimaryAlpha);
    }

    @ColorInt
    public int secondaryText() {
        return Colors.alpha(mColor, mSecondaryAlpha);
    }

    @ColorInt
    public int hint() {
        return Colors.alpha(mColor, mHintAlpha);
    }

    @ColorInt
    public int divider() {
        return Colors.alpha(mColor, mDividerAlpha);
    }

}
