package edu.msoe.supermileagehud.UIComponents;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Connor on 10/19/2015.
 */
public class TempSeekBar extends SeekBar
{
    private List<ProgressItem> mProgressItemsList = new ArrayList<>();

    private float totalSpan = 100;

    private float redSpan = 10;
    private float blueSpan = 55;
    private float greenSpan = 35;

    public TempSeekBar(Context context)
    {
        super(context);

        initData();
    }

    public TempSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initData();
    }

    public TempSeekBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        initData();
    }

    public void initData()
    {
        ProgressItem mProgressItem;

        mProgressItemsList = new ArrayList<>();
        // blue span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = ((blueSpan / totalSpan) * 100);
        mProgressItem.color = Color.parseColor("#2196F3");
        mProgressItemsList.add(mProgressItem);
        // green span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (greenSpan / totalSpan) * 100;
        mProgressItem.color = Color.parseColor("#00E676");
        mProgressItemsList.add(mProgressItem);
        // red span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (redSpan / totalSpan) * 100;
        mProgressItem.color = Color.parseColor("#F44336");
        mProgressItemsList.add(mProgressItem);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return false;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas)
    {
        if (mProgressItemsList.size() > 0)
        {
            int progressBarWidth = getWidth();
            int progressBarHeight = getHeight();

            int thumbOffset = getThumbOffset();
            int lastProgressX = 0;

            int progressItemWidth, progressItemRight;

            for (int i = 0; i < mProgressItemsList.size(); i++)
            {
                ProgressItem progressItem = mProgressItemsList.get(i);
                Paint progressPaint = new Paint();

                progressPaint.setColor(progressItem.color);

                progressItemWidth = (int) (progressItem.progressItemPercentage * progressBarWidth / 100);
                progressItemRight = lastProgressX + progressItemWidth;

                // for last item give right to progress item to the width
                if (i == mProgressItemsList.size() - 1 && progressItemRight != progressBarWidth)
                {
                    progressItemRight = progressBarWidth;
                }

                Rect progressRect = new Rect();
                progressRect.set(lastProgressX, thumbOffset / 2, progressItemRight, progressBarHeight - thumbOffset / 2);

                canvas.drawRect(progressRect, progressPaint);
                lastProgressX = progressItemRight;
            }

            super.onDraw(canvas);
        }
    }
}
