package de.daniel_nowak.muscletraining.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.daniel_nowak.muscletraining.R;

public class MuscleBodyView extends View {

    public enum Side { FRONT, BACK }

    private final Bitmap frontBitmap;
    private final Bitmap backBitmap;

    private final RectF dstRect = new RectF();
    private Side currentSide = Side.FRONT;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float markerRadius;

    private float scaleFactor = 1f;
    private final float minScale = 1f;
    private final float maxScale = 8f;

    private float translateX = 0f;
    private float translateY = 0f;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    public static class Marker {
        public float xNorm;
        public float yNorm;
        public String side;

        public Marker(float xNorm, float yNorm, String side) {
            this.xNorm = xNorm;
            this.yNorm = yNorm;
            this.side = side;
        }
    }

    private final List<Marker> markers = new ArrayList<>();

    public MuscleBodyView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        frontBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body_front);
        backBitmap  = BitmapFactory.decodeResource(getResources(), R.drawable.body_back);

        markerPaint.setColor(Color.RED);
        markerPaint.setStyle(Paint.Style.FILL);

        markerRadius = 14f * getResources().getDisplayMetrics().density;

        setupGestureDetectors();
    }

    private void setupGestureDetectors() {

        scaleDetector = new ScaleGestureDetector(getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {

                        float scale = detector.getScaleFactor();
                        scaleFactor *= scale;

                        scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));

                        invalidate();
                        return true;
                    }

                });

        gestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {

                        translateX -= dx;
                        translateY -= dy;

                        invalidate();
                        return true;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        handleTap(e.getX(), e.getY());
                        return true;
                    }
                });
    }

    public void setSide(Side s) {
        if (currentSide != s) {
            currentSide = s;
            requestLayout();
            invalidate();
        }
    }

    public void setMarkers(List<Marker> list) {
        markers.clear();
        markers.addAll(list);
        invalidate();
    }

    public List<Marker> getMarkers() {
        return new ArrayList<>(markers);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        Bitmap bmp = (currentSide == Side.FRONT) ? frontBitmap : backBitmap;
        if (bmp == null) return;

        float viewW = w;
        float viewH = h;

        float imgW = bmp.getWidth();
        float imgH = bmp.getHeight();

        float scale = Math.min(viewW / imgW, viewH / imgH);

        float scaledW = imgW * scale;
        float scaledH = imgH * scale;

        float left = (viewW - scaledW) / 2f;
        float top  = (viewH - scaledH) / 2f;

        dstRect.set(left, top, left + scaledW, top + scaledH);

        scaleFactor = 1f;
        translateX = 0f;
        translateY = 0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap bmp = (currentSide == Side.FRONT) ? frontBitmap : backBitmap;
        if (bmp == null) return;

        canvas.save();

        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor, getWidth()/2f, getHeight()/2f);

        canvas.drawBitmap(bmp, null, dstRect, bitmapPaint);

        for (Marker m : markers) {

            if (currentSide == Side.FRONT && !"front".equals(m.side)) continue;
            if (currentSide == Side.BACK  && !"back".equals(m.side)) continue;

            float x = dstRect.left + m.xNorm * dstRect.width();
            float y = dstRect.top  + m.yNorm * dstRect.height();

            canvas.drawCircle(x, y, markerRadius, markerPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        return true;
    }

    private void handleTap(float x, float y) {

        float[] pts = new float[]{x, y};
        Matrix inv = new Matrix();

        Matrix m = new Matrix();
        m.postTranslate(translateX, translateY);
        m.postScale(scaleFactor, scaleFactor, getWidth()/2f, getHeight()/2f);

        if (!m.invert(inv)) return;
        inv.mapPoints(pts);

        float tx = pts[0];
        float ty = pts[1];

        if (!dstRect.contains(tx, ty)) return;

        float nx = (tx - dstRect.left) / dstRect.width();
        float ny = (ty - dstRect.top)  / dstRect.height();

        String side = (currentSide == Side.FRONT) ? "front" : "back";

        float touchRadius = 0.05f;

        for (int i = 0; i < markers.size(); i++) {
            Marker mkr = markers.get(i);

            if (!mkr.side.equals(side)) continue;

            float dx = nx - mkr.xNorm;
            float dy = ny - mkr.yNorm;

            if (dx * dx + dy * dy < touchRadius * touchRadius) {
                markers.remove(i);
                invalidate();
                return;
            }
        }

        markers.add(new Marker(nx, ny, side));
        invalidate();
    }
}
