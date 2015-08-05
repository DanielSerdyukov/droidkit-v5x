package droidkit.view;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import droidkit.annotation.InjectView;

/**
 * @author Daniel Serdyukov
 */
public class MainFrame extends FrameLayout {

    @InjectView(android.R.id.text1)
    TextView mText1;

    @InjectView(android.R.id.text2)
    TextView mText2;

    public MainFrame(Context context) {
        super(context);
        final TextView text1 = new TextView(context);
        addView(text1);
        text1.setId(android.R.id.text1);
        final TextView text2 = new TextView(context);
        text2.setId(android.R.id.text2);
        addView(text2);
        ViewInjector.inject(this, this);
    }

}
