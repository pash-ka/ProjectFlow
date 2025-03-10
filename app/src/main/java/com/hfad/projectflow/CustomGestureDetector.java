package com.hfad.projectflow;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomGestureDetector extends GestureDetector {

    private GestureListener gestureListener;

    public CustomGestureDetector(@Nullable Context context, @NonNull GestureListener listener) {
        super(context, listener);
        this.gestureListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            gestureListener.onUp(event);
        }
        return super.onTouchEvent(event);
    }

    public interface GestureListener extends OnGestureListener {
        void onUp(MotionEvent e);
    }


}
