package com.elexlab.mydisk.ui.wiget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CostDashBoard extends View {
    private float storageCost=0.1f;
    private float volumeCost=0.1f;
    private float requestCost=0.1f;
    public CostDashBoard(Context context) {
        super(context);
    }

    public CostDashBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CostDashBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CostDashBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int strokeWidth = 60;
        int ringWidth = width-strokeWidth/2;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);//设置填满
        paint.setStrokeWidth((float) 60.0);
        RectF rectF = new RectF(strokeWidth/2,strokeWidth/2,ringWidth,ringWidth);

        float allCost = storageCost + volumeCost + requestCost;

        float storageAngle = (storageCost/allCost) * 360;
        float volumeAngle = (volumeCost/allCost) * 360;
        float requestAngle = 360 - (storageAngle + volumeAngle);

        paint.setColor(0xFFFC4850);
        canvas.drawArc(rectF,0,storageAngle,false, paint);

        paint.setColor(0xFFF8C822);
        canvas.drawArc(rectF,storageAngle,volumeAngle,false, paint);

        paint.setColor(0xFF00FF90);
        canvas.drawArc(rectF,storageAngle+volumeAngle,requestAngle,false, paint);
    }

    public void setCost(float storageCost, float volumeCost, float requestCost){
        this.storageCost = storageCost;
        this.volumeCost = volumeCost;
        this.requestCost = requestCost;
        invalidate();
    }

    public float getStorageCost() {
        return storageCost;
    }

    public void setStorageCost(float storageCost) {
        this.storageCost = storageCost;
        invalidate();
    }

    public float getVolumeCost() {
        return volumeCost;
    }

    public void setVolumeCost(float volumeCost) {
        this.volumeCost = volumeCost;
        invalidate();
    }

    public float getRequestCost() {
        return requestCost;
    }

    public void setRequestCost(float requestCost) {
        this.requestCost = requestCost;
        invalidate();
    }
}
