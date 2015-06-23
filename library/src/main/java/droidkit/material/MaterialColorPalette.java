package droidkit.material;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.SparseArray;

/**
 * @author Daniel Serdyukov
 */
class MaterialColorPalette extends ColorPalette implements MaterialColors {

    private static final SparseArray<ColorSpec> FONT_COLOR_MAP = new SparseArray<>();

    static {
        FONT_COLOR_MAP.put(RED_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_A200, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_A400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_300, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_A200, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_A400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_200, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_300, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_A200, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_A400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_300, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_A200, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_A400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_300, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_A200, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_A400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_A200, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_A400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(CYAN_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(CYAN_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(CYAN_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_GREEN_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_GREEN_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_GREEN_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIME_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(ORANGE_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(ORANGE_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_A400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_A700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_300, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_900, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_400, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_500, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_600, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_700, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_800, ColorSpec.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_900, ColorSpec.MATERIAL_LIGHT);
    }

    public MaterialColorPalette() {
        super(
                RED_400,
                PINK_400,
                PURPLE_400,
                DEEP_PURPLE_400,
                INDIGO_400,
                BLUE_400,
                LIGHT_BLUE_400,
                CYAN_400,
                TEAL_400,
                GREEN_400,
                LIGHT_GREEN_400,
                LIME_400,
                YELLOW_400,
                AMBER_400,
                ORANGE_400,
                DEEP_ORANGE_400,
                BROWN_400,
                GREY_400,
                BLUE_GREY_400
        );
    }

    @NonNull
    @Override
    public ColorSpec getColorSpec(@ColorInt int mainColor) {
        return FONT_COLOR_MAP.get(mainColor, ColorSpec.MATERIAL_DARK);
    }

}
