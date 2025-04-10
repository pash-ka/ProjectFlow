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
* add tabs for each drawing? or some other functionality to switch between them +
* !!! problems with matrix transformation when switching between drawings via drawer, coordinates are going crazy!!!  ++
* maybe change the saving to gallery functionality
* !!! eraser doesn't work right, off with the bitmap or modes or smth  ++
* ?? why use oldcontenbitmap, why not override current bitmap?? +
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
    public float lastStrokeWidth = 10f;

    private Path path;
    private final Path previewPath;

    private Bitmap bitmap;

    private final Matrix matrix;
    private final Matrix inverseMatrix;

    private float startX;
    private float startY;

    private ShapeType shapeType;

    private final ScaleGestureDetector scaleGestureDetector;
    private final GestureDetector gestureDetector;

    private final int gridSize = 50; // Grid size in pixels
    public boolean grid = true;
    public boolean hand = false;
    private boolean eraser = false;

    private int projectId;
    private int drawingId;

    private final int toolbarOffset = 128;
    public boolean newDrawing = false;

    private float minX = Float.MAX_VALUE;
    private float maxX = Float.MIN_VALUE;
    private float minY = Float.MAX_VALUE;
    private float maxY = Float.MIN_VALUE;

    private float oldMinX = 0, oldMinY = toolbarOffset;

    private final WhiteBoardActivity activity;

    private final AppDatabase db;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if (hand) {
                float scaleFactor = detector.getScaleFactor();
                scaleCanvas(scaleFactor);
            }
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
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
        float[] matrixValues = new float[9];

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
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Drawing> drawings = drawingDao.getDrawingsForProject(projectId);
            if (!drawings.isEmpty()) {
                System.out.println("Drawings found");
                System.out.println("Amount: " + drawings.size());

                activity.runOnUiThread(() -> {
                    byte[] drawingData = drawings.get(drawings.size()-1).drawingData;
                    drawingId = drawings.get(drawings.size()-1).id;
                    bitmap = convertByteArrayToBitmap(drawingData);
                    setBoundsForLoadedBitmap(bitmap);
                    System.out.println("Drawing Id: " + drawingId);
                    System.out.println("bitmap width: " + bitmap.getWidth());
                    invalidate();
                });
            }
            else {
                activity.runOnUiThread(() -> newDrawing = true);
            }
        });

        super.onAttachedToWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas){
        super.onDraw(canvas);

        canvas.save();
        canvas.setMatrix(matrix);

        if (bitmap != null) canvas.drawBitmap(bitmap, oldMinX, oldMinY, null);
        //if (oldContentBitmap != null) canvas.drawBitmap(oldContentBitmap, oldMinX, oldMinY, null);

        canvas.drawPath(path, paint);

        canvas.drawPath(previewPath, paint);

        if (grid) drawDynamicGrid(canvas);

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

    private float snapToGrid(float coordinate){
        return Math.round(coordinate / gridSize) * gridSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        float[] touchPoint = { event.getX(), event.getY() };
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(touchPoint);
        float currentX = touchPoint[0];
        float currentY = touchPoint[1];

        if (shapeType != ShapeType.NONE && !hand){
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    if (grid){
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
                    if (grid){
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
                        float left = Math.min(startX, currentX);
                        float right = Math.max(startX, currentX);
                        float top = Math.min(startY, currentY);
                        float bottom = Math.max(startY, currentY);

                        previewPath.addRect(left, top, right, bottom, Path.Direction.CW);
                    }
                    else if (shapeType == ShapeType.ROUNDED_RECT){
                        previewPath.reset();
                        float left = Math.min(startX, currentX);
                        float right = Math.max(startX, currentX);
                        float top = Math.min(startY, currentY);
                        float bottom = Math.max(startY, currentY);

                        previewPath.addRoundRect(left, top, right, bottom, 20, 20, Path.Direction.CW);
                    }
                    else if (shapeType == ShapeType.CIRCLE) {
                        previewPath.reset();
                        float radius = (float) Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                        previewPath.addCircle(startX, startY, radius, Path.Direction.CW);
                    }
                    else if (shapeType == ShapeType.DIAMOND) {
                        previewPath.reset();
                        float leftX = startX;
                        float leftY = (startY + currentY)/2;
                        float rightY = (startY + currentY)/2;
                        float topX = (startX + currentX)/2;
                        float topY = startY;
                        float bottomX = (startX + currentX)/2;
                        previewPath.moveTo(leftX, leftY);
                        previewPath.lineTo(topX, topY);
                        previewPath.lineTo(currentX, rightY);
                        previewPath.lineTo(bottomX, currentY);
                        previewPath.close();
                    } else if (shapeType == ShapeType.ARROW) {
                        // TODO: implement arrow drawing
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (grid){
                        currentX = snapToGrid(currentX);
                        currentY = snapToGrid(currentY);
                    }
                    if (shapeType == ShapeType.LINE) {
                        path.moveTo(startX, startY);
                        path.lineTo(currentX, currentY);
                    }
                    else if (shapeType == ShapeType.RECTANGLE) {
                        float left = Math.min(startX, currentX);
                        float right = Math.max(startX, currentX);
                        float top = Math.min(startY, currentY);
                        float bottom = Math.max(startY, currentY);
                        path.addRect(left, top, right, bottom, Path.Direction.CW);
                    }
                    else if (shapeType == ShapeType.ROUNDED_RECT) {
                        float left = Math.min(startX, currentX);
                        float right = Math.max(startX, currentX);
                        float top = Math.min(startY, currentY);
                        float bottom = Math.max(startY, currentY);
                        path.addRoundRect(left, top, right, bottom, 20, 20, Path.Direction.CW);
                    }
                    else if (shapeType == ShapeType.CIRCLE) {
                        float radius = (float) Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                        path.addCircle(startX, startY, radius, Path.Direction.CW);
                        updateBounds(startX-radius, startY-radius);
                        updateBounds(startX+radius, startY+radius);
                    }
                    else if (shapeType == ShapeType.DIAMOND) {
                        float leftX = startX;
                        float leftY = (startY + currentY)/2;
                        float rightY = (startY + currentY)/2;
                        float topX = (startX + currentX)/2;
                        float topY = startY;
                        float bottomX = (startX + currentX)/2;

                        path.moveTo(leftX, leftY);
                        path.lineTo(topX, topY);
                        path.lineTo(currentX, rightY);
                        path.lineTo(bottomX, currentY);
                        path.close();
                    }
                    previewPath.reset();
                    updateBounds(startX, startY);
                    updateBounds(currentX, currentY);
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
            if (!eraser) updateBounds(currentX, currentY);
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

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public void setPaintColor(int color) {
        resetBitmap();
        paint.setColor(color);
    }

    public int getPaintColor(){
        return paint.getColor();
    }

    public void eraserOn(){
        setEraser(true);
        setShapeType(ShapeType.NONE);
        setPaintStrokeWidth(80f);
        paint.setColor(Color.WHITE);
    }

    public void setEraser(boolean er){
        this.eraser = er;
    }

    public boolean getEraser(){
        return this.eraser;
    }

    public void setPaintStrokeWidth(float width) {
        resetBitmap();
        lastStrokeWidth = paint.getStrokeWidth();
        paint.setStrokeWidth(width);
    }

    private void resetBitmap(){
        if (!path.isEmpty()){
            bitmap = getBitmapFromView(this, true);
            oldMinX = minX;
            oldMinY = minY;
            path.reset();
        }
    }

    public void clearCanvas() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle(); // Frees the memory used by the Bitmap
            bitmap = null;    // Clears the reference to the Bitmap object
        }
        matrix.reset();
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

    // need to squeeze canvas so that it fits all the drawings ow at least for regular size zoom  ++
    public void saveDrawingToStorage() {
        // Get the bitmap from the view
        if (bitmap != null || !path.isEmpty()){
            Bitmap drawingBitmap = getBitmapFromView(this, false);
            // Save the bitmap to external storage
            try {
                byte[] drawingData = convertBitmapToByteArray(drawingBitmap);
                // Insert into database
                if (newDrawing){
                    Drawing drawing = new Drawing();
                    drawingId = drawing.id;
                    drawing.projectId = projectId;
                    drawing.drawingData = drawingData;
                    new Thread(() -> db.drawingDao().insertDrawing(drawing)).start();
                    System.out.println("Drawing saved to database");
                }
                else {
                    new Thread(()-> {
                        Drawing drawing = db.drawingDao().getDrawingById(drawingId);
                        drawing.drawingData = drawingData;
                        db.drawingDao().updateDrawing(drawing);
                        activity.runOnUiThread(() -> System.out.println("Drawing updated"));
                    }).start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    private Bitmap getBitmapFromView(View view, boolean reset) {

        boolean temp = grid;
        grid = false;

        // Create a bitmap with the same dimensions as the view
        int bitmapWidth;
        int bitmapHeight;
        Bitmap bm;

        if (!reset) {
            matrix.reset();
            invalidate();
            bitmapWidth = (int) (maxX - minX);
            bitmapHeight = (int) (maxY - minY);

            bm = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);

            // Create a canvas to draw on the bitmap
            Canvas canvas = new Canvas(bm);

            panCanvas(-minX, -minY);
            System.out.println("-minX: " + (-minX) + " -minY: " + (-minY));
            canvas.drawColor(Color.WHITE);

            // Draw the view on the canvas
            view.draw(canvas);
            grid = temp;
            panCanvas(minX, minY);
        }
        else {
            Matrix tempMx = new Matrix();
            tempMx.set(matrix);
            matrix.reset();
            invalidate();
            bitmapWidth = (int) (maxX - minX);
            bitmapHeight = (int) (maxY - minY);

            bm = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);

            // Create a canvas to draw on the bitmap
            Canvas canvas = new Canvas(bm);

            panCanvas(-minX, -minY);
            canvas.drawColor(Color.WHITE);

            // Draw the view on the canvas
            view.draw(canvas);
            grid = temp;
            panCanvas(minX, minY);
            matrix.set(tempMx);
        }
        return bm;
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
        Bitmap bitmap = getBitmapFromView(this, false);

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
            }
        }
        panCanvas(minX, minY);
    }

    public void setProjectId(int projectId){
        this.projectId = projectId;
    }

    public void setDrawingId(int drawingId){
        this.drawingId = drawingId;
    }

    public Path getPath(){
        if (path.isEmpty()) System.out.println("empty path");
        return path;
    }

    public void setPath(Path path){
        this.path.set(path);
    }

    public void setBitmap(Bitmap bitmap){
        if (bitmap != null){
            this.bitmap = bitmap;
            setBoundsForLoadedBitmap(bitmap);
        }
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    private void updateBounds(float x, float y) {
        int offset = 50;

        if (x < minX) minX = x - offset;

        if (x > maxX) maxX = x + offset;

        if (y < minY) minY = y - offset;

        if (y > maxY) maxY = y + offset;
    }

    public void resetBounds(){
        minX = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        minY = Float.MAX_VALUE;
        maxY = Float.MIN_VALUE;
    }

    private void setBoundsForLoadedBitmap(Bitmap bitmap){
        minX = 0;
        minY = toolbarOffset;
        maxX = bitmap.getWidth();
        maxY = bitmap.getHeight()+toolbarOffset;
    }
}

