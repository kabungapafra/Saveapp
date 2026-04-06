package com.example.save.ui.views;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChartRenderer extends BarChartRenderer {

    private final float mRadius;

    public RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, float radius) {
        super(chart, animator, viewPortHandler);
        mRadius = radius;
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        if (!shouldDrawValues(dataSet)) return;

        com.github.mikephil.charting.utils.Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
        
        BarBuffer buffer = mBarBuffers[index];
        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        buffer.setPhases(phaseX, phaseY);
        buffer.feed(dataSet);

        trans.pointValuesToPixel(buffer.buffer);

        for (int j = 0; j < buffer.size(); j += 4) {
            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) continue;
            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break;

            mRenderPaint.setColor(dataSet.getColor(j / 4));

            // Custom drawing with rounded corners
            float left = buffer.buffer[j];
            float top = buffer.buffer[j + 1];
            float right = buffer.buffer[j + 2];
            float bottom = buffer.buffer[j + 3];

            // Only draw tops if the bar has a positive value
            if (top < bottom) {
                RectF rect = new RectF(left, top, right, bottom);
                Path path = new Path();
                float[] radii = {mRadius, mRadius, mRadius, mRadius, 0, 0, 0, 0}; // Top-left, Top-right, Bottom-right, Bottom-left
                path.addRoundRect(rect, radii, Path.Direction.CW);
                c.drawPath(path, mRenderPaint);
            } else {
                c.drawRect(left, top, right, bottom, mRenderPaint);
            }
        }
    }

    private boolean shouldDrawValues(IBarDataSet set) {
        return set.isVisible() && (set.getEntryCount() > 0);
    }
}
