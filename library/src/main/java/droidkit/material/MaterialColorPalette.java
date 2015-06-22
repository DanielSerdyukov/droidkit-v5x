package droidkit.material;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.SparseArray;

/**
 * @author Daniel Serdyukov
 */
class MaterialColorPalette extends ColorPalette implements MaterialColors {

    private static final SparseArray<FontColor> FONT_COLOR_MAP = new SparseArray<>();

    static {
        FONT_COLOR_MAP.put(RED_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_A200, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_A400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(RED_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_300, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_A200, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_A400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PINK_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_200, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_300, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_A200, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_A400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(PURPLE_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_300, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_A200, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_A400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_PURPLE_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_300, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_A200, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_A400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(INDIGO_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_A200, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_A400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_BLUE_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(CYAN_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(CYAN_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(CYAN_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(TEAL_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREEN_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_GREEN_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_GREEN_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIGHT_GREEN_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(LIME_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(ORANGE_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(ORANGE_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_A400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(DEEP_ORANGE_A700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_300, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BROWN_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(GREY_900, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_400, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_500, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_600, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_700, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_800, FontColor.MATERIAL_LIGHT);
        FONT_COLOR_MAP.put(BLUE_GREY_900, FontColor.MATERIAL_LIGHT);
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
    public FontColor getFontColor(@ColorInt int mainColor) {
        return FONT_COLOR_MAP.get(mainColor, FontColor.MATERIAL_DARK);
    }

}
