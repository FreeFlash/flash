package com.tianxiaolei.flashbanner.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tianxiaolei.tianxiaolei.flashbanner.R;
import com.tianxiaolei.flashbanner.view.transformers.BasePageTransformer;
import com.tianxiaolei.flashbanner.view.transformers.util.Transformer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by tianxiaolei on 2017/2/7.
 */

public class FlashBanner extends RelativeLayout implements FlashViewPager.AutoPlayDelegate, ViewPager.OnPageChangeListener {
    private Drawable mPointNoraml;
    private Drawable mPointSelected;
    private Drawable defaultImg;
    private Drawable defaultImgNone;
    private FlashViewPager mViewPager;//图片viewpager
    private NavigationBar navigationBar;//指示点容器

    private static final int VEL_THRESHOLD = 400;//
    private int mPageScrollPosition;//页面滑动下标
    private float mPageScrollPositionOffset;

    private boolean mIsOneImg = false;//是否只有一张图片
    private boolean mIsAutoPlay = true;//是否开启自动轮播
    private boolean mIsAutoPlaying = false;//是否正在播放

    private int mAutoPalyTime = 5000; //自动播放时间
    private AutoSwitchTask mAutoSwitchTask;

    private int mPageChangeDuration = 1000;    //默认图片切换速度为1s

    private List<? extends Object> mModels;//真正资源集合
    private List<View> mViews;    //视图集合

    private boolean mIsAllowUserScroll = true;    //是否允许用户滑动
    private Transformer mTransformer;//翻页效果
    private int mSlideScrollMode = OVER_SCROLL_ALWAYS;//viewpager从最后一张到第一张的动画效果

    private boolean mPointsIsVisible = false;//指示点是否可见
    private boolean isShowIndicatorOnlyOne = false;//只有一个指示点时 是否可见

    private int navigationBarLeftRightPadding;//指示点左右边距
    private int navigationBarBottomPadding;//指示点下边距
    private int pointSpace;//指示点间距

    private FlashBannerAdapter mAdapter;


    private LayoutParams mLayoutParams;
    private RelativeLayout navigationBarContainer;

    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private OnItemClickListener mOnItemClickListener;

    public FlashBanner(Context context) {
        this(context, null);
    }

