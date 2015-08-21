package droidkit.material;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import java.security.SecureRandom;

/**
 * @author Daniel Serdyukov
 */
public abstract class ColorPalette {

    public static final ColorPalette MATERIAL = new MaterialColorPalette();

    private final int[] mColors;

    private final SecureRandom mRandom;

    public ColorPalette(@ColorInt int... colors) {
        mColors = colors;
        mRandom = new SecureRandom();
    }

    @ColorInt
    public int getRandomColor() {
        return mColors[mRandom.nextInt(mColors.length)];
    }

    @ColorInt
    public int getColor(@NonNull Object object) {
        return mColors[Math.abs(object.hashCode()) % mColors.length];
    }

    @NonNull
    public abstract ColorSpec getColorSpec(@ColorInt int mainColor);

}
