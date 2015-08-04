package droidkit.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import droidkit.io.IOUtils;
import droidkit.log.Logger;

/**
 * @author Daniel Serdyukov
 */
public final class Bitmaps {

    private static final int BITMAP_HEAD = 1024;

    private static final double LN_2 = Math.log(2);

    private Bitmaps() {
    }

    @NonNull
    public static Bitmap scale(@NonNull Bitmap bitmap, int maxSize) {
        final float factor = (float) maxSize / Math.max(bitmap.getWidth(), bitmap.getHeight());
        final Matrix matrix = new Matrix();
        matrix.postScale(factor, factor);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    @NonNull
    public static Bitmap scale(@NonNull Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    @NonNull
    public static Bitmap round(@NonNull Bitmap bitmap, float radius) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        final RectF rect = new RectF(0.0f, 0.0f, width, height);
        canvas.drawRoundRect(rect, radius, radius, paint);

        return output;
    }

    @NonNull
    public static Bitmap circle(@NonNull Bitmap bitmap) {
        final int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        final Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        final float bounds = (float) size / 2;

        canvas.drawCircle(bounds, bounds, bounds, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    @Nullable
    public static Bitmap decodeFile(@NonNull String filePath, int hwSize) {
        return decodeFile(filePath, hwSize, true);
    }

    @Nullable
    public static Bitmap decodeFile(@NonNull String filePath, int hwSize, boolean exif) {
        final Bitmap bitmap = decodeFileInternal(filePath, hwSize);
        if (bitmap != null && exif) {
            return applyExif(bitmap, filePath);
        }
        return bitmap;
    }

    @Nullable
    public static Bitmap decodeStream(@NonNull InputStream stream, int hwSize) {
        return decodeStream(stream, null, hwSize);
    }

    @Nullable
    public static Bitmap decodeStream(@NonNull InputStream stream, Rect outPadding, int hwSize) {
        if (hwSize > 0) {
            final InputStream localIn = new BufferedInputStream(stream);
            try {
                final BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inJustDecodeBounds = true;
                localIn.mark(BITMAP_HEAD);
                BitmapFactory.decodeStream(localIn, outPadding, ops);
                ops.inSampleSize = calculateInSampleSize(ops, hwSize);
                ops.inJustDecodeBounds = false;
                localIn.reset();
                return BitmapFactory.decodeStream(localIn, outPadding, ops);
            } catch (IOException e) {
                Logger.error(e);
            } finally {
                IOUtils.closeQuietly(localIn);
            }
            return null;
        }
        return BitmapFactory.decodeStream(stream);
    }

    public static int calculateInSampleSize(BitmapFactory.Options ops, int hwSize) {
        final int outHeight = ops.outHeight;
        final int outWidth = ops.outWidth;
        if (outWidth > hwSize || outHeight > hwSize) {
            final double ratio = Math.max(
                    Math.round((double) outWidth / (double) hwSize),
                    Math.round((double) outHeight / (double) hwSize)
            );
            return ratio > 0 ? (int) Math.pow(2, Math.floor(Math.log(ratio) / LN_2)) : 1;
        }
        return 1;
    }

    @Nullable
    private static Bitmap decodeFileInternal(String filePath, int hwSize) {
        if (hwSize > 0) {
            final BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, ops);
            ops.inSampleSize = calculateInSampleSize(ops, hwSize);
            ops.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(filePath, ops);
        }
        return BitmapFactory.decodeFile(filePath);
    }

    @Nullable
    private static Bitmap applyExif(@NonNull Bitmap bitmap, String exifFilePath) {
        final int orientation = getExifOrientation(exifFilePath);
        if (orientation == ExifInterface.ORIENTATION_NORMAL
                || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            return bitmap;
        }
        try {
            return Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    getExifMatrix(orientation), true
            );
        } finally {
            bitmap.recycle();
        }
    }

    private static int getExifOrientation(String filePath) {
        try {
            return new ExifInterface(filePath).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );
        } catch (IOException e) {
            Logger.error(e);
        }
        return ExifInterface.ORIENTATION_UNDEFINED;
    }

    @NonNull
    private static Matrix getExifMatrix(int orientation) {
        final Matrix matrix = new Matrix();
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            matrix.setRotate(180);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.setRotate(90);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            matrix.setRotate(-90);
        }
        return matrix;
    }

}
