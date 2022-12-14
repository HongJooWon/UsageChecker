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

    //longterm & shortterm database 생성 후 데이터 업로드 시 shortterm 삭제 (일주일 치)

    // DBHelper 생성자
    public DBHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    // Table 생성
    @Override
    public void onCreate(SQLiteDatabase db) {




        try {
            String sql = "create table if not exists cell "
                    + "(cell_name varchar(20) not null , usage_time int, datetime date(10) default current_timestamp)";
            db.execSQL(sql) ; // 테이블 생성

        }catch ( Exception e){

        }
    }

    //Table Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cell");
        onCreate(db);
    }

//    //Table 데이터 입력
//    public void insert(String name) {
//        int check_data = 0;
//        SQLiteDatabase db = getWritableDatabase();
//        String str = "SELECT EXISTS (SELECT * FROM cell WHERE cell_name = '" + name + "' AND datetime = date('now', 'localtime'))";
//        Cursor cursor = db.rawQuery(str, null);
//        check_data = cursor.getInt(0);
//
//        if(check_data >= 1){
//            Update(name);
//        } else {
//            db.execSQL("INSERT INTO cell VALUES('" + name + "', " + 0 + ")");
//        }
//    }

    // Table 데이터 수정
    public void Update(String name) {

        int check_data = 0;

        SQLiteDatabase db = getWritableDatabase();
        String str = "SELECT * FROM cell WHERE cell_name = '" + name + "' AND datetime = date('now', 'localtime')";
        Cursor cursor = db.rawQuery(str, null);


        check_data = cursor.getCount();

        if(check_data > 0){
            db.execSQL("UPDATE cell SET usage_time = usage_time + 3 WHERE cell_name = '" + name + "' AND datetime = date('now', 'localtime')");
        } else {
            db.execSQL("INSERT INTO cell VALUES('" + name + "', '" + 3 + "', date('now', 'localtime'))");
        }

        db.close();
    }

//    // Table 데이터 삭제
//    public void Delete(String name) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("DELETE Person WHERE NAME = '" + name + "'");
//        db.close();
//    }

    public String getMost() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String str = "SELECT cell_name, sum(usage_time) FROM cell GROUP BY cell_name ORDER BY sum(usage_time) DESC";
        String result = "";
        int check_data = 0;

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery(str, null);

        check_data = cursor.getCount();

        if(check_data > 0) {
            cursor.moveToNext();

            result = cursor.getString(0);
            Log.v("result", result);

            //패키지 이름을 split으로 마지막 앱 이름만만
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

    // Table 조회
    public ArrayList getWeekResult(int checktype) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> week_list = new ArrayList<>();
        String result = "";
        String str="";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
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

    public ArrayList getTwoWeekResult(int checktype) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> week_list = new ArrayList<>();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT datetime, sum(usage_time) FROM cell GROUP BY datetime HAVING datetime > date('now', '-14 days')", null);

        if (checktype == 1) {
            while (cursor.moveToNext()) {
                result = cursor.getString(0);
                week_list.add(result);
            }
        } else {
            while (cursor.moveToNext()) {
                result = cursor.getString(1);
                week_list.add(result);
            }
        }


        return week_list;
    }

    public ArrayList getMonthResult(int checktype) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> week_list = new ArrayList<>();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT datetime, sum(usage_time) FROM cell GROUP BY datetime HAVING datetime > date('now', '-30 days')", null);

        if (checktype == 1) {
            while (cursor.moveToNext()) {
                result = cursor.getString(0);
                week_list.add(result);
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
