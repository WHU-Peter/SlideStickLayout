package com.panfei.dragdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.panfei.library.SlideStickLayout;

public class MainActivity extends AppCompatActivity {

    private SlideStickLayout mSlideStickLayout;
    private ScrollView mFirst;
    private ScrollView mSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSlideStickLayout = (SlideStickLayout)findViewById(R.id.slide_stick_layout);
        mFirst = (ScrollView) findViewById(R.id.first);
        mSecond = (ScrollView) findViewById(R.id.second);

        mSlideStickLayout.setPageListener(new SlideStickLayout.OnSlideDetailPageListener() {
            @Override
            public void onPageExchangeStart(int page) {

            }

            @Override
            public void onPageExchangeFinished(int page) {
//                if (page == 0) {
//                    mFirst.setText("上拉，查看下一页");
//                    mSecond.setText("");
//                }else if (page == 1) {
//                    mFirst.setText("");
//                    mSecond.setText("下拉，查看前一页");
//                }
            }
        });
    }
}
