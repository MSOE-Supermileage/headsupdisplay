package edu.msoe.supermileagehud.UIComponents;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Connor on 10/19/2015.
 */
public class VerticalTempSeekBar extends SeekBar {
    private List<ProgressItem> mProgressItemsList = new ArrayList<>();

    private float totalSpan = 100;

    private float redSpan = 10;
    private float blueSpan = 55;
    private float greenSpan = 35;

    public VerticalTempSeekBar(Context context) {
        super(context);

        initData();
    }

    public VerticalTempSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initData();
    }

    public VerticalTempSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        initData();
    }

    public void initData() {
        ProgressItem mProgressItem;

        mProgressItemsList = new ArrayList<>();
        // red span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (redSpan / totalSpan) * 100;
        mProgressItem.color = Color.parseColor("#F44336");
        mProgressItemsList.add(mProgressItem);
        // green span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (greenSpan / totalSpan) * 100;
        mProgressItem.color = Color.parseColor("#00E676");
        mProgressItemsList.add(mProgressItem);
        // blue span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = ((blueSpan / totalSpan) * 100);
        mProgressItem.color = Color.parseColor("#2196F3");
        mProgressItemsList.add(mProgressItem);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);

        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (mProgressItemsList.size() > 0) {
            int progressBarWidth = getWidth();
            int progressBarHeight = getHeight();

            int thumbOffset = getThumbOffset();
            int lastProgressY = 0;

            int progressItemHeight, progressItemBottom;

            for (int i = 0; i < mProgressItemsList.size(); i++) {
                ProgressItem progressItem = mProgressItemsList.get(i);
                Paint progressPaint = new Paint();

                progressPaint.setColor(progressItem.color);

                progressItemHeight = (int) (progressItem.progressItemPercentage * progressBarHeight / 100);
                progressItemBottom = lastProgressY + progressItemHeight;

                // for last item give right to progress item to the width
                if (i == mProgressItemsList.size() - 1 && progressItemBottom != progressBarWidth) {
                    progressItemBottom = progressBarHeight;
                }

                Rect progressRect = new Rect();
                progressRect.set(thumbOffset / 2, lastProgressY, progressBarWidth - thumbOffset / 2, progressItemBottom);

                canvas.drawRect(progressRect, progressPaint);
                lastProgressY = progressItemBottom;
            }

            canvas.rotate(-90);
            canvas.translate(-getHeight(), 0);

            super.onDraw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
