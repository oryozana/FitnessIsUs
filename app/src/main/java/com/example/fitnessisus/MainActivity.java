package com.example.fitnessisus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    NetworkConnectionReceiver networkConnectionReceiver;
    boolean isInternetConnectionAvailable = true;

    UploadInfoTask uploadInfoTask;

    private TextClock textClock;

    RelativeLayout weatherLayout;
    TextView tvCityName, tvForecast, tvCurrentTemperature, tvUpdatedAt;

    String response = "";

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    FoodSelectionFragment foodSelectionFragment = new FoodSelectionFragment();
    CustomMealsFragment customMealsFragment = new CustomMealsFragment();

    private VideoView videoView;
    private MediaPlayer mediaPlayer;

    FileAndDatabaseHelper fADHelper = new FileAndDatabaseHelper(MainActivity.this);
    DailyMenu todayMenu;
    Song activeSong;  // In this activity he get a initial value at "createTheFirstIntent".

    FileOutputStream fos;
    OutputStreamWriter osw;
    BufferedWriter bw;
    String currentDate;
    int currentHour;

    SQLiteDatabase sqdb;
    DBHelper my_db;

    Intent me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        me = getIntent();
        if (me.hasExtra("cameFromLogin")) {
            Toast.makeText(this, "Welcome back " + User.getCurrentUser().getUsername() + "!.", Toast.LENGTH_SHORT).show();
            me.removeExtra("cameFromLogin");
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  // Set initial value.
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        isInternetConnectionAvailable = (networkInfo != null && networkInfo.isConnected());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        LocalDateTime today = LocalDateTime.now();
        currentDate = dtf.format(today);

        Calendar calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        textClock = (TextClock) findViewById(R.id.mainActivityTextClock);

        videoView = (VideoView) findViewById(R.id.mainActivityVideoView);

        weatherLayout = (RelativeLayout) findViewById(R.id.weatherLayout);
        tvCityName = (TextView) findViewById(R.id.tvCityName);
        tvForecast = (TextView) findViewById(R.id.tvForecast);
        tvCurrentTemperature = (TextView) findViewById(R.id.tvCurrentTemperature);
        tvUpdatedAt = (TextView) findViewById(R.id.tvUpdatedAt);

        bottomNavigationView = findViewById(R.id.bnvMain);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                weatherLayout.setVisibility(View.INVISIBLE);
                textClock.setVisibility(View.INVISIBLE);

                if (itemId == R.id.sendToHome) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, homeFragment).commit();

                    FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(MainActivity.this, me);
                    if(fileAndDatabaseHelper.getShowDigitalClockStatus())
                        textClock.setVisibility(View.VISIBLE);
                    else
                        weatherLayout.setVisibility(View.VISIBLE);

                    fileAndDatabaseHelper.implementSettingsData();
                    return true;
                }

                if (itemId == R.id.sendToFoodSelection) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, foodSelectionFragment).commit();
                    return true;
                }

                if (itemId == R.id.sendToCustomMeals) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, customMealsFragment).commit();
                    return true;
                }

                return false;
            }
        });

        setCustomNetworkConnectionReceiver();

        me = createTheFirstIntent(me);
        if (me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");

        FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(this, me);
        activeSong = fileAndDatabaseHelper.implementSettingsData();

        if(fileAndDatabaseHelper.getShowDigitalClockStatus())
            textClock.setVisibility(View.VISIBLE);
        else
            weatherLayout.setVisibility(View.VISIBLE);

        initiateMediaPlayer();
        initiateVideoPlayer();
    }

    public Intent createTheFirstIntent(Intent me) {
        if (!me.hasExtra("exists")) {
            me = new Intent(this, UserInfoScreen.class);
            me.putExtra("exists", true);
            me.putExtra("currentDate", currentDate);

            Song.initiateSongs();

            FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(MainActivity.this, me);
            if (!fileAndDatabaseHelper.isSettingsExist()) {
                fileAndDatabaseHelper.firstInitiateSettings();
                fileAndDatabaseHelper.implementSettingsData();
                firstInitiateCustomMealsNamesFile();
                firstInitiateLocalUsersDatabase();
                firstInitiateDailyMenusFile();
                firstInitiatePrimaryUser();
                firstInitiateWeather();
            }

            activeSong = fileAndDatabaseHelper.getActiveSongAndShuffleIfNeedTo();

            if (fileAndDatabaseHelper.isDatabaseEmpty())
                Ingredient.initiateIngredientsDatabase(this);
            Ingredient.initiateIngredientList(this);

            DailyMenu.setDailyMenus(MainActivity.this);
            if (DailyMenu.hasTodayMenuInsideAllDailyMenus(currentDate))
                todayMenu = DailyMenu.getTodayMenuFromAllDailyMenus(currentDate);
            else
                todayMenu = new DailyMenu(currentDate);
            DailyMenu.setTodayMenu(todayMenu);

            if (fileAndDatabaseHelper.checkIfPrimaryUserExist()) {
                User primary = fileAndDatabaseHelper.getPrimaryUser();
                Toast.makeText(this, "Welcome back " + primary.getUsername() + "!.", Toast.LENGTH_SHORT).show();
                User.setCurrentUser(primary);

                me.setClass(this, MainActivity.class);
                startActivity(me);
            }
            else {
                me.setClass(this, LoginAndRegister.class);
                startActivity(me);
            }
        }
        else {
            todayMenu = DailyMenu.getTodayMenu();
            DailyMenu.saveDailyMenuIntoFile(todayMenu, MainActivity.this);
            getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, homeFragment).commit();

            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                me.putExtra("sendNotifications", false);
                fADHelper.setSendNotificationsStatus(false);
            }

            if(!fADHelper.getShowDigitalClockStatus()){
                if(fADHelper.hasWeatherConditions()){
                    if(fADHelper.isNeedToUpdateWeather() && isInternetConnectionAvailable){
                        WeatherTask weatherTask = new WeatherTask();
                        weatherTask.execute();
                    }
                    else
                        setAvailableWeatherInfo();
                }
                else{
                    WeatherTask weatherTask = new WeatherTask();
                    weatherTask.execute();
                }
            }
        }
        return me;
    }

    public void setAvailableWeatherInfo(){
        tvCityName.setText(fADHelper.getCityName());
        tvForecast.setText(fADHelper.getForecast());
        tvCurrentTemperature.setText(fADHelper.getTemperature());
        tvUpdatedAt.setText(fADHelper.getUpdatedAt());
    }

    public void initiateVideoPlayer() {
        if (6 <= currentHour && currentHour < 12)
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.main_activity_morning_background_video));
        if (12 <= currentHour && currentHour < 18)
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.main_activity_noon_background_video));
        if ((18 <= currentHour && currentHour <= 23) || (0 <= currentHour && currentHour < 6))
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.main_activity_night_background_video));


        if (me.getBooleanExtra("useVideos", true))
            videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    public void initiateMediaPlayer() {
        mediaPlayer = MediaPlayer.create(MainActivity.this, activeSong.getId());
        mediaPlayer.setLooping(true);
        if (me.getBooleanExtra("playMusic", true)) {
            mediaPlayer.start();
        }
    }

    public void setCustomNetworkConnectionReceiver() {
        networkConnectionReceiver = new NetworkConnectionReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isOnline(context)) {
                    isInternetConnectionAvailable = false;

                    Toast.makeText(context, "Internet connection lost, data won't be saved.", Toast.LENGTH_SHORT).show();
                }
                else {
                    isInternetConnectionAvailable = true;

                    if(!fADHelper.getShowDigitalClockStatus()){
                        if(fADHelper.hasWeatherConditions()){
                            if(fADHelper.isNeedToUpdateWeather()){
                                WeatherTask weatherTask = new WeatherTask();
                                weatherTask.execute();
                            }
                            else
                                setAvailableWeatherInfo();
                        }
                        else{
                            WeatherTask weatherTask = new WeatherTask();
                            weatherTask.execute();
                        }
                    }

                    uploadInfoTask = new UploadInfoTask(MainActivity.this);
                    uploadInfoTask.execute();
                }
            }
        };
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoView.resume();
        if (!me.getBooleanExtra("useVideos", true)) { // videoView background cause of the clock.
            videoView.stopPlayback();
            videoView.setBackground(getDrawable(R.drawable.main_activity_morning_background));
            if (12 <= currentHour && currentHour < 18)
                videoView.setBackground(getDrawable(R.drawable.main_activity_noon_background));
            if ((18 <= currentHour && currentHour <= 23) || (0 <= currentHour && currentHour < 6))
                videoView.setBackground(getDrawable(R.drawable.main_activity_night_background));
        } else
            videoView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter networkConnectionFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(networkConnectionReceiver, networkConnectionFilter);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(networkConnectionReceiver, networkConnectionFilter);
        }

        mediaPlayer.start();
        if (!me.getBooleanExtra("playMusic", true)) {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onPause() {

        try {
            unregisterReceiver(networkConnectionReceiver);
        } catch (IllegalArgumentException e) {
            e.getStackTrace();
        }

        videoView.suspend();
        mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        videoView.stopPlayback();
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.sendToMusicMaster) {
            me.setClass(MainActivity.this, MusicMaster.class);
            me.putExtra("cameToMusicMasterFrom", getLocalClassName());
            startActivity(me);
        }

        if (itemID == R.id.sendToSettings) {
            me.setClass(MainActivity.this, SettingsSetter.class);
            me.putExtra("cameToSettingsFrom", getLocalClassName());
            startActivity(me);
        }

        if (itemID == R.id.sendToUserScreen) {
            me.setClass(MainActivity.this, UserInfoScreen.class);
            me.putExtra("cameToUserScreenFrom", getLocalClassName());
            startActivity(me);
        }
        return super.onOptionsItemSelected(item);
    }

    public void firstInitiateCustomMealsNamesFile() {
        try {
            fos = openFileOutput("customMealsNames", Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);

            bw.write("Custom meals names: " + "\n");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void firstInitiateDailyMenusFile() {
        try {
            fos = openFileOutput("dailyMenusFile", Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);

            bw.write("Daily menus: " + "\n");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void firstInitiatePrimaryUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("primary_user", Context.MODE_PRIVATE);
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

    public void firstInitiateWeather(){
        SharedPreferences sharedPreferences = getSharedPreferences("weather", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("CityName: ", "").apply();
        sharedPreferences.edit().putString("Forecast: ", "").apply();
        sharedPreferences.edit().putString("Temperature: ", "").apply();
        sharedPreferences.edit().putString("UpdatedAt: ", "").apply();
        sharedPreferences.edit().putString("DateOfUpdate: ", "").apply();
    }

    public void firstInitiateLocalUsersDatabase() {
        my_db = new DBHelper(this);
        sqdb = my_db.getWritableDatabase();
        sqdb.close();
    }

    public static class UploadInfoTask extends AsyncTask<Void, Void, Void> {
        FirebaseDatabase usersDb;
        DatabaseReference userReference;
        Context context;
        boolean isAlreadyRunning = false;
        DailyMenu todayMenu;

        public UploadInfoTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {  // "..." - means that every amount of parameters will do.
            try {
                if (!isAlreadyRunning && todayMenu.isNeedToBeSaved()) {
                    User user = User.getCurrentUser();
                    user.downloadUserDailyMenusFromTemporaryFile(context);
                    User.setCurrentUser(user);

                    startUpdateDailyMenusPreparations(0);
                }
            } catch (DatabaseException e) {
                Toast.makeText(context, "Failed to save info.", Toast.LENGTH_SHORT).show();
                isAlreadyRunning = false;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            todayMenu = DailyMenu.getTodayMenu();
            super.onPreExecute();
        }

        public void startUpdateDailyMenusPreparations(int counter) {
            User user = User.getCurrentUser();
            isAlreadyRunning = true;

            usersDb = FirebaseDatabase.getInstance();
            userReference = usersDb.getReference("users");

            userReference.child(user.getUsername()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            String[] fbDailyMenus = String.valueOf(dataSnapshot.child("userDailyMenus").getValue()).split("       ");

                            ArrayList<String> dailyMenus = new ArrayList<String>();
                            for (String dailyMenu : fbDailyMenus) {
                                if (dailyMenu.contains(" { ") && dailyMenu.contains(" }"))
                                    dailyMenus.add(dailyMenu);
                            }

                            int firebaseTodayMenuIndex = 0;
                            boolean found = false;
                            for (int i = 0; i < dailyMenus.size() && !found; i++) {
                                DailyMenu tmp = DailyMenu.generateDailyMenuObjectFromFile(dailyMenus.get(i));
                                if (tmp.getDate().equals(todayMenu.getDate())) {
                                    firebaseTodayMenuIndex = i;
                                    found = true;
                                }
                            }

                            String info = DailyMenu.getTodayMenu().generateDailyMenuDescriptionForFiles();
                            if (found)
                                dailyMenus.remove(firebaseTodayMenuIndex);
                            dailyMenus.add(info);

                            String userDailyMenus = "";
                            for (String dailyMenu : dailyMenus)
                                userDailyMenus += dailyMenu;

                            updateDailyMenusInFirebase(userDailyMenus, 0);
                        } else
                            isAlreadyRunning = false;
                    } else {
                        if (counter < 2)
                            startUpdateDailyMenusPreparations(counter + 1);
                        else
                            isAlreadyRunning = false;
                    }
                }
            });
        }

        public void updateDailyMenusInFirebase(String userDailyMenus, int counter) {
            String username = User.getCurrentUser().getUsername();

            userReference.child(username).child("userDailyMenus").setValue(userDailyMenus)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(context);
                            if (fileAndDatabaseHelper.getPrimaryUsername().equals(username))
                                fileAndDatabaseHelper.updatePrimaryUserDailyMenus(userDailyMenus);

                            todayMenu.setIsNeedToBeSaved(false);
                            isAlreadyRunning = false;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (counter < 2)
                                updateDailyMenusInFirebase(userDailyMenus, counter + 1);
                            else
                                isAlreadyRunning = false;
                        }
                    });
        }
    }

    public class WeatherTask extends AsyncTask<Void, Void, Void>{
        String weatherAPI = "7fd9239e91743947217f48e81e11c139";
        String cityName = "";

        public WeatherTask(){
            int length = fADHelper.getCityName().split(", ").length;  // There, might be some ", " inside the country name
            cityName = fADHelper.getCityName().split(", ")[length - 1];
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(isInternetConnectionAvailable && !fADHelper.getShowDigitalClockStatus() && !cityName.equals(""))
                    getWeatherData();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        public void getWeatherData() throws IOException {
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=metric&appid=" + weatherAPI;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Failed to get weather data from OpenWeatherMap API. HTTP error code: " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder responseFromApi = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                responseFromApi.append(inputLine);
            }
            in.close();

            response = responseFromApi.toString();

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateWeatherInfo();
                }
            });
        }
    }

    public void updateWeatherInfo(){
        if(!response.equals("")){
            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
                JSONObject sys = jsonObj.getJSONObject("sys");

                // CALL VALUE IN API :
                String cityName = sys.getString("country") + ", " + jsonObj.getString("name");
                String temperature = main.getString("temp") + "°C";
                String forecast = weather.getString("description");

                long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Last Updated at: " + new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(updatedAt * 1000));

                LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Jerusalem"));
                fADHelper.updateWeatherConditions(cityName, forecast, temperature, updatedAtText, currentTime.toString());

                // SET ALL VALUES IN TextViews :
                tvCityName.setText(cityName);
                tvCurrentTemperature.setText(temperature);
                tvUpdatedAt.setText(updatedAtText);
                tvForecast.setText(forecast);

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
}