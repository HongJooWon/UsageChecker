package com.jjd.timeisgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;


import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {

    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Button button_home = findViewById(R.id.homeButton);
        button_home.setOnClickListener(new  View.OnClickListener(){
           @Override
            public void onClick(View v){
               Intent myIntent = new Intent(GraphActivity.this,MainActivity.class);
               startActivity(myIntent);
               finish();
           }


        });
        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
        barChart = (BarChart) findViewById(R.id.chart);

        BarData barData = new BarData(); // 차트에 담길 데이터

        entry_chart.add(new BarEntry(1, 1)); //entry_chart1에 좌표 데이터를 담는다.
        entry_chart.add(new BarEntry(2, 2));
        entry_chart.add(new BarEntry(3, 3));
        entry_chart.add(new BarEntry(4, 4));
        entry_chart.add(new BarEntry(5, 2));
        entry_chart.add(new BarEntry(6, 8));


        BarDataSet barDataSet = new BarDataSet(entry_chart, "bardataset"); // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환한다.

        barDataSet.setColor(Color.BLUE); // 해당 BarDataSet 색 설정 :: 각 막대 과 관련된 세팅은 여기서 설정한다.

        barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.

        barChart.setData(barData); // 차트에 위의 DataSet 을 넣는다.

        barChart.invalidate(); // 차트 업데이트
        barChart.setTouchEnabled(false); // 차트 터치 불가능하게

    }

    }


