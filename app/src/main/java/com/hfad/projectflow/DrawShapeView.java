package com.hfad.projectflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawShapeView extends View {

    private final Paint paint;
    private final Paint gridPaint;
    private int startX, startY, endX, endY;
    private String shapeType = "RECTANGLE";
    private List<Shape> shapes = new ArrayList<>();

    private final int gridSize = 50; // Grid size in pixels

    public DrawShapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        drawGrid(canvas);
        for (Shape shape : shapes) {
            drawShape(canvas, shape);
        }
        drawShape(canvas, new Shape(shapeType, startX, startY, endX, endY));
    }

    private void drawGrid(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        // Draw vertical lines
        for (int i = 0; i <= width; i += gridSize) {
            canvas.drawLine(i, 0, i, height, gridPaint);
        }
        // Draw horizontal lines
        for (int i = 0; i <= height; i += gridSize) {
            canvas.drawLine(0, i, getWidth(), i, gridPaint);
        }
    }

    private void drawShape(Canvas canvas, Shape shape){
        switch (shape.type){
            case "RECTANGLE":
                canvas.drawRect(shape.startX, shape.startY, shape.endX, shape.endY, paint);
                break;
            case "CIRCLE":
                float radius = Math.max(Math.abs(shape.endX - shape.startX), Math.abs(shape.endY - shape.startY));
                canvas.drawCircle(shape.startX, shape.startY, radius, paint);
                break;
            case "LINE":
                canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, paint);
                // TO DO figure out how to add arrow ends, calculate the positions
                break;
            case "DIAMOND":
                canvas.drawLine(shape.startX, (shape.endY+shape.startY)/2, (shape.endX+shape.startX)/2, shape.startY, paint);
                canvas.drawLine((shape.endX+shape.startX)/2, shape.startY, shape.endX, (shape.endY+shape.startY)/2, paint);
                canvas.drawLine(shape.startX, (shape.endY+shape.startY)/2, (shape.endX+shape.startX)/2, shape.endY, paint);
                canvas.drawLine((shape.endX+shape.startX)/2, shape.endY, shape.endX, (shape.endY+shape.startY)/2, paint);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        boolean tempX = isEven(((int) event.getX()) /50);
        boolean tempY = isEven(((int) event.getY()) /50);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                startX = ((int) event.getX()) % 50 < 25 ? ((int) event.getX()) /50 *50 : tempX ? Math.round(event.getX()/100)*100+50 : Math.round(event.getX()/100)*100;
                startY = ((int) event.getY()) % 50 < 25 ? ((int) event.getY()) /50 *50 : tempY ? Math.round(event.getY()/100)*100+50 : Math.round(event.getY()/100)*100;
                //startX = Math.round(event.getX()/100)*100;
                //startY = Math.round(event.getY()/100)*100;
                break;
            case MotionEvent.ACTION_MOVE:
                endX = ((int) event.getX()) % 50 < 25 ? ((int) event.getX()) /50 *50 : tempX ? Math.round(event.getX()/100)*100+50 : Math.round(event.getX()/100)*100;;
                endY = ((int) event.getY()) % 50 < 25 ? ((int) event.getY()) /50 *50 : tempY ? Math.round(event.getY()/100)*100+50 : Math.round(event.getY()/100)*100;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                endX = ((int) event.getX()) % 50 < 25 ? ((int) event.getX()) /50 *50 : tempX ? Math.round(event.getX()/100)*100+50 : Math.round(event.getX()/100)*100;
                endY = ((int) event.getY()) % 50 < 25 ? ((int) event.getY()) /50 *50 : tempY ? Math.round(event.getY()/100)*100+50 : Math.round(event.getY()/100)*100;
                shapes.add(new Shape(shapeType, startX, startY, endX, endY));
                invalidate();
                break;
        }
        return true;


    }

    public static boolean isEven(int number) {
        return number % 2 == 0;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }

    public void setPaintColor(int color) {
        paint.setColor(color);
    }

    public void setPaintStrokeWidth(float width) {
        paint.setStrokeWidth(width);
    }
}

