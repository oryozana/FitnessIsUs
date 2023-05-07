package com.example.fitnessisus;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class FileAndDatabaseHelper {
    private static SQLiteDatabase sqdb;
    private static DBHelper my_db;
    private final Context context;
    private final Intent me;

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
//            boolean playMusic, useVideos, useManuallySave;
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

    public Song implementSettingsData(){
        if(isSettingsExist() && me != null){
            me.putExtra("playMusic", getPlayMusicStatus());
            me.putExtra("useVideos", getUseVideosStatus());
            me.putExtra("sendNotifications", getSendNotificationsStatus());
            me.putExtra("showDigitalClock", getShowDigitalClockStatus());
            me.putExtra("activeSong", getCurrentActiveSong());
            me.putExtra("shuffle", getShuffleStatus());

            return getCurrentActiveSong();
        }
        return Song.getActiveSong();
    }

    public void firstInitiateSettings(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        sharedPreferences.edit().putBoolean("PlayMusic ?: ", true).apply();
        sharedPreferences.edit().putBoolean("UseVideos ?: ", true).apply();
        sharedPreferences.edit().putBoolean("SendNotifications ?: ", true).apply();
        sharedPreferences.edit().putBoolean("ShowDigitalClock ?: ", true).apply();
        sharedPreferences.edit().putString("ActiveSongName: ", Song.getSongs().get(0).getName()).apply();
        sharedPreferences.edit().putBoolean("Shuffle ?: ", false).apply();
    }

    public void updateAppSettings(boolean[] newSettings){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        sharedPreferences.edit().putBoolean("PlayMusic ?: ", newSettings[0]).apply();
        sharedPreferences.edit().putBoolean("UseVideos ?: ", newSettings[1]).apply();
        sharedPreferences.edit().putBoolean("SendNotifications ?: ", newSettings[2]).apply();
        sharedPreferences.edit().putBoolean("ShowDigitalClock ?: ", newSettings[3]).apply();
    }

    public void updateSongSettings(String activeSongName, boolean shuffle){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("ActiveSongName: ", activeSongName).apply();
        sharedPreferences.edit().putBoolean("Shuffle ?: ", shuffle).apply();
    }

    public boolean isSettingsExist(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.contains("PlayMusic ?: ");
    }

    public void saveRandomizeSongName(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("ActiveSongName: ", Song.getSongs().get(((int)(Math.random() * Song.getSongs().size()))).getName()).apply();
    }

    public boolean getPlayMusicStatus(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("PlayMusic ?: ", true);
    }

    public boolean getUseVideosStatus(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("UseVideos ?: ", true);
    }

    public boolean setSendNotificationsStatus(boolean status){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("SendNotifications ?: ", status);
    }

    public boolean getSendNotificationsStatus(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("SendNotifications ?: ", true);
    }

    public boolean getShowDigitalClockStatus(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ShowDigitalClock ?: ", true);
    }

    public Song getActiveSongAndShuffleIfNeedTo(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        if(getShuffleStatus())
            saveRandomizeSongName();

        String songName = sharedPreferences.getString("ActiveSongName: ", Song.getSongs().get(0).getName());
        Song tmp = Song.getSongByName(songName);

        if(tmp != null)
            tmp.playSong();
        else
            return Song.getSongs().get(0);

        return tmp;
    }

    public Song getCurrentActiveSong(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        String songName = sharedPreferences.getString("ActiveSongName: ", Song.getSongs().get(0).getName());
        Song tmp = Song.getSongByName(songName);

        if(tmp != null)
            tmp.playSong();
        else
            return Song.getSongs().get(0);

        return tmp;
    }

    public boolean getShuffleStatus(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("Shuffle ?: ", false);
    }

    public void updateWeatherConditions(String cityName, String forecast, String temperature, String updatedAt, String dateOfUpdate){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("CityName: ", cityName).apply();
        sharedPreferences.edit().putString("Forecast: ", forecast).apply();
        sharedPreferences.edit().putString("Temperature: ", temperature).apply();
        sharedPreferences.edit().putString("UpdatedAt: ", updatedAt).apply();
        sharedPreferences.edit().putString("DateOfUpdate: ", dateOfUpdate).apply();
    }

    public void updateCityName(String cityName){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("CityName: ", cityName).apply();
        sharedPreferences.edit().putString("Forecast: ", "").apply();
        sharedPreferences.edit().putString("Temperature: ", "").apply();
        sharedPreferences.edit().putString("UpdatedAt: ", "").apply();
        sharedPreferences.edit().putString("DateOfUpdate: ", "").apply();
    }

    public boolean isNeedToUpdateWeather(){
        LocalDateTime updatedAt = LocalDateTime.parse(String.valueOf(getDateOfUpdate()));
        updatedAt.plusHours(1);
        return LocalDateTime.now(ZoneId.of("Asia/Jerusalem")).isAfter(updatedAt);
    }

    public boolean hasWeatherConditions(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);
        return !sharedPreferences.getString("Forecast: ", "").equals("");
    }

    public boolean hasCityName(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);
        return !sharedPreferences.getString("CityName: ", "").equals("");
    }

    public String getCityName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);
        return sharedPreferences.getString("CityName: ", "");
    }

    public String getForecast(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Forecast: ", "");
    }

    public String getTemperature(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Temperature: ", "");
    }

    public String getUpdatedAt(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UpdatedAt: ", "");
    }

    public String getDateOfUpdate(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE);

        return sharedPreferences.getString("DateOfUpdate: ", "");
    }

    public void setPrimaryUser(User user){
        if(user != null){
            SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();

            sharedPreferences.edit().putString("Username: ", user.getUsername()).apply();
            sharedPreferences.edit().putString("Password: ", user.getPassword()).apply();
            sharedPreferences.edit().putString("Email: ", user.getEmail()).apply();
            sharedPreferences.edit().putString("StartingWeight: ", user.getStartingWeight() + "").apply();
            sharedPreferences.edit().putString("Weight: ", user.getWeight() + "").apply();
            sharedPreferences.edit().putString("TargetCalories: ", user.getCurrentPlan().getTargetCalories() + "").apply();
            sharedPreferences.edit().putString("TargetProteins: ", user.getCurrentPlan().getTargetProteins() + "").apply();
            sharedPreferences.edit().putString("TargetFats: ", user.getCurrentPlan().getTargetFats() + "").apply();
            sharedPreferences.edit().putString("ProfilePictureId: ", user.getProfilePictureId() + "").apply();
            sharedPreferences.edit().putString("DailyMenus: ", user.getUserDailyMenus()).apply();
        }
    }

    public void updatePrimaryUserPassword(String newPassword){
        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("Password: ", newPassword).apply();
    }

    public void updatePrimaryUserPlan(Plan plan){
        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("TargetCalories: ", plan.getTargetCalories() + "").apply();
        sharedPreferences.edit().putString("TargetProteins: ", plan.getTargetProteins() + "").apply();
        sharedPreferences.edit().putString("TargetFats: ", plan.getTargetFats() + "").apply();
    }

    public void updatePrimaryUserDailyMenus(String dailyMenus){
        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("DailyMenus: ", dailyMenus).apply();
    }

    public void updatePrimaryUserProfilePictureId(int profilePictureId){
        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("DailyMenus: ", profilePictureId + "").apply();
    }

    public User getPrimaryUser(){
        String[] info = new String[10];
        User user = null;

        if(checkIfPrimaryUserExist()){
            SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
            info[0] = sharedPreferences.getString("Username: ", "");
            info[1] = sharedPreferences.getString("Password: ", "");
            info[2] = sharedPreferences.getString("Email: ", "");
            info[3] = sharedPreferences.getString("StartingWeight: ", "");
            info[4] = sharedPreferences.getString("Weight: ", "");
            info[5] = sharedPreferences.getString("TargetCalories: ", "");
            info[6] = sharedPreferences.getString("TargetProteins: ", "");
            info[7] = sharedPreferences.getString("TargetFats: ", "");
            info[8] = sharedPreferences.getString("ProfilePictureId: ", "");
            info[9] = sharedPreferences.getString("DailyMenus: ", "");

            user = new User(info[0], info[1], info[2], info[3], info[4], info[5], info[6], info[7], info[8], info[9]);
        }
        return user;
    }

    public String getPrimaryUsername(){
        if(checkIfPrimaryUserExist()){
            SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
            return sharedPreferences.getString("Username: ", "");
        }
        return "";
    }

    public boolean checkIfPrimaryUserExist(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        if(sharedPreferences.contains("Username: ") && sharedPreferences.contains("Password: "))
            return !sharedPreferences.getString("Username: ", "").equals("");
        return false;
    }

    public void removePrimaryUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("primary_user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("Username: ", "").apply();
        sharedPreferences.edit().putString("Password: ", "").apply();
        sharedPreferences.edit().putString("Email: ", "").apply();
        sharedPreferences.edit().putString("StartingWeight: ", "").apply();
        sharedPreferences.edit().putString("Weight: ", "").apply();
        sharedPreferences.edit().putString("TargetCalories: ", "").apply();
        sharedPreferences.edit().putString("TargetProteins: ", "").apply();
        sharedPreferences.edit().putString("TargetFats: ", "").apply();
        sharedPreferences.edit().putString("ProfilePictureId: ", "").apply();
        sharedPreferences.edit().putString("DailyMenus: ", "").apply();
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

    public Ingredient getIngredientByName(String name) {
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

    public ArrayList<Ingredient> getAllOfTheIngredients() {
        sqdb = my_db.getWritableDatabase();

        Cursor c = sqdb.query(DBHelper.TABLE_NAME, new String[] {DBHelper.INGREDIENT_NAME, DBHelper.PROTEINS, DBHelper.FATS, DBHelper.CALORIES, DBHelper.INGREDIENT_PICTURE_ID},null, null, null, null, null);

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

            ingredients.add(new Ingredient(t1, 100, t2, t3, t4, t5));
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
