package droidkit.text;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.LruCache;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public final class Fonts {

    private static final String FONTS_DIR = "fonts";

    private static final String FONTS_PATH = FONTS_DIR + "/";

    private static final LruCache<String, Typeface> FONTS = new LruCache<>(8);

    private Fonts() {
    }

    @NonNull
    public static List<String> list(@NonNull AssetManager am) {
        try {
            return Arrays.asList(am.list(FONTS_DIR));
        } catch (IOException ignored) {
            return Collections.emptyList();
        }
    }

    @NonNull
    public static Typeface get(@NonNull AssetManager am, @NonNull String name) {
        Typeface tf = FONTS.get(name);
        if (tf == null) {
            tf = Typeface.createFromAsset(am, FONTS_PATH + name);
            FONTS.put(name, tf);
        }
        return tf;
    }

    public static void apply(@NonNull TextView textView, @NonNull Typeface typeface) {
        textView.setTypeface(typeface);
        textView.getPaint().setAntiAlias(true);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public static void apply(@NonNull TextView textView, @NonNull String name) {
        apply(textView, get(textView.getContext().getAssets(), name));
    }

}
