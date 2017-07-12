package com.tianxiaolei.flashbanner.view;

import android.content.Context;
import android.widget.Scroller;

/**
 * Created by tianxiaolei on 2017/2/8.
 */

public class FlashScroller extends Scroller {

    private int mDuration = 800;//值越大，滑动越慢

    public FlashScroller(Context context, int duration) {
        super(context);
        mDuration = duration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
