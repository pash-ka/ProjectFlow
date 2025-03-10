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

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawShapeView extends View {

    private final Paint paint;
    private final Paint gridPaint;

    private final Path path;
    private final Path previewPath;

    private final Matrix matrix;
    private final Matrix inverseMatrix;
    private float[] matrixValues;

    private float startX, startY, endX, endY, currentX, currentY;
    private ShapeType shapeType;
    private List<Shape> shapes = new ArrayList<>();

    private final ScaleGestureDetector scaleGestureDetector;
    private final GestureDetector gestureDetector;

    private final int gridSize = 50; // Grid size in pixels
    public boolean grid = true;
    public boolean drawOnGrid = true;
    public boolean hand = false;

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

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
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
        // Get the visible bounds in canvas space
        Rect visibleRect = new Rect();
        canvas.getClipBounds(visibleRect);

        // Extract the bounds in transformed space
        float visibleLeft = visibleRect.left;
        float visibleTop = visibleRect.top;
        float visibleRight = visibleRect.right;
        float visibleBottom = visibleRect.bottom;

        // Align the grid lines to the grid spacing
        float alignedLeft = (float) Math.floor(visibleLeft / gridSize) * gridSize;
        float alignedTop = (float) Math.floor(visibleTop / gridSize) * gridSize;

        // Draw horizontal grid lines
        for (float y = alignedTop; y <= visibleBottom; y += gridSize) {
            canvas.drawLine(visibleLeft, y, visibleRight, y, gridPaint);
        }

        // Draw vertical grid lines
        for (float x = alignedLeft; x <= visibleRight; x += gridSize) {
            canvas.drawLine(x, visibleTop, x, visibleBottom, gridPaint);
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


    private float snapToGrid(float coordinate){
        return Math.round(coordinate / gridSize) * gridSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        float[] touchPoint = { event.getX(), event.getY() };
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(touchPoint);
        currentX = touchPoint[0];
        currentY = touchPoint[1];

        //currentX = (int) event.getX();
        //currentY = (int) event.getY();


        boolean tempX = isEven(((int) currentX) /50);
        boolean tempY = isEven(((int) currentY) /50);

        if (shapeType != ShapeType.NONE && !hand){
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    if (drawOnGrid){
                        /*
                        startX = Math.abs(currentX % 50) < 25 ? (float) ((int) currentX /50) *50 : tempX ? (float) (Math.round(currentX /100))*100+(currentX>0 ? 50 : -50) : (float) (Math.round(currentX /100))*100;
                        startY = Math.abs(currentY % 50) < 25 ? (float) ((int) currentY /50) *50 : tempY ? (float) (Math.round(currentY /100))*100+(currentY>0 ? 50 : -50) : (float) (Math.round(currentY /100))*100;
                        */
                        startX = snapToGrid(currentX);
                        startY = snapToGrid(currentY);
                    }
                    else {
                        startX = currentX;
                        startY = currentY;
                    }
                    if (shapeType == ShapeType.LINE) {
                        previewPath.moveTo(startX, startY);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (drawOnGrid){
                        currentX = snapToGrid(currentX);
                        currentY = snapToGrid(currentY);
                    }
                    if (shapeType == ShapeType.LINE) {
                        previewPath.reset();
                        previewPath.moveTo(startX, startY);
                        previewPath.lineTo(currentX, currentY);
                    }
                    else if (shapeType == ShapeType.RECTANGLE) {
                        previewPath.reset();
                        previewPath.addRect(startX, startY, currentX, currentY, Path.Direction.CW);
                    } else if (shapeType == ShapeType.CIRCLE) {
                        previewPath.reset();
                        float radius = (float) Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                        previewPath.addCircle(startX, startY, radius, Path.Direction.CW);
                    }

                    //invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if (drawOnGrid){
                        currentX = snapToGrid(currentX);
                        currentY = snapToGrid(currentY);
                    }

                    if (shapeType == ShapeType.LINE) {
                        path.moveTo(startX, startY);
                        path.lineTo(currentX, currentY);

                    } else if (shapeType == ShapeType.RECTANGLE) {
                        path.addRect(startX, startY, currentX, currentY, Path.Direction.CW);
                    } else if (shapeType == ShapeType.CIRCLE) {
                        float radius = (float) Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                        path.addCircle(startX, startY, radius, Path.Direction.CW);
                    }
                    previewPath.reset();
                    //invalidate();
                    //shapes.add(new Shape(shapeType, startX, startY, endX, endY));

                    break;
            }
        }
        else if (shapeType == ShapeType.NONE && !hand){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startDrawing(currentX, currentY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    updateDrawing(currentX, currentY);
                    break;
                case MotionEvent.ACTION_UP:
                    // Optional: handle action up if needed
                    break;
            }
        }
        else{
            scaleGestureDetector.onTouchEvent(event);
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

