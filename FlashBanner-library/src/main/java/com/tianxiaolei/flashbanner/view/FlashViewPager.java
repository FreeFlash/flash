package com.tianxiaolei.flashbanner.view;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by tianxiaolei on 2017/2/7.
 */

public class FlashViewPager extends ViewPager {
    private boolean isAllowUserScroll = true;//允许用户自己滑动
    private AutoPlayDelegate autoPlayDelegate;//自动播放 控制接口
    public FlashViewPager(Context context) {
        super(context);
    }

    public FlashViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 切换到指定索引的页面，主要用于自动轮播
     *
     * @param position
     */
    public void setBannerCurrentItemInternal(int position) {
        Class viewpagerClass = ViewPager.class;
        try {
            Method setCurrentItemInternalMethod = viewpagerClass.getDeclaredMethod("setCurrentItemInternal", int.class, boolean.class, boolean.class);
            setCurrentItemInternalMethod.setAccessible(true);
            setCurrentItemInternalMethod.invoke(this, position, true, true);
            ViewCompat.postInvalidateOnAnimation(this);
        } catch (Exception e) {
        }
    }

    /**
     * 设置ViewPager的滚动速度
     *
     * @param duration page切换的时间长度
     */
    public void setScrollDuration(int duration) {
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            scrollerField.set(this, new FlashScroller(getContext(), duration));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置是否允许用户手指滑动
     *
     * @param isAllowUserScroll
     */
    public void setIsAllowUserScroll(boolean isAllowUserScroll) {
        this.isAllowUserScroll = isAllowUserScroll;
    }

    /**
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isAllowUserScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (isAllowUserScroll) {
            if (autoPlayDelegate != null && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP)) {
                autoPlayDelegate.handleAutoPlayActionUpOrCancel(getXVelocity());
                return false;
            } else {
                return super.onTouchEvent(ev);
            }
        } else {
            return false;
        }
    }

    private float getXVelocity() {
        float xVelocity = 0;
        Class viewpagerClass = ViewPager.class;
        try {
            Field velocityTrackerField = viewpagerClass.getDeclaredField("mVelocityTracker");
            velocityTrackerField.setAccessible(true);
            VelocityTracker velocityTracker = (VelocityTracker) velocityTrackerField.get(this);

            Field activePointerIdField = viewpagerClass.getDeclaredField("mActivePointerId");
            activePointerIdField.setAccessible(true);

            Field maximumVelocityField = viewpagerClass.getDeclaredField("mMaximumVelocity");
            maximumVelocityField.setAccessible(true);
            int maximumVelocity = maximumVelocityField.getInt(this);

            velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
            xVelocity = VelocityTrackerCompat.getXVelocity(velocityTracker, activePointerIdField.getInt(this));
        } catch (Exception e) {
        }
        return xVelocity;
    }

    /**
     * 自动播放控制接口
     */
    public interface AutoPlayDelegate {
        void handleAutoPlayActionUpOrCancel(float xVelocity);
    }

    public void setAutoPlayDelegate(AutoPlayDelegate autoPlayDelegate) {
        autoPlayDelegate = autoPlayDelegate;
    }
}
