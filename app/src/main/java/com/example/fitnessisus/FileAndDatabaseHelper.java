package com.example.fitnessisus;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileAndDatabaseHelper {
    private static SQLiteDatabase sqdb;
    private static DBHelper my_db;
    private Context context;
    private Intent me;

    private FileOutputStream fos;
    private OutputStreamWriter osw;
    private BufferedWriter bw;

    public FileAndDatabaseHelper(Context context, Intent me){
        FileAndDatabaseHelper.my_db = new DBHelper(context);
        this.context = context;
        this.me = me;
    }

    public FileAndDatabaseHelper(Context context){
        FileAndDatabaseHelper.my_db = new DBHelper(context);
        this.context = context;
        this.me = null;
    }

    public String getFileData(String fileName){
        String currentLine = "", allData = "";
        try{
            FileInputStream is = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            currentLine = br.readLine();
            while(currentLine != null){
                allData += currentLine + "\n";
                currentLine = br.readLine();
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return allData;
    }

//    public Song implementSettingsData(){
//        Song activeSong = Song.getActiveSong();
//        if(getFileData("settings") != null){
//            String[] settingsParts = getFileData("settings").split("\n");
//            Boolean playMusic, useVideos, useManuallySave;
//
//            playMusic = Boolean.parseBoolean(settingsParts[0].split(": ")[1]);
//            useVideos = Boolean.parseBoolean(settingsParts[1].split(": ")[1]);
//            useManuallySave = Boolean.parseBoolean(settingsParts[2].split(": ")[1]);
//            activeSong = Song.getSongByName(settingsParts[3].split(": ")[1]);
//
//            me.putExtra("playMusic", playMusic);
//            me.putExtra("useVideos", useVideos);
//            me.putExtra("useManuallySave", useManuallySave);
//            me.putExtra("activeSong", activeSong);
//            return activeSong;
//        }
//        return activeSong;
//    }
//

    public void setPrimaryUser(User user){
        User.setPrimaryUser(user);

        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("Username: ", user.getUsername()).apply();
        sharedPreferences.edit().putString("Password: ", user.getPassword()).apply();
    }

    public String[] getPrimaryUserInfo(){
        String[] info = new String[]{"", ""};
        if(checkIfPrimaryUserExist()){
            SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
            info[0] = sharedPreferences.getString("Username: ", "");
            info[1] = sharedPreferences.getString("Password: ", "");
        }
        return info;
    }

    public boolean checkIfPrimaryUserExist(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        return sharedPreferences.contains("Username: ") && sharedPreferences.contains("Password: ");
    }

    public void removePrimaryUser() {
        User.setPrimaryUser(null);

        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    public void addIngredientIntoLocalDatabase(Ingredient ingredient){
        ContentValues cv = new ContentValues();

        cv.put(my_db.INGREDIENT_NAME, ingredient.getName());
        cv.put(my_db.PROTEINS, ingredient.getProteins());
        cv.put(my_db.FATS, ingredient.getFats());
        cv.put(my_db.CALORIES, ingredient.getCalories());
        cv.put(my_db.INGREDIENT_PICTURE_ID, ingredient.getImgId());

        sqdb = my_db.getWritableDatabase();
        sqdb.insert(my_db.TABLE_NAME, null, cv);
        sqdb.close();
    }

    public static Ingredient getIngredientByName(String name) {
        sqdb = my_db.getWritableDatabase();

        Cursor c = sqdb.query(DBHelper.TABLE_NAME,null, null, null, null, null, null);

        int col1 = c.getColumnIndex(DBHelper.INGREDIENT_NAME);
        int col2 = c.getColumnIndex(DBHelper.PROTEINS);
        int col3 = c.getColumnIndex(DBHelper.FATS);
        int col4 = c.getColumnIndex(DBHelper.CALORIES);
        int col5 = c.getColumnIndex(DBHelper.INGREDIENT_PICTURE_ID);

        c.moveToFirst();

        Ingredient ingredient = new Ingredient(name, -1, -1, -1, -1);
        boolean found = false;

        while(!c.isAfterLast() && !found) {
            String t1 = c.getString(col1);

            if(t1.equals(name)){
                double t2 = c.getDouble(col2);
                double t3 = c.getDouble(col3);
                double t4 = c.getDouble(col4);
                int t5 = c.getInt(col5);

                ingredient = new Ingredient(t1, 1, t2, t3, t4, t5);  // Info based on 1 gram.
                found = true;
            }

            c.moveToNext();
        }

        c.close();
        sqdb.close();
        return ingredient;
    }

    public static ArrayList<Ingredient> getAllOfTheIngredients() {
        sqdb = my_db.getWritableDatabase();

        Cursor c = sqdb.query(DBHelper.TABLE_NAME,null, null, null, null, null, null);

        int col1 = c.getColumnIndex(DBHelper.INGREDIENT_NAME);
        int col2 = c.getColumnIndex(DBHelper.PROTEINS);
        int col3 = c.getColumnIndex(DBHelper.FATS);
        int col4 = c.getColumnIndex(DBHelper.CALORIES);
        int col5 = c.getColumnIndex(DBHelper.INGREDIENT_PICTURE_ID);

        c.moveToFirst();

        ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
        while(!c.isAfterLast()) {
            String t1 = c.getString(col1);
            double t2 = c.getDouble(col2);
            double t3 = c.getDouble(col3);
            double t4 = c.getDouble(col4);
            int t5 = c.getInt(col5);

            ingredients.add(new Ingredient(t1, t2, t3, t4, t5));
            c.moveToNext();
        }

        c.close();
        sqdb.close();
        return ingredients;
    }

    public boolean isDatabaseEmpty() {
        sqdb = my_db.getWritableDatabase();
        boolean flag = true;

        Cursor c = sqdb.query(DBHelper.TABLE_NAME,null, null, null, null, null, null);
        c.moveToFirst();

        if(!c.isAfterLast())
            flag = false;

        c.close();
        sqdb.close();

        return flag;
    }
}
