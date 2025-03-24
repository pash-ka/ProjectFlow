package com.hfad.projectflow;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Drawing;
import com.hfad.projectflow.database.DrawingDao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;


/*
* get drawing from db +
* get all the drawings +
* add tabs for each drawing? or some other functionality to switch between them
* override the coordinates for drawing circle
*/
/*
Сохранять в БД при закрытии +
Через кнопку сохранять файлом +
Эту кнопку наверх к меню +
Рисунок пропадает при повороте экрана!!! +
Отключить тёмную тему на время +
Что-то не так с передачей через файлы projectId +
*/

public class DrawShapeView extends View {

    private final Paint paint;
    private final Paint gridPaint;

    private Path path;
    private final Path previewPath;

    private Bitmap bitmap;

    private final Matrix matrix;
    private final Matrix inverseMatrix;
    private float[] matrixValues;


    private float startX, startY, currentX, currentY;
    private ShapeType shapeType;

    private final ScaleGestureDetector scaleGestureDetector;
    private final GestureDetector gestureDetector;

    private final int gridSize = 50; // Grid size in pixels
    public boolean grid = true;
    public boolean drawOnGrid = true;
    public boolean hand = false;
    private int projectId;
    private int drawingId;
    private boolean newDrawing = false;

    private WhiteBoardActivity activity;

    private AppDatabase db;

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
            if (hand) panCanvas(-distanceX, -distanceY);
            return true;
        }

    }

    public DrawShapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.WHITE);
        if (context instanceof WhiteBoardActivity) {
            activity = (WhiteBoardActivity) context;
        } else {
            throw new IllegalArgumentException("Context must be an instance of Activity");
        }

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.STROKE);


        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1);


        previewPath = new Path();

        matrix = new Matrix();
        inverseMatrix = new Matrix();
        matrixValues = new float[9];

        shapeType = ShapeType.NONE;

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        db = DatabaseSingleton.getInstance(getContext());

        if (path == null) {
            path = new Path();
        }
    }


    @Override
    protected void onAttachedToWindow() {

        DrawingDao drawingDao = db.drawingDao();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // passing wrong id to the method!!!
                List<Drawing> drawings = drawingDao.getDrawingsForProject(projectId);
                if (!drawings.isEmpty()) {
                    System.out.println("Drawings found");
                    System.out.println("Amount: " + drawings.size());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] drawingData = drawings.get(drawings.size()-1).drawingData;
                            drawingId = drawings.get(drawings.size()-1).id;
                            bitmap = convertByteArrayToBitmap(drawingData);
                            System.out.println("Drawing Id: " + drawingId);
                            invalidate();
                        }
                    });
                }
                else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newDrawing = true;
                        }
                    });
                }
            }
        });

        super.onAttachedToWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas){
        super.onDraw(canvas);

        canvas.save();
        canvas.setMatrix(matrix);

        if (bitmap != null) canvas.drawBitmap(bitmap, 0, 0, null);

        if (grid) drawDynamicGrid(canvas);

        canvas.drawPath(path, paint);

        canvas.drawPath(previewPath, paint);

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
                    } else if (shapeType == ShapeType.DIAMOND) {
                        previewPath.reset();
                        float leftX = startX;
                        float leftY = (startY + currentY)/2;
                        float rightX = currentX;
                        float rightY = (startY + currentY)/2;
                        float topX = (startX + currentX)/2;
                        float topY = startY;
                        float bottomX = (startX + currentX)/2;
                        float bottomY = currentY;
                        previewPath.moveTo(leftX, leftY);
                        previewPath.lineTo(topX, topY);
                        previewPath.lineTo(rightX, rightY);
                        previewPath.lineTo(bottomX, bottomY);
                        previewPath.close();
                    }


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
                    } else if (shapeType == ShapeType.DIAMOND) {
                        float leftX = startX;
                        float leftY = (startY + currentY)/2;
                        float rightX = currentX;
                        float rightY = (startY + currentY)/2;
                        float topX = (startX + currentX)/2;
                        float topY = startY;
                        float bottomX = (startX + currentX)/2;
                        float bottomY = currentY;

                        path.moveTo(leftX, leftY);
                        path.lineTo(topX, topY);
                        path.lineTo(rightX, rightY);
                        path.lineTo(bottomX, bottomY);
                        path.close();
                    }
                    previewPath.reset();


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

    public void resetBeforeSaving(){
        matrix.reset();
        grid = !grid;
        invalidate();
    }

    // need to squeeze canvas so that it fits all the drawings ow at least for regular size zoom
    public void saveDrawingToStorage() {
        // Get the bitmap from the view
        resetBeforeSaving();

        Bitmap drawingBitmap = getBitmapFromView(this);

        // Save the bitmap to external storage
        try {
            byte[] drawingData = convertBitmapToByteArray(drawingBitmap);
            // Insert into database
            if (newDrawing){
                Drawing drawing = new Drawing();
                drawingId = drawing.id;
                drawing.projectId = projectId;
                drawing.drawingData = drawingData;
                new Thread(() -> {
                    db.drawingDao().insertDrawing(drawing);
                }).start();
                System.out.println("Drawing saved to database");
            }
            else {
                new Thread(()-> {
                    Drawing drawing = db.drawingDao().getDrawingById(drawingId);
                    drawing.drawingData = drawingData;
                    db.drawingDao().updateDrawing(drawing);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Drawing updated");
                        }
                    });
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        // Create a bitmap with the same dimensions as the view

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth()+50, view.getHeight()+50, Bitmap.Config.ARGB_8888);
        // Create a canvas to draw on the bitmap
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // Draw the view on the canvas
        view.draw(canvas);
        return bitmap;
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap convertByteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public void saveImageToExternalStorage() throws IOException {
        Bitmap bitmap = getBitmapFromView(this);

        System.out.println("saveImageToExternalStorage()");
        // Get the current time for the file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        Log.d("Saving file: ", imageFileName);

        // Check if the device is running Android Q or higher

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = activity.getContentResolver().openOutputStream(uri)) {
                assert outputStream != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Toast.makeText(activity, "Image saved to gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setProjectId(int projectId){
        this.projectId = projectId;
    }

    public Path getPath(){
        if (path.isEmpty()) System.out.println("empty path");
        return path;
    }
    public void setPath(Path path){
        this.path = path;
    }
}

