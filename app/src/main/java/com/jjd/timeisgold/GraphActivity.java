package com.jjd.timeisgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GraphActivity extends AppCompatActivity {

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
    }
}