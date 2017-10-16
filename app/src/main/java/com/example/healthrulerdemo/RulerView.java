package com.example.healthrulerdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by atom on 2017/10/13.
 * 仿薄荷健康的卷尺View
 */

public class RulerView extends View {
    private static final String TAG = "RulerView";
    /**
     * 测速工具
     */
    private VelocityTracker mVelocityTracker;
    /**
     * 辅助滑动工具
     */
    private Scroller mScroller;
    private Paint linePaint, textPaint;

    private final int smallLine = 40, longLine = 80, centerLine = 100;
    public static final int space = 20;

    /**
     * 记录手指位置
     */
    private float mLastX;

    public RulerView(Context context) {
        super(context);
        init(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        linePaint = new Paint();
        textPaint = new Paint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);
        mVelocityTracker.computeCurrentVelocity(1000);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) mScroller.forceFinished(true);
                mLastX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                int scrollX = (int) (mLastX - event.getX());
                if (getScrollX() <= -getWidth() / 2 && scrollX < 0) {
                    scrollTo(-getWidth() / 2, 0);
                    break;
                } else if (scrollX > 0 && getScrollX() >= 1000 * space - getWidth() / 2) {
                    scrollTo(1000 * space - getWidth() / 2, 0);
                    break;
                }
                scrollBy(scrollX, 0);
                mLastX = event.getX();
                Log.e(TAG, "onTouchEvent: getScrollX():" + getScrollX());
                Log.e(TAG, "onTouchEvent: scrollX:" + scrollX);
                break;
            case MotionEvent.ACTION_UP:
                mScroller.fling(getScrollX(), 0, (int) (-mVelocityTracker.getXVelocity()), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                Log.e(TAG, "onTouchEvent: mVelocityTracker:" + mVelocityTracker.getXVelocity());
                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                break;
        }
        return super.onTouchEvent(event);
    }

    //释放VelocityTracker
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(32);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int mWidth = getWidth();
        int mHeight = getHeight();
        linePaint.setColor(getResources().getColor(R.color.gray));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4);
        for (int i = 0; i < 1000; i++) {
            //画横线
            canvas.drawLine(i * space, mHeight / 2 - centerLine, (i + 1) * space, mHeight / 2 - centerLine, linePaint);
            //画竖线
            if (i % 10 == 0) {
                canvas.drawLine(i * space, mHeight / 2 - centerLine, i * space, mHeight / 2 - (centerLine - longLine), linePaint);
                canvas.drawText(i / 10 + "", i * space - textPaint.measureText(i / 10 + "") / 2, mHeight / 2 + 30, textPaint);
            } else
                canvas.drawLine(i * space, mHeight / 2 - centerLine, i * space, mHeight / 2 - (centerLine - smallLine), linePaint);
        }

        //画中间的绿线，始终位于屏幕中间
        linePaint.setColor(getResources().getColor(R.color.green));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(mWidth / 2 + getScrollX(), mHeight / 2 - centerLine, mWidth / 2 + getScrollX(), mHeight / 2, linePaint);

        //画中间的字，始终位于屏幕中间
        textPaint.setColor(getResources().getColor(R.color.green));
        textPaint.setTextSize(64);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        //当前值
        String currentNum = (getScrollX() + mWidth / 2) / (space * 10) + "." + ((getScrollX() + mWidth / 2) / space) % 10;
        //计算文字所在矩形，可以得到宽高
        Rect rect = new Rect();
        textPaint.getTextBounds(currentNum, 0, currentNum.length(), rect);
        int textWidth = rect.width();
        int textHeight = rect.height();
        canvas.drawText(currentNum, mWidth / 2 + getScrollX() - textWidth / 2, (mHeight / 2 - centerLine) / 2, textPaint);

        //画"kg"
        textPaint.setTextSize(32);
        canvas.drawText("kg", mWidth / 2 + getScrollX() + textWidth / 2 + space, (mHeight / 2 - centerLine) / 2 - textHeight / 2, textPaint);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            //边界控制
            if (mScroller.getCurrX() <= -getWidth() / 2) {
                scrollTo(-getWidth() / 2, 0);
            } else if (mScroller.getCurrX() >= 1000 * space - getWidth() / 2) {
                scrollTo(1000 * space - getWidth() / 2, 0);
            } else {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                invalidate();
            }
        }
    }
}
