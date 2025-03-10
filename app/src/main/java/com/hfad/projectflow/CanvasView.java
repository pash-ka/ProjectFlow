package com.hfad.projectflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CanvasView extends View {

    private Paint paint;
    private Path path;
    private Matrix matrix;
    private float[] matrixValues;

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);

        path = new Path();

        matrix = new Matrix();
        matrixValues = new float[9];


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.setMatrix(matrix);
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startDrawing(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                updateDrawing(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // Optional: handle action up if needed
                break;
        }

        invalidate();
        return true;
    }

    private void startDrawing(float x, float y) {
        path.moveTo(x, y);
    }

    private void updateDrawing(float x, float y) {
        path.lineTo(x, y);

    }

    public void clearCanvas() {
        path.reset();
        invalidate();
    }

    public void scaleCanvas(float scaleFactor) {
        matrix.postScale(scaleFactor, scaleFactor);
        invalidate();
    }

    public void panCanvas(float dx, float dy) {
        matrix.postTranslate(dx, dy);
        invalidate();
    }
}
