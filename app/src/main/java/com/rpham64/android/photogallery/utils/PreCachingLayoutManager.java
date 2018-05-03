package com.rpham64.android.photogallery.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Rudolf on 10/20/2016.
 */

public class PreCachingLayoutManager extends GridLayoutManager {

    private static final int DEFAULT_EXTRA_LAYOUT_SPACE = 600;

    private Context mContext;
    private int mExtraLayoutSpace = -1;

    public PreCachingLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public PreCachingLayoutManager(Context context, int spanCount, int extraLayoutSpace) {
        super(context, spanCount);
        mContext = context;
        mExtraLayoutSpace = extraLayoutSpace;
    }

    public PreCachingLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreCachingLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.mExtraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (mExtraLayoutSpace > 0) {
            return mExtraLayoutSpace;
        }
        return DEFAULT_EXTRA_LAYOUT_SPACE;
    }

    /**
     * Enables smooth scrolling to a specified position in RecyclerView.
     *
     * @param recyclerView The RecyclerView using this layout manager.
     * @param state The RecyclerView's current state.
     * @param position Position in RecyclerView to smooth scroll to.
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(mContext);
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }
}
