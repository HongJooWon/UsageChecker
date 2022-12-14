package com.jjd.timeisgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;
import android.widget.TextView;

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
               finish();
           }


        });

        usage_vals = dbHelper.getWeekResult(2);

        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // Arraylist for data
        barChart = (BarChart) findViewById(R.id.chart);

        BarData barData = new BarData(); // Data to be included in the chart

        int parse = 0;

        if(usage_vals.size() > 0){
            for(int i=0; i<usage_vals.size(); i++){
                parse = Integer.parseInt(usage_vals.get(i));
                entry_chart.add(new BarEntry(i, parse));
            }
        }



        BarDataSet barDataSet = new BarDataSet(entry_chart, "bardataset"); // Convert an Arraylist containing data to a BarDataSet.

        barDataSet.setColor(Color.BLUE); // Set the corresponding BarDataSet color: The settings associated with each bar are set here.

        barData.addDataSet(barDataSet); // Put the corresponding BarDataSet into the DataSet to be placed in the chart to be applied.

        barChart.setData(barData); // Put the DataSet above in the chart.

        barChart.invalidate(); // Update Chart
        barChart.setTouchEnabled(false); // unable to touch the chart

        xAxisLabel = dbHelper.getWeekResult(1);

        XAxis xAxis = barChart.getXAxis();
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));

        //getmost usage app

        TextView bottomLine = (TextView)findViewById(R.id.text_mostUse);
        String mostUsage = dbHelper.getMost();
        bottomLine.setText("This week's most Usage app is : "+mostUsage);
    }

}


