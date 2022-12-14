package com.jjd.timeisgold;

import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "usagedata.db";
    SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd");
    String date = fm1.format(new Date());


    //  DBHelper
    public DBHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    // Table 생성
    @Override
    public void onCreate(SQLiteDatabase db) {




        try {
            String sql = "create table if not exists cell "
                    + "(cell_name varchar(20) not null , usage_time int, datetime date(10) default current_timestamp)";
            db.execSQL(sql) ; // create table if there is no table

        }catch ( Exception e){

        }
    }

    //Table Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cell");
        onCreate(db);
    }


    // Table update
    public void Update(String name) {

        int check_data = 0;

        SQLiteDatabase db = getWritableDatabase();
        String str = "SELECT * FROM cell WHERE cell_name = '" + name + "' AND datetime = date('now', 'localtime')";
        Cursor cursor = db.rawQuery(str, null);


        check_data = cursor.getCount();

        // send appropirate form of data to the graph
        if(check_data > 0){
            db.execSQL("UPDATE cell SET usage_time = usage_time + 3 WHERE cell_name = '" + name + "' AND datetime = date('now', 'localtime')");
        } else {
            db.execSQL("INSERT INTO cell VALUES('" + name + "', '" + 3 + "', date('now', 'localtime'))");
        }

        db.close();
    }

    public String getMost() {

        SQLiteDatabase db = getReadableDatabase();
        String str = "SELECT cell_name, sum(usage_time) FROM cell GROUP BY cell_name ORDER BY sum(usage_time) DESC";
        String result = "";
        int check_data = 0;


        Cursor cursor = db.rawQuery(str, null);

        check_data = cursor.getCount();

        if(check_data > 0) {
            cursor.moveToNext();

            result = cursor.getString(0);
            Log.v("result", result);

            //split to get app name
            String[] array = result.split("[.]");
            int last = array.length - 1;

            Log.v("result split", array[last]);
            result = array[last];
        } else {
            result = "nothing used";
        }

        //앱 이름 리턴
        return result;
    }

    // read last 7days' data
    public ArrayList getWeekResult(int checktype) {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> week_list = new ArrayList<>();
        String result = "";
        String str="";

        Cursor cursor = db.rawQuery("SELECT datetime, sum(usage_time) FROM cell GROUP BY datetime ORDER BY ROWID LIMIT 7", null);

        if (checktype == 1) {
            while (cursor.moveToNext()) {
                result = cursor.getString(0);
                str = result.substring(5);
                Log.v("result:", str);
                week_list.add(str);
            }
        } else {
            while (cursor.moveToNext()) {
                result = cursor.getString(1);
                week_list.add(result);
            }
        }


        return week_list;
    }

}
