package droidkit.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;

/**
 * @author Daniel Serdyukov
 */
public class MainFragment extends Fragment {

    @InjectView(android.R.id.text1)
    TextView mText1;

    @InjectView(android.R.id.button1)
    Button mButton1;

    boolean mButton1Clicked;

    boolean mCutClicked;

    boolean mCopyClicked;

    boolean mPasteClicked;

    boolean mEditClicked;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FrameLayout layout = new FrameLayout(container.getContext());
        final TextView textView = new TextView(container.getContext());
        textView.setId(android.R.id.text1);
        layout.addView(textView);
        final Button button1 = new Button(container.getContext());
        button1.setId(android.R.id.button1);
        layout.addView(button1);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, android.R.id.cut, 0, "Cut");
        menu.add(0, android.R.id.copy, 1, "Copy");
        menu.add(0, android.R.id.paste, 2, "Paste");
        menu.add(0, android.R.id.edit, 3, "Edit");
    }

    @OnClick(android.R.id.button1)
    private void onButton1Click() {
        mButton1Clicked = true;
    }

    @OnActionClick(android.R.id.cut)
    private void onCutActionClick() {
        mCutClicked = true;
    }

    @OnActionClick(android.R.id.copy)
    private boolean onCopyActionClick() {
        mCopyClicked = true;
        return true;
    }

    @OnActionClick(android.R.id.paste)
    private void onPasteActionClick(MenuItem item) {
        mPasteClicked = android.R.id.paste == item.getItemId();
    }

    @OnActionClick(android.R.id.edit)
    private boolean onEditActionClick(MenuItem item) {
        return (mEditClicked = android.R.id.edit == item.getItemId());
    }

}
