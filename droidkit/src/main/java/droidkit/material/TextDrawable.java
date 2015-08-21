package droidkit.material;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import droidkit.content.StringValue;

/**
 * @author Daniel Serdyukov
 */
public class TextDrawable extends ShapeDrawable {

    private static final Typeface DEFAULT_FONT = Typeface.create("sans-serif-light", Typeface.NORMAL);

    private static final float DARKER_FACTOR = 0.9f;

    private final RectShape mShape;

    private final Paint mTextPaint;

    private final Paint mBorderPaint;

    private final String mText;

    private final int mWidth;

    private final int mHeight;

    private final float mFontSize;

    private final float mCornerRadius;

    private final int mBorderThickness;

    private TextDrawable(@NonNull Builder builder, RectShape shape) {
        super(shape);
        mShape = shape;
        mText = builder.mText;
        mWidth = builder.mWidth;
        mHeight = builder.mHeight;
        mBorderThickness = builder.mBorderThickness;
        mCornerRadius = builder.mCornerRadius;
        mFontSize = builder.mFontSize;

        mTextPaint = new Paint();
        mTextPaint.setColor(builder.mFontColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(builder.mFontBold);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTypeface(builder.mFont);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStrokeWidth(mBorderThickness);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(Colors.darker(builder.mColor, DARKER_FACTOR));
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderThickness);

        getPaint().setColor(builder.mColor);
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    @NonNull
    public static TextDrawable circle(@NonNull String text) {
        return builder().color(text).text(text, 1).buildCircle();
    }

    @NonNull
    public static TextDrawable rect(@NonNull String text) {
        return builder().color(text).text(text, 1).buildRect();
    }

    @NonNull
    public static TextDrawable roundRect(@NonNull String text, float cornerRadius) {
        return builder().color(text).text(text, 1).buildRect(cornerRadius);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final Rect rect = getBounds();
        if (mBorderThickness > 0) {
            final RectF rectf = new RectF(rect);
            rectf.inset(mBorderThickness / 2, mBorderThickness / 2);
            if (mShape instanceof OvalShape) {
                canvas.drawOval(rectf, mBorderPaint);
            } else if (mShape instanceof RoundRectShape) {
                canvas.drawRoundRect(rectf, mCornerRadius, mCornerRadius, mBorderPaint);
            } else {
                canvas.drawRect(rectf, mBorderPaint);
            }
        }
        final int count = canvas.save();
        canvas.translate(rect.left, rect.top);
        final int width = mWidth < 0 ? rect.width() : mWidth;
        final int height = mHeight < 0 ? rect.height() : mHeight;
        mTextPaint.setTextSize(mFontSize < 0 ? Math.min(width, height) / 2 : mFontSize);
        canvas.drawText(mText, width / 2, height / 2 - ((mTextPaint.descent() + mTextPaint.ascent()) / 2), mTextPaint);
        canvas.restoreToCount(count);
    }

    @Override
    public void setAlpha(int alpha) {
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mTextPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    public static final class Builder {

        private int mColor = Color.GRAY;

        private int mFontColor = Color.WHITE;

        private int mWidth = -1;

        private int mHeight = -1;

        private int mBorderThickness;

        private float mCornerRadius;

        private Typeface mFont = DEFAULT_FONT;

        private float mFontSize = -1;

        private boolean mFontBold;

        private String mText = StringValue.EMPTY;

        @NonNull
        public Builder text(@NonNull String text) {
            return text(text, text.length());
        }

        @NonNull
        public Builder text(@NonNull String text, int length) {
            final int textLength = Math.min(length, text.length());
            if (textLength > 0) {
                mText = text.substring(0, textLength);
            } else {
                mText = text;
            }
            return this;
        }

        @NonNull
        public Builder color(@NonNull Object color) {
            return color(ColorPalette.MATERIAL.getColor(color));
        }

        @NonNull
        public Builder color(@ColorInt int color) {
            mColor = color;
            return fontColor(ColorPalette.MATERIAL.getColorSpec(color).primaryText());
        }

        @NonNull
        public Builder font(@NonNull Typeface tf) {
            mFont = tf;
            return this;
        }

        @NonNull
        public Builder fontColor(@ColorInt int fontColor) {
            mFontColor = fontColor;
            return this;
        }

        @NonNull
        public Builder fontSize(float size) {
            mFontSize = size;
            return this;
        }

        @NonNull
        public Builder fontBold(boolean bold) {
            mFontBold = bold;
            return this;
        }

        @NonNull
        public Builder width(int width) {
            mWidth = width;
            return this;
        }

        @NonNull
        public Builder height(int height) {
            mHeight = height;
            return this;
        }

        @NonNull
        public Builder border(int thickness) {
            mBorderThickness = thickness;
            return this;
        }

        @NonNull
        public TextDrawable buildRect() {
            return build(new RectShape());
        }

        @NonNull
        public TextDrawable buildRect(float cornerRadius) {
            mCornerRadius = cornerRadius;
            return build(new RoundRectShape(new float[]{
                    cornerRadius, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius
            }, null, null));
        }

        @NonNull
        public TextDrawable buildCircle() {
            return build(new OvalShape());
        }

        @NonNull
        private TextDrawable build(@NonNull RectShape shape) {
            return new TextDrawable(this, shape);
        }

    }

}