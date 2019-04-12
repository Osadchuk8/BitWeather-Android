package com.example.bitweather;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class View6d extends LinearLayout {

    public TextView tvTitle;
    public TextView tvTempHigh;
    public TextView tvTempLow;
    public TextView tvPrecip;
    public ImageView ivIcon;



    public View6d(Context context, AttributeSet attrs) {
        super(context, attrs);
        tvTitle = findViewById(R.id.tv_6day_title);
        tvTempHigh = findViewById(R.id.tv_6day_temp_high);
        tvTempLow = findViewById(R.id.tv_6day_temp_low);
        tvPrecip = findViewById(R.id.tv_6day_precip);
        ivIcon = findViewById(R.id.iv_6day_icon);

    }

    public void setChildViews(View parentView){
        tvTitle = parentView.findViewById(R.id.tv_6day_title);
        tvTempHigh = parentView.findViewById(R.id.tv_6day_temp_high);
        tvTempLow = parentView.findViewById(R.id.tv_6day_temp_low);
        tvPrecip = parentView.findViewById(R.id.tv_6day_precip);
        ivIcon = parentView.findViewById(R.id.iv_6day_icon);
    }


}


