package com.jjd.timeisgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;


import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {

    DBHelper dbHelper;

    private BarChart barChart;

    ArrayList<String> xAxisLabel = new ArrayList<>();
    ArrayList<String> usage_vals = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        dbHelper = new DBHelper(GraphActivity.this, 1);

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

        usage_vals = dbHelper.getWeekResult(2);

        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
        barChart = (BarChart) findViewById(R.id.chart);

        BarData barData = new BarData(); // 차트에 담길 데이터

        int parse = 0;

        if(usage_vals.size() > 0){
            for(int i=0; i<usage_vals.size(); i++){
                parse = Integer.parseInt(usage_vals.get(i));
                entry_chart.add(new BarEntry(i, parse));
            }
        }

//        entry_chart.add(new BarEntry(1, 1)); //entry_chart1에 좌표 데이터를 담는다.
//        entry_chart.add(new BarEntry(2, 2));
//        entry_chart.add(new BarEntry(3, 3));
//        entry_chart.add(new BarEntry(4, 4));
//        entry_chart.add(new BarEntry(5, 2));
//        entry_chart.add(new BarEntry(6, 8));
//        entry_chart.add(new BarEntry(7, 8));


        BarDataSet barDataSet = new BarDataSet(entry_chart, "bardataset"); // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환한다.

        barDataSet.setColor(Color.BLUE); // 해당 BarDataSet 색 설정 :: 각 막대 과 관련된 세팅은 여기서 설정한다.

        barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.

        barChart.setData(barData); // 차트에 위의 DataSet 을 넣는다.

        barChart.invalidate(); // 차트 업데이트
        barChart.setTouchEnabled(false); // 차트 터치 불가능하게

        xAxisLabel = dbHelper.getWeekResult(1);

        XAxis xAxis = barChart.getXAxis();
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));

    }

    }


