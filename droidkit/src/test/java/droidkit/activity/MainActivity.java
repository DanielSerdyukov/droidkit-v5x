package droidkit.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;
import droidkit.util.Objects;
import droidkit.view.Views;

/**
 * @author Daniel Serdyukov
 */
public class MainActivity extends Activity {

    @InjectView(android.R.id.text1)
    TextView mText1;

    @InjectView(android.R.id.button1)
    Button mButton1;

    boolean mButton1Clicked;

    boolean mButton2Clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FrameLayout layout = new FrameLayout(this);
        final TextView textView = new TextView(this);
        textView.setId(android.R.id.text1);
        layout.addView(textView);
        final Button button1 = new Button(this);
        button1.setId(android.R.id.button1);
        layout.addView(button1);
        final Button button2 = new Button(this);
        button2.setId(android.R.id.button2);
        layout.addView(button2);
        setContentView(layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, android.R.id.cut, 0, "Cut");
        menu.add(0, android.R.id.copy, 1, "Copy");
        menu.add(0, android.R.id.paste, 2, "Paste");
        menu.add(0, android.R.id.edit, 3, "Edit");
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(android.R.id.button1)
    private void onButton1Click() {
        mButton1Clicked = true;
    }

    @OnClick(android.R.id.button2)
    private void onButton2Click(View view) {
        mButton2Clicked = Objects.equal(Views.findById(this, android.R.id.button2), view);
    }

    @OnActionClick(android.R.id.cut)
    private void onCutActionClick() {

    }

    @OnActionClick(android.R.id.copy)
    private boolean onCopyActionClick() {
        return true;
    }

    @OnActionClick(android.R.id.paste)
    private void onPasteActionClick(MenuItem item) {

    }

    @OnActionClick(android.R.id.edit)
    private boolean onEditActionClick(MenuItem item) {
        return item.getItemId() == android.R.id.edit;
    }

}
