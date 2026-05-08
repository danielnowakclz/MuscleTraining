package de.daniel_nowak.muscletraining.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.daniel_nowak.muscletraining.R;

public class MuscleBodyView extends View {

    public enum Side { FRONT, BACK }

    private Bitmap frontBitmap;
    private Bitmap backBitmap;

    private RectF dstRect = new RectF();
    private Side currentSide = Side.FRONT;

    private Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float markerRadius = 14f;

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
    }

    public void setSide(Side s) {
        if (currentSide != s) {
            currentSide = s;
            requestLayout();
            forceLayout();
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap bmp = (currentSide == Side.FRONT) ? frontBitmap : backBitmap;

        canvas.drawBitmap(bmp, null, dstRect, null);

        for (Marker m : markers) {

            if (currentSide == Side.FRONT && !"front".equals(m.side)) continue;
            if (currentSide == Side.BACK  && !"back".equals(m.side)) continue;

            float x = dstRect.left + m.xNorm * dstRect.width();
            float y = dstRect.top  + m.yNorm * dstRect.height();

            canvas.drawCircle(x, y, markerRadius, markerPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (e.getAction() == MotionEvent.ACTION_DOWN) {

            float x = e.getX();
            float y = e.getY();

            if (!dstRect.contains(x, y)) return true;

            float nx = (x - dstRect.left) / dstRect.width();
            float ny = (y - dstRect.top)  / dstRect.height();

            String side = (currentSide == Side.FRONT) ? "front" : "back";

            float touchRadius = 0.05f;

            for (int i = 0; i < markers.size(); i++) {
                Marker m = markers.get(i);

                if (!m.side.equals(side)) continue;

                float dx = Math.abs(nx - m.xNorm);
                float dy = Math.abs(ny - m.yNorm);

                if (dx < touchRadius && dy < touchRadius) {
                    markers.remove(i);
                    invalidate();
                    return true;
                }
            }

            markers.add(new Marker(nx, ny, side));
            invalidate();
            return true;
        }

        return super.onTouchEvent(e);
    }
}
