package com.tianxiaolei.flashbanner.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by tianxiaolei on 2017/2/7.
 * Banner 中一个指示点view
 */

public class PointView extends LinearLayout {
    private ImageView normal;
    private ImageView selected;
    public PointView(Context context) {
        this(context,null);
    }

    public PointView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewGroup.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setOrientation(LinearLayout.HORIZONTAL);
        normal = new ImageView(context);
        selected = new ImageView(context);
        selected.setVisibility(GONE);
        this.addView(normal);
        this.addView(selected);
    }
    public void setResource(Drawable normal,Drawable selected){
        this.normal.setImageDrawable(normal);
        this.selected.setImageDrawable(selected);
        invalidate();
    }
    public void isSelected(boolean isSelected){
        if(isSelected){
            selected.setVisibility(VISIBLE);
            normal.setVisibility(GONE);
        }else{
            selected.setVisibility(GONE);
            normal.setVisibility(VISIBLE);
        }
        invalidate();
    }
    public void setPointSpace(int px){
        LayoutParams layoutParams = (LayoutParams) this.getLayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.setMarginEnd(px);
        }else{
            layoutParams.setMargins(0,0,px,0);
        }
    }
}
