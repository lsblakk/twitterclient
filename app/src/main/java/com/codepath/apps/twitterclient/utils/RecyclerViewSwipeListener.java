package com.codepath.apps.twitterclient.utils;

/**
 * https://gist.github.com/rogerhu/9e769149f9550c0a6ddb4987b94caee8
 */

import android.support.v7.widget.RecyclerView;

public class RecyclerViewSwipeListener extends RecyclerView.OnFlingListener {

    private static final int SWIPE_VELOCITY_THRESHOLD = 2000;

    boolean mIsScrollingVertically;

    // change swipe listener depending on whether we are scanning items horizontally or vertically
    RecyclerViewSwipeListener(boolean vertical) {
        mIsScrollingVertically = vertical;
    }

    @Override
    public boolean onFling(int velocityX, int velocityY) {
        if (mIsScrollingVertically && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (velocityY < 0) {
                onSwipeDown();
            } else {
                onSwipeUp();
            }
            return true;
        } else if (!mIsScrollingVertically && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (velocityX < 0) {
                onSwipeLeft();
            } else {
                onSwipeRight();
            }
            return true;
        }
        return false;
    }


    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }
}