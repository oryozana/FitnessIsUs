package com.example.fitnessisus;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ingredients.db";
    private static final int DATABASE_VERSION = 1;

    //הצהרה על שדות הטבלה
    public static final String TABLE_NAME = "all_ingredients";
    public static final String INGREDIENT_NAME = "name";
    public static final String PROTEINS = "proteins";
    public static final String FATS = "fats";
    public static final String CALORIES = "calories";
    public static final String INGREDIENT_PICTURE_ID = "ingredient_picture_id";

    //מחרוזות שבאמצעותן נריץ שאילתות SQL ליצירת טבלה או עדכון ומחיקת טבלה
    String SQL_Create="";
    String SQL_Delete="";

    //בנאי של המחלקה DBHelper
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //בפעולה און קריאייט יוצרים את הטבלה
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        SQL_Create="CREATE TABLE "+TABLE_NAME+" (";
        SQL_Create+=INGREDIENT_NAME+" TEXT, ";
        SQL_Create+=PROTEINS+" REAL, ";
        SQL_Create+=FATS+" REAL, ";
        SQL_Create+=CALORIES+" REAL, ";
        SQL_Create+=INGREDIENT_PICTURE_ID+" INTEGER);";
        sqLiteDatabase.execSQL(SQL_Create);
    }

    //כותבים אבל לא נשתמש שאילתא שתאפשר העברת נתונים הקיימים באפליקציה כשיש עדכון גרסה
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        SQL_Delete = "DROP TABLE IF EXISTS "+ TABLE_NAME;
        sqLiteDatabase.execSQL(SQL_Delete);
        onCreate(sqLiteDatabase);
    }
}
