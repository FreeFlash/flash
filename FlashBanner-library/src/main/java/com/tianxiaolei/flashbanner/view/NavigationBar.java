package com.tianxiaolei.flashbanner.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianxiaolei on 2017/2/7.
 * 图片轮播的指示点
 */

public class NavigationBar extends LinearLayout {
    private Drawable normalResource;//未选中样式
    private Drawable selectedResource;//选中时样式
    private Context context;
    private List<PointView> points;//指示点集合

    private int pointSpace = 20;//指示点间距

    private LinearLayout.LayoutParams layoutParams;//布局参数
    public NavigationBar(Context context) {
        this(context,null);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.setVisibility(VISIBLE);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setGravity(Gravity.CENTER);
        points = new ArrayList<>();
    }

    /**
     * 设置指示点总数
     * @param count
     */
    public void setCount(int count){
        points.clear();
        this.removeAllViews();
        for (int i = 0; i < count; i++) {
            PointView pointView = createPoint();
            if(i!=count-1){
                pointView.setPointSpace(pointSpace);
            }
            points.add(pointView);
            this.addView(pointView);
        }
    }


    /**
     * 设置指示点图片
     * @param normal
     * @param selected
     */
    public void setResources(Drawable normal,Drawable selected){
        normalResource = normal;
        selectedResource = selected;
    }

    /**
     *设置选中page
     * @param selected
     */
    public void setSelected(int selected){
        for (int i = 0; i < points.size(); i++) {
            points.get(i).isSelected(false);
        }
        if(selected<=points.size()-1){
            points.get(selected).isSelected(true);
        }
    }

    private PointView createPoint(){
        PointView pointView = new PointView(context);
        // TODO: 2017/2/7 添加默认背景
        pointView.setResource(normalResource,selectedResource);
        pointView.isSelected(false);
        return pointView;
    }

    public void setPointSpace(int px){
        this.pointSpace = px;
    }
}
