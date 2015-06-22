package droidkit.material;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * @author Daniel Serdyukov
 */
public final class FontColor {

    public static final FontColor MATERIAL_DARK = new FontColor(Color.BLACK, 0.12f, 0.26f, 0.54f, 0.87f);

    public static final FontColor MATERIAL_LIGHT = new FontColor(Color.WHITE, 0.12f, 0.30f, 0.70f, 1.00f);

    private final int mColor;

    private final float mDividerAlpha;

    private final float mHintAlpha;

    private final float mSecondaryAlpha;

    private final float mPrimaryAlpha;

    public FontColor(@ColorInt int color, float dividerAlpha, float hintAlpha,
                     float secondaryAlpha, float primaryAlpha) {
        mColor = color;
        mDividerAlpha = dividerAlpha;
        mHintAlpha = hintAlpha;
        mSecondaryAlpha = secondaryAlpha;
        mPrimaryAlpha = primaryAlpha;
    }

    @ColorInt
    public int divider() {
        return Colors.alpha(mColor, mDividerAlpha);
    }

    @ColorInt
    public int hint() {
        return Colors.alpha(mColor, mHintAlpha);
    }

    @ColorInt
    public int secondary() {
        return Colors.alpha(mColor, mSecondaryAlpha);
    }

    @ColorInt
    public int primary() {
        return Colors.alpha(mColor, mPrimaryAlpha);
    }

}
