package droidkit.text;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Daniel Serdyukov
 */
public final class Fonts {

    private static final String FONTS_DIR = "fonts";

    private static final AtomicReference<FontFactory> FONT_FACTORY = new AtomicReference<>();

    static {
        setFontFactory(new AssetsFontFactory(FONTS_DIR));
    }

    private Fonts() {
    }

    public static void setFontFactory(@NonNull FontFactory factory) {
        FONT_FACTORY.compareAndSet(FONT_FACTORY.get(), factory);
    }

    @NonNull
    public static Collection<String> list(@NonNull Context context) {
        return FONT_FACTORY.get().getAvailableFontNames(context);
    }

    @NonNull
    public static Typeface getTypeface(@NonNull Context context, @NonNull String fontName) {
        return FONT_FACTORY.get().getTypeface(context, fontName);
    }

    public static void apply(@NonNull TextView textView, @NonNull Typeface typeface) {
        textView.setTypeface(typeface);
        textView.getPaint().setAntiAlias(true);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public static void apply(@NonNull TextView textView, @NonNull String fontName) {
        apply(textView, getTypeface(textView.getContext(), fontName));
    }

    public interface FontFactory {

        Collection<String> getAvailableFontNames(@NonNull Context context);

        @NonNull
        Typeface getTypeface(@NonNull Context context, @NonNull String fontName);

    }

    public static class AssetsFontFactory implements FontFactory {

        private final LruCache<String, Typeface> mFontsCache = new LruCache<>(8);

        private final String mFontsDir;

        public AssetsFontFactory(@NonNull String fontsDir) {
            mFontsDir = fontsDir;
        }

        @Override
        public Collection<String> getAvailableFontNames(@NonNull Context context) {
            try {
                return Arrays.asList(context.getAssets().list(mFontsDir));
            } catch (IOException e) {
                Log.e("AssetsFontFactory", e.getMessage(), e);
                return Collections.emptyList();
            }
        }

        @NonNull
        @Override
        public Typeface getTypeface(@NonNull Context context, @NonNull String fontName) {
            if (!getAvailableFontNames(context).contains(fontName)) {
                throw new NoSuchElementException("No such font '" + fontName + "' in directory " + mFontsDir);
            }
            Typeface tf = mFontsCache.get(fontName);
            if (tf == null) {
                tf = Typeface.createFromAsset(context.getAssets(), mFontsDir + File.separator + fontName);
                mFontsCache.put(fontName, tf);
            }
            return tf;
        }

    }

}
