package de.daniel_nowak.muscletraining.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import de.daniel_nowak.muscletraining.R;
import de.daniel_nowak.muscletraining.model.Muscle;

public class MuscleRegenView extends View {

    public enum Side { FRONT, BACK }

    private final Bitmap frontBitmap;
    private final Bitmap backBitmap;

    private final RectF dstRect = new RectF();
    private Side currentSide = Side.FRONT;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private Map<String, Muscle> muscleMap = new HashMap<>();
    private Map<String, Float> regenMap = new HashMap<>();

    public MuscleRegenView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        frontBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body_front);
        backBitmap  = BitmapFactory.decodeResource(getResources(), R.drawable.body_back);

        paint.setStyle(Paint.Style.FILL);
    }

    public void setSide(Side s) {
        currentSide = s;
        requestLayout();
        invalidate();
    }

    public void setMuscles(Map<String, Muscle> map) {
        muscleMap = map;
        invalidate();
    }

    public void setRegenData(Map<String, Float> map) {
        regenMap = map;
        invalidate();
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap bmp = (currentSide == Side.FRONT) ? frontBitmap : backBitmap;
        if (bmp == null) return;

        canvas.drawBitmap(bmp, null, dstRect, paint);

        for (Muscle m : muscleMap.values()) {

            Float regen = regenMap.get(m.getId());
            if (regen == null || regen >= 100f) continue;

            boolean isFront = currentSide == Side.FRONT;

            for (int i = 0; i < m.posXList.size(); i++) {

                String side = m.sideList.get(i);

                if (isFront && !"front".equals(side)) continue;
                if (!isFront && !"back".equals(side)) continue;

                float x = dstRect.left + m.posXList.get(i) * dstRect.width();
                float y = dstRect.top  + m.posYList.get(i) * dstRect.height();

                paint.setColor(getColorForRegen(regen));
                float radius = getRadiusForRegen(regen);

                canvas.drawCircle(x, y, radius, paint);
            }
        }
    }

    private int getColorForRegen(float r) {
        if (r < 40f) return Color.RED;
        if (r < 70f) return Color.YELLOW;
        return Color.GREEN;
    }

    private float getRadiusForRegen(float r) {
        if (r < 40f) return 26f;
        if (r < 70f) return 18f;
        return 12f;
    }
}
