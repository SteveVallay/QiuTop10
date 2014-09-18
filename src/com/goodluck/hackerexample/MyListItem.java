package com.goodluck.hackerexample;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.TwoLineListItem;

public class MyListItem extends TwoLineListItem  implements Checkable{

    private boolean isChecked = false;
    public MyListItem(Context context) {
        this(context, null, 0);
    }
    public MyListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MyListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public MyListItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setChecked(boolean check) {
        isChecked = check;
        if(isChecked){
            this.setBackgroundColor(Color.MAGENTA);
        } else {
            this.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void toggle() {
    }
}
