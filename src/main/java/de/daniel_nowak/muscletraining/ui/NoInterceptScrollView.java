package de.daniel_nowak.muscletraining.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class NoInterceptScrollView extends ScrollView {

    public NoInterceptScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // Wenn mehr als 1 Finger → NICHT abfangen
        if (ev.getPointerCount() > 1) {
            return false;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