    public FlashBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlashBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaultAttrs(context);
        initCustomAttrs(context, attrs);
        initView(context);
    }

    private void initDefaultAttrs(Context context) {
        mAutoSwitchTask = new FlashBanner.AutoSwitchTask(this);
        navigationBarLeftRightPadding = dp2px(context, 3);
        navigationBarBottomPadding = dp2px(context, 6);
        pointSpace = dp2px(context,3);
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlashBanner);
        if (typedArray != null) {
            mIsAutoPlay = typedArray.getBoolean(R.styleable.FlashBanner_isAutoPlay, true);
            mAutoPalyTime = typedArray.getInteger(R.styleable.FlashBanner_AutoPlayTime, 5000);
            mPointsIsVisible = typedArray.getBoolean(R.styleable.FlashBanner_pointsVisibility, true);
            mPointPosition = typedArray.getInt(R.styleable.FlashBanner_pointsPosition, CENTER);
            navigationBarLeftRightPadding = typedArray.getDimensionPixelSize(R.styleable.FlashBanner_pointLeftRightPadding, navigationBarLeftRightPadding);
            navigationBarBottomPadding = typedArray.getDimensionPixelSize(R.styleable.FlashBanner_pointTopBottomPadding,navigationBarBottomPadding);
            pointSpace = typedArray.getDimensionPixelSize(R.styleable.FlashBanner_pointSpace,pointSpace);
            defaultImg = typedArray.getDrawable(R.styleable.FlashBanner_defaultImg);
            mPointNoraml = typedArray.getDrawable(R.styleable.FlashBanner_pointNormal);
            mPointSelected = typedArray.getDrawable(R.styleable.FlashBanner_pointSelect);
            isShowIndicatorOnlyOne = typedArray.getBoolean(R.styleable.FlashBanner_isShowIndicatorOnlyOne, isShowIndicatorOnlyOne);
            mPageChangeDuration = typedArray.getInt(R.styleable.FlashBanner_pageChangeDuration, mPageChangeDuration);
            typedArray.recycle();
        }

    }

    private void initView(Context context) {
//        mLayoutParams = (LayoutParams) getLayoutParams();
        navigationBarContainer = new RelativeLayout(context);
        //设置指示点位置
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(navigationBarLeftRightPadding, 0, navigationBarLeftRightPadding, navigationBarBottomPadding);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//       设置指示器布局位置
        if (CENTER == mPointPosition) {
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else if (LEFT == mPointPosition) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (RIGHT == mPointPosition) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        navigationBarContainer.setLayoutParams(layoutParams);

        navigationBar = new NavigationBar(context);
        navigationBar.setResources(mPointNoraml,mPointSelected);
        navigationBar.setPointSpace(pointSpace);
        navigationBarContainer.addView(navigationBar);
        //设置指示器是否可见
        if (navigationBar != null) {
            if (mPointsIsVisible) {
                navigationBar.setVisibility(View.VISIBLE);
            } else {
                navigationBar.setVisibility(View.GONE);
            }
        }
        //设置默认背景
        if(defaultImg!=null){
            this.setBackgroundDrawable(defaultImg);
        }
        defaultImgNone = this.getResources().getDrawable(R.drawable.flashbanner_backgroud);

    }

    /**
     * 设置数据模型和文案，布局资源默认为ImageView
     *
     * @param models 每一页的数据模型集合
     */
    public void setData(List<? extends Object> models) {
        setData(R.layout.flashbanner_item_image, models);
    }

    public void setData(@LayoutRes int layoutResId, List<? extends Object> models) {
        mViews = new ArrayList<>();
        this.setBackgroundDrawable(defaultImgNone);
        if (models.size() == 2) {
            for (int i = 0; i < 4; i++) {
                mViews.add(View.inflate(getContext(), layoutResId, null));
            }
        } else if(models.size()==0){
            if(defaultImg!=null){
                this.setBackgroundDrawable(defaultImg);
                if(mViewPager!=null){
                    removeView(mViewPager);
                }
            }
        }else {
            for (int i = 0; i < models.size(); i++) {
                mViews.add(View.inflate(getContext(), layoutResId, null));
            }
        }
        setData(mViews, models);
    }

    /**
     * 设置bannner数据
     *
     * @param data
     */
    public void setData(List<View> views, List<? extends Object> data) {

        this.mModels = data;
        this.mViews = views;

        if (data.size() <= 1) {
            mIsOneImg = true;
            if (null != navigationBar) {
                navigationBar.setVisibility(View.GONE);
            }
        } else {
            mIsOneImg = false;
            setPointsIsVisible(mPointsIsVisible);
        }
        //初始化ViewPager
        if (data != null && !data.isEmpty())
            initViewPager();
    }

    public void setAdapter(FlashBannerAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    /**
     * 设置指示点是否可见
     *
     * @param isVisible
     */
    public void setPointsIsVisible(boolean isVisible) {
        this.mPointsIsVisible = isVisible;
        if (null != navigationBar) {
            if (isVisible && getRealCount() > 1) {
                navigationBar.setVisibility(View.VISIBLE);
            } else {
                navigationBar.setVisibility(View.GONE);
            }
        }
    }

    //指示点位置
    private int mPointPosition = CENTER;
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;

//    /**
//     * 对应三个位置 CENTER,RIGHT,LEFT
//     *
//     * @param position
//     */
//    public void setPoinstPosition(int position) {
//        // 设置指示器布局位置
//        if (CENTER == position) {
//            mLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        } else if (LEFT == position) {
//            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        } else if (RIGHT == position) {
//            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        }
//    }

    private void initViewPager() {

        //添加ViewPager
        if (mViewPager != null && this.equals(mViewPager.getParent())) {
            removeView(mViewPager);
            mViewPager = null;
        }
        mViewPager = new FlashViewPager(getContext());

        //当图片多于1张时添加指示点
        addPoints();
        //初始化ViewPager
        mViewPager.setAdapter(new FlashPageAdapter());
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOverScrollMode(mSlideScrollMode);
        mViewPager.setIsAllowUserScroll(mIsAllowUserScroll);
        setPageChangeDuration(mPageChangeDuration);
        addView(mViewPager, 0, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        removeView(navigationBarContainer);
        addView(navigationBarContainer);
        //当图片多于1张时开始轮播
        if (!mIsOneImg && mIsAutoPlay) {
            mViewPager.setAutoPlayDelegate(this);
            int zeroItem = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % getRealCount();
            mViewPager.setCurrentItem(zeroItem, false);
            startAutoPlay();
        } else {
            switchToPoint(0);
        }
    }


    //自动播放控制
    @Override
    public void handleAutoPlayActionUpOrCancel(float xVelocity) {
        assert mViewPager != null;
        if (mPageScrollPosition < mViewPager.getCurrentItem()) {
            // 往右滑
            if (xVelocity > VEL_THRESHOLD || (mPageScrollPositionOffset < 0.7f && xVelocity > -VEL_THRESHOLD)) {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition);
            } else {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1);
            }
        } else {
            // 往左滑
            if (xVelocity < -VEL_THRESHOLD || (mPageScrollPositionOffset > 0.3f && xVelocity < VEL_THRESHOLD)) {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1);
            } else {
                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition);
            }
        }
    }



    /**
     * 获取广告页面数量
     *
     * @return
     */
    public int getRealCount() {
        return mModels == null ? 0 : mModels.size();
    }

    public FlashViewPager getViewPager() {
        return mViewPager;
    }


    /**
     * 添加指示点
     */
    private void addPoints() {
        if (navigationBar != null) {
            navigationBar.setCount(getRealCount());
        }
    }

    public void setPointSpace(float dp){
        if(navigationBar!=null){
            navigationBar.setPointSpace(dp2px(getContext(),dp));
        }
    }

    /**
     * 切换指示器
     *
     * @param currentPoint
     */
    private void switchToPoint(final int currentPoint) {
        if (navigationBar != null & mModels != null && getRealCount() > 1) {
            navigationBar.setSelected(currentPoint);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsAutoPlay && !mIsOneImg) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoPlay();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    startAutoPlay();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 设置翻页动画效果
     *
     * @param transformer
     */
    public void setPageTransformer(Transformer transformer) {
        if (transformer != null && mViewPager != null) {
            mTransformer = transformer;
            mViewPager.setPageTransformer(true, BasePageTransformer.getPageTransformer(mTransformer));
        }
    }

    /**
     * 自定义翻页动画效果
     *
     * @param transformer
     */
    public void setCustomPageTransformer(ViewPager.PageTransformer transformer) {
        if (transformer != null && mViewPager != null) {
            mViewPager.setPageTransformer(true, transformer);
        }
    }

    /**
     * 设置ViewPager切换速度
     *
     * @param duration
     */
    public void setPageChangeDuration(int duration) {
        if (mViewPager != null) {
            mViewPager.setScrollDuration(duration);
        }
    }

    /**
     * 开始播放
     */
    public void startAutoPlay() {
        if (mIsAutoPlay && !mIsAutoPlaying) {
            mIsAutoPlaying = true;
            postDelayed(mAutoSwitchTask, mAutoPalyTime);
        }
    }

    /**
     * 停止播放
     */
    public void stopAutoPlay() {
        if (mIsAutoPlay && mIsAutoPlaying) {
            mIsAutoPlaying = false;
            removeCallbacks(mAutoSwitchTask);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (VISIBLE == visibility) {
            startAutoPlay();
        } else if (INVISIBLE == visibility) {
            stopAutoPlay();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoPlay();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoPlay();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPageScrollPosition = position;
        mPageScrollPositionOffset = positionOffset;
        if (null != mOnPageChangeListener)
            mOnPageChangeListener.onPageScrolled(position % getRealCount(), positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        if(getRealCount() != 0){
            position = position % getRealCount();
            switchToPoint(position);
        }
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrollStateChanged(state);
    }

    private static class AutoSwitchTask implements Runnable {
        private final WeakReference<FlashBanner> flashBanner;

        private AutoSwitchTask(FlashBanner flashBanner) {
            this.flashBanner = new WeakReference<>(flashBanner);
        }

        @Override
        public void run() {
            FlashBanner banner = flashBanner.get();
            if (banner != null) {
                banner.switchNextPage();
                banner.postDelayed(banner.mAutoSwitchTask, banner.mAutoPalyTime);
            }
        }
    }

    private void switchNextPage() {
        if (mViewPager != null) {
            int currentItem = mViewPager.getCurrentItem() + 1;
            mViewPager.setCurrentItem(currentItem);
        }
    }

    public void setOnItemClickListener(FlashBanner.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(FlashBanner banner, int position);
    }

    public interface FlashBannerAdapter {
        void loadBanner(FlashBanner banner, View view, int position);
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    public void setResource(Drawable normal, Drawable selected) {
        if (navigationBar != null) {
            navigationBar.setResources(normal, selected);
        }
    }



    /**
     * created by tianxiaolei 2017-2-8
     * 自动轮播适配器（无限滑动）
     */
    private class FlashPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            //当只有一张图片时返回1
            if (mIsOneImg) {
                return 1;
            }
            return mIsAutoPlay ? Integer.MAX_VALUE : getRealCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final int realPosition = position % getRealCount();
            int viewPosition = position%mViews.size();
            View view = null;

            view = mViews.get(viewPosition);
            if (container.equals(view.getParent())) {
                container.removeView(view);
            }

            if (mOnItemClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(FlashBanner.this, realPosition);
                    }
                });
            }

            if (null != mAdapter && mModels.size() != 0) {
                mAdapter.loadBanner(FlashBanner.this, view, realPosition);
            }

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
