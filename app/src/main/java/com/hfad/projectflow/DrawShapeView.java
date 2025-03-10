package com.hfad.projectflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrawShapeView extends View {

    private final Paint paint;
    private final Paint gridPaint;

    private Path path;
    private Path previewPath;

    private Matrix matrix;
    private Matrix inverseMatrix;
    private float[] matrixValues;

    private int startX, startY, endX, endY, x, y;
    private ShapeType shapeType;
    private List<Shape> shapes = new ArrayList<>();

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private final int gridSize = 50; // Grid size in pixels
    public boolean grid = true;
    public boolean drawOnGrid = false;
    public boolean hand = false;
    public boolean doubleTap;

    /*
    back to drawing in onTouchEvent
    gestureListener only for panning the canvas
    hand in use
    no custom detector
     */

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (hand) {
                float scaleFactor = detector.getScaleFactor();
                scaleCanvas(scaleFactor);
            }
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d("Event", "onScroll");
            //Log.d("onScroll", String.valueOf(doubleTap));
            if (hand) panCanvas(-distanceX, -distanceY);
            //if (!doubleTap) handleDrawing(e2);

            return true;
        }

    }

    public DrawShapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.STROKE);


        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1);

        path = new Path();
        previewPath = new Path();

        matrix = new Matrix();
        inverseMatrix = new Matrix();
        matrixValues = new float[9];

        shapeType = ShapeType.NONE;

        //scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }


    @Override
    protected void onDraw(Canvas canvas){
        /*super.onDraw(canvas);
        drawGrid(canvas);
        for (Shape shape : shapes) {
            drawShape(canvas, shape);
        }
        drawShape(canvas, new Shape(shapeType, startX, startY, endX, endY));*/
        super.onDraw(canvas);


        canvas.save();
        canvas.setMatrix(matrix);

        if (grid) drawDynamicGrid(canvas);
        canvas.drawPath(path, paint);
        canvas.drawPath(previewPath, paint);

        //if (shapeType != ShapeType.NONE) drawCurrentShapePreview(canvas);

        canvas.restore();
    }

    private void drawDynamicGrid(Canvas canvas) {
        // Get the current transformation matrix's inverse
        Matrix inverse = new Matrix();
        matrix.invert(inverse);

        // Map the canvas visible area to the grid's coordinate system
        Rect visibleRect = new Rect();
        canvas.getClipBounds(visibleRect);
        float[] transformedCorners = new float[]{
                visibleRect.left, visibleRect.top,
                visibleRect.right, visibleRect.top,
                visibleRect.right, visibleRect.bottom,
                visibleRect.left, visibleRect.bottom
        };
        inverse.mapPoints(transformedCorners);

        // Calculate visible bounds in grid space
        float left = Math.min(transformedCorners[0], transformedCorners[2]); // Minimum x
        float top = Math.min(transformedCorners[1], transformedCorners[5]);  // Minimum y
        float right = Math.max(transformedCorners[4], transformedCorners[6]); // Maximum x
        float bottom = Math.max(transformedCorners[3], transformedCorners[7]); // Maximum y

        // Align the grid lines to the grid spacing
        float alignedLeft = (float) Math.floor(left / gridSize) * gridSize;
        float alignedTop = (float) Math.floor(top / gridSize) * gridSize;

        // Draw horizontal lines
        for (float y = alignedTop; y <= bottom; y += gridSize) {
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        // Draw vertical lines
        for (float x = alignedLeft; x <= right; x += gridSize) {
            canvas.drawLine(x, top, x, bottom, gridPaint);
        }
    }


    private void drawGrid(Canvas canvas) {

        float[] values = new float[9];
        matrix.getValues(values);

        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        float translateX = values[Matrix.MTRANS_X];
        float translateY = values[Matrix.MTRANS_Y];

        int width = getWidth();
        int height = getHeight();

        // Calculate the start and end points for the grid lines
        float startX = (translateX % (gridSize * scaleX)) - width;
        float startY = (translateY % (gridSize * scaleY)) - height;
        float endX = startX + width * 2;
        float endY = startY + height * 2;

        // Draw vertical grid lines
        for (float x = startX; x < endX; x += gridSize * scaleX) {
            canvas.drawLine(x, startY, x, endY, gridPaint);
        }

        // Draw horizontal grid lines
        for (float y = startY; y < endY; y += gridSize * scaleY) {
            canvas.drawLine(startX, y, endX, y, gridPaint);
        }


    }

    public void drawShape(Canvas canvas, Shape shape){
        switch (shape.type){
            case RECTANGLE:
                canvas.drawRect(shape.startX, shape.startY, shape.endX, shape.endY, paint);
                break;
            case CIRCLE:
                float radius = Math.max(Math.abs(shape.endX - shape.startX), Math.abs(shape.endY - shape.startY));
                canvas.drawCircle(shape.startX, shape.startY, radius, paint);
                break;
            case LINE:
                canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, paint);
                // TO DO figure out how to add arrow ends, calculate the positions
                break;
            case DIAMOND:
                canvas.drawLine(shape.startX, (shape.endY+shape.startY)/2, (shape.endX+shape.startX)/2, shape.startY, paint);
                canvas.drawLine((shape.endX+shape.startX)/2, shape.startY, shape.endX, (shape.endY+shape.startY)/2, paint);
                canvas.drawLine(shape.startX, (shape.endY+shape.startY)/2, (shape.endX+shape.startX)/2, shape.endY, paint);
                canvas.drawLine((shape.endX+shape.startX)/2, shape.endY, shape.endX, (shape.endY+shape.startY)/2, paint);
                break;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){

        float[] touchPoint = { event.getX(), event.getY() };
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(touchPoint);
        x = (int) touchPoint[0];
        y = (int) touchPoint[1];

        //x = (int) event.getX();
        //y = (int) event.getY();


        boolean tempX = isEven(x /50);
        boolean tempY = isEven(y /50);

        if (shapeType != ShapeType.NONE && !hand){
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    if (drawOnGrid){
                        startX = x % 50 < 25 ? x /50 *50 : tempX ? Math.round(event.getX()/100)*100+50 : Math.round(event.getX()/100)*100;
                        startY = y % 50 < 25 ? y /50 *50 : tempY ? Math.round(event.getY()/100)*100+50 : Math.round(event.getY()/100)*100;
                    }
                    else {
                        startX = x;
                        startY = y;
                        if (shapeType == ShapeType.LINE) {
                            previewPath.moveTo(x, y);
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (drawOnGrid){
                        endX = ((int) event.getX()) % 50 < 25 ? ((int) event.getX()) /50 *50 : tempX ? Math.round(event.getX()/100)*100+50 : Math.round(event.getX()/100)*100;;
                        endY = ((int) event.getY()) % 50 < 25 ? ((int) event.getY()) /50 *50 : tempY ? Math.round(event.getY()/100)*100+50 : Math.round(event.getY()/100)*100;
                    }
                    else {
                        endX = (int) event.getX();
                        endY = (int) event.getY();
                        if (shapeType == ShapeType.LINE) {
                            previewPath.reset();
                            previewPath.moveTo(startX, startY);
                            previewPath.lineTo(x, y);
                        }
                        else if (shapeType == ShapeType.RECTANGLE) {
                            previewPath.reset();
                            previewPath.addRect(startX, startY, x, y, Path.Direction.CW);
                        } else if (shapeType == ShapeType.CIRCLE) {
                            previewPath.reset();
                            float radius = (float) Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
                            previewPath.addCircle(startX, startY, radius, Path.Direction.CW);
                        }
                    }

                    //invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if (drawOnGrid){
                        endX = ((int) event.getX()) % 50 < 25 ? ((int) event.getX()) /50 *50 : tempX ? Math.round(event.getX()/100)*100+50 : Math.round(event.getX()/100)*100;
                        endY = ((int) event.getY()) % 50 < 25 ? ((int) event.getY()) /50 *50 : tempY ? Math.round(event.getY()/100)*100+50 : Math.round(event.getY()/100)*100;
                    }
                    else {
                        endX = (int) event.getX();
                        endY = (int) event.getY();
                        if (shapeType == ShapeType.LINE) {
                            path.moveTo(startX, startY);
                            path.lineTo(x, y);

                        } else if (shapeType == ShapeType.RECTANGLE) {
                            path.addRect(startX, startY, x, y, Path.Direction.CW);
                        } else if (shapeType == ShapeType.CIRCLE) {
                            float radius = (float) Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
                            path.addCircle(startX, startY, radius, Path.Direction.CW);
                        }
                        previewPath.reset();

                    }
                    //invalidate();
                    shapes.add(new Shape(shapeType, startX, startY, endX, endY));

                    break;
            }
        }
        else if (shapeType == ShapeType.NONE && !hand){
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
        }
        else{
            //scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
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

    public static boolean isEven(int number) {
        return number % 2 == 0;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public void setPaintColor(int color) {
        paint.setColor(color);
    }

    public void setPaintStrokeWidth(float width) {
        paint.setStrokeWidth(width);
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

