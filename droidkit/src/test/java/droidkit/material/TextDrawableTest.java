package droidkit.material;

import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class TextDrawableTest {

    private TextDrawable.Builder mBuilder;

    @Before
    public void setUp() throws Exception {
        mBuilder = TextDrawable.builder()
                .width(56)
                .height(56)
                .font(Typeface.SANS_SERIF)
                .fontSize(14f)
                .fontBold(true)
                .border(2)
                .text("A")
                .color("A");
    }

    @Test
    public void testBounds() throws Exception {
        final TextDrawable drawable = mBuilder.buildCircle();
        Assert.assertEquals(56, drawable.getIntrinsicWidth());
        Assert.assertEquals(56, drawable.getIntrinsicHeight());
    }

    @Test
    public void testCircle() throws Exception {
        final TextDrawable drawable = TextDrawable.circle("A");
        Assert.assertTrue(drawable.getShape() instanceof OvalShape);
    }

    @Test
    public void testBuildCircle() throws Exception {
        final TextDrawable drawable = mBuilder.buildCircle();
        Assert.assertTrue(drawable.getShape() instanceof OvalShape);
    }

    @Test
    public void testRect() throws Exception {
        final TextDrawable drawable = TextDrawable.rect("A");
        Assert.assertTrue(drawable.getShape() instanceof RectShape);
    }

    @Test
    public void testBuildRect() throws Exception {
        final TextDrawable drawable = mBuilder.buildRect();
        Assert.assertTrue(drawable.getShape() instanceof RectShape);
    }

    @Test
    public void testRoundRect() throws Exception {
        final TextDrawable drawable = TextDrawable.roundRect("A", 8f);
        Assert.assertTrue(drawable.getShape() instanceof RoundRectShape);
    }

    @Test
    public void testBuildRoundRect() throws Exception {
        final TextDrawable drawable = mBuilder.buildRect(8f);
        Assert.assertTrue(drawable.getShape() instanceof RoundRectShape);
    }

    @Test
    public void testDraw() throws Exception {
        final TextDrawable drawable = mBuilder.buildCircle();
        final Canvas canvas = new Canvas();
        drawable.draw(canvas);
        // TODO: assertions?
    }
}