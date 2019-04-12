package com.example.bitweather;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class View24h extends LinearLayout {

    public TextView tvTitle;
    public TextView tvTemp;
    public TextView tvPrecipitation;
    public ImageView ivIcon;
    public View vLevel;

    /*
    String title = "";
    String tempStr = "";
    String precipitationStr = "";
    String iconName = "";
    int precipLevel = 0;
*/

    public View24h(Context context, AttributeSet attrs) {
        super(context, attrs);
        tvTitle = findViewById(R.id.tv_24h_title);
        tvTemp = findViewById(R.id.tv_24h_temp);
        tvPrecipitation = findViewById(R.id.tv_24h_precip);
        ivIcon = findViewById(R.id.iv_24h_icon);
        vLevel = findViewById(R.id.v_24h_level);


    }

    /*
    public void setFields(String title, String temp, String precipitation, String iconName, int precipLevel){

        this.title = title;
        this.tempStr = temp;
        this.precipitationStr = precipitation;
        this.iconName = iconName;
        this.precipLevel = precipLevel;

    }
*/

}
