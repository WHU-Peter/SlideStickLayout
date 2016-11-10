package com.panfei.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by panfei on 2016/11/10.
 */
public class SlideStickLayout extends ViewGroup {
    /**
     * 配置项
     */
    // 达到翻页的比例阈值
    public static float RATE_TO_PAGE = 5;
    // 翻页动画的时间
    public static int PAGE_DURATION = 400;

    // 第一页的子View
    private View mFirst;
    // 第二页的子View
    private View mSecond;
    private View mCurrentView;

    private Scroller mScroller;
    // 上一次mScroller的获取值
    private int mScrollerY;

    //上一次点击时间的x坐标
    private float mLastX;
    //上一次点击时间的y坐标
    private float mLastY;

    private OnSlideDetailPageListener mPageListener;

    public SlideStickLayout(Context context) {
        this(context, null);
    }

    public SlideStickLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideStickLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SlideStickLayout);
            RATE_TO_PAGE = a.getDimensionPixelSize(R.styleable.SlideStickLayout_rateToPage, 5);
            PAGE_DURATION = a.getDimensionPixelSize(R.styleable.SlideStickLayout_pageDuration, 400);
            a.recycle();
        }

        mScroller = new Scroller(getContext());
    }

    protected View createFirstView(){
        return null;
    }

    protected View createSecondView(){
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildWithMargins(mFirst, widthMeasureSpec, 0, heightMeasureSpec, 0);
        measureChildWithMargins(mSecond, widthMeasureSpec, 0, heightMeasureSpec, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mFirst != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mFirst.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + mFirst.getTop();
            final int right = left + mFirst.getMeasuredWidth();
            final int bottom = top + mFirst.getMeasuredHeight();
            mFirst.layout(left, top, right, bottom);
        }
        if (mSecond != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mSecond.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = mFirst.getBottom() + getPaddingBottom() + getPaddingTop() + lp.topMargin;
            final int right = left + mSecond.getMeasuredWidth();
            final int bottom = top + mSecond.getMeasuredHeight();
            mSecond.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            mFirst = createFirstView();
            addView(mFirst);
            mSecond = createSecondView();
            addView(mSecond);
        }else {
            mFirst = getChildAt(0);
            mSecond = getChildAt(1);
        }

        mCurrentView = mFirst;
        if (mPageListener != null) {
            mPageListener.onPageExchangeFinished(0);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mFirst.offsetTopAndBottom(mScroller.getFinalY() - mScrollerY);
                    mSecond.offsetTopAndBottom(mScroller.getFinalY() - mScrollerY);
                    mScroller.forceFinished(true);
                    computeScroll();
                }

                mLastX = ev.getX();
                mLastY = ev.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                float offsetX = ev.getX() - mLastX;
                float offsetY = ev.getY() - mLastY;
                mLastX = ev.getX();
                mLastY = ev.getY();
                if (Math.abs(offsetX) > Math.abs(offsetY)) {
                    return false;
                }

                // 第一页滑动到最下方才会拦截事件
                if (mCurrentView == mFirst && offsetY < 0 && !mFirst.canScrollVertically(1)) {
                    return true;
                }

                // 第二页滑动到最上方才会拦截事件
                if (mCurrentView == mSecond && offsetY > 0 && !mSecond.canScrollVertically(-1)) {
                    return true;
                }

                return false;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLastX = ev.getX();
                mLastY = ev.getY();

                // 当控件发生位移才会拦截up、cancel事件
                if (mFirst.getTop() != 0 && mSecond.getTop() != 0) {
                    return true;
                }else {
                    return false;
                }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mFirst.offsetTopAndBottom((int)(event.getY() - mLastY));
                mSecond.offsetTopAndBottom((int)(event.getY() - mLastY));
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mCurrentView == mFirst) {
                    if (mSecond.getTop() < mFirst.getMeasuredHeight() * (RATE_TO_PAGE - 1) / RATE_TO_PAGE) {
                        mScroller.startScroll(0, 0, 0, -(mSecond.getTop() - getPaddingTop()), PAGE_DURATION);
                        invalidate();
                        mCurrentView = mSecond;
                        if (mPageListener != null) {
                            mPageListener.onPageExchangeStart(1);
                        }
                    }else {
                        mScroller.startScroll(0, 0, 0, getMeasuredHeight()- mSecond.getTop() + getPaddingTop(), PAGE_DURATION);
                        invalidate();
                    }
                }else if (mCurrentView == mSecond) {
                    if (mSecond.getTop() > mFirst.getMeasuredHeight() / RATE_TO_PAGE) {
                        mScroller.startScroll(0, 0, 0, getMeasuredHeight() - mSecond.getTop() + getPaddingTop(), PAGE_DURATION);
                        invalidate();
                        mCurrentView = mFirst;
                        if (mPageListener != null) {
                            mPageListener.onPageExchangeStart(0);
                        }
                    }else {
                        mScroller.startScroll(0, 0, 0, -mSecond.getTop() + getPaddingTop(), PAGE_DURATION);
                        invalidate();
                    }
                }
        }

        mLastX = event.getX();
        mLastY = event.getY();
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mFirst.offsetTopAndBottom(mScroller.getCurrY() - mScrollerY);
            mSecond.offsetTopAndBottom(mScroller.getCurrY() - mScrollerY);
            mScrollerY = mScroller.getCurrY();
            postInvalidate();
        }else if (mScrollerY != 0){
            mScrollerY = 0;
            if (mPageListener != null) {
                mPageListener.onPageExchangeFinished(mCurrentView == mFirst ? 0 : 1);
            }
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public void setPageListener(OnSlideDetailPageListener pageListener) {
        this.mPageListener = pageListener;
        mPageListener.onPageExchangeFinished(mCurrentView == mFirst ? 0 : 1);
    }

    public interface OnSlideDetailPageListener{

        void onPageExchangeStart(int page);

        void onPageExchangeFinished(int page);
    }

    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
