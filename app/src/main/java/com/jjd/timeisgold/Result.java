package com.jjd.timeisgold;

//파이어베이스 메소드 라이브러리
import com.google.firebase.components.BuildConfig;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

import com.github.mikephil.charting.charts.*;


public class Result extends AppCompatActivity {

    TextInputLayout textInputLayout;
    AutoCompleteTextView autoCompleteTextView;

    TextView textShowItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage_stats);

        textInputLayout = findViewById(R.id.inputLayout);
        autoCompleteTextView = findViewById(R.id.text_item);
        textShowItem = findViewById(R.id.text_show_item);


        String[] items = {"1 Week", "2 Weeks", "A Month"};
        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(Result.this,
                R.layout.usage_stats_item, items);
        autoCompleteTextView.setAdapter(itemAdapter);


        autoCompleteTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                textShowItem.setText(items[position]);
                showData(items[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
                textShowItem.setText(items[0]);
                showData(items[0]);
            }
        });
    }

    public void showData(String item){

    }

}
