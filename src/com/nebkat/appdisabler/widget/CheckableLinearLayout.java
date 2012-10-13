package com.nebkat.appdisabler.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;
import com.nebkat.appdisabler.R;

/*
 * This class is useful for using inside of ListView that needs to have checkable items.
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private CheckBox mCheckBox;

    @SuppressWarnings("unused")
    public CheckableLinearLayout(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
    }

    @Override
    public boolean isChecked() {
        return mCheckBox.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        mCheckBox.setChecked(checked);
    }

    @Override
    public void toggle() {
        mCheckBox.toggle();
    }
} 