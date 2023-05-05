package com.example.fitnessisus;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
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
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AnalogClock;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    NetworkConnectionReceiver networkConnectionReceiver;
    boolean isInternetConnectionAvailable = false;

    MyAsyncClass mac;

    private AnalogClock analogClock;
    private TextClock textClock;

    RelativeLayout weatherLayout;
    TextView tvCityName, tvForecast, tvCurrentTemperature, tvUpdatedAt;

    String city;
    String weatherAPI = "7fd9239e91743947217f48e81e11c139";
    String response = "";

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    FoodSelectionFragment foodSelectionFragment = new FoodSelectionFragment();
    CustomMealsFragment customMealsFragment = new CustomMealsFragment();

    private VideoView videoView;
    private MediaPlayer mediaPlayer;

    int locationPermissionId = 42;

    DailyMenu todayMenu;
    Song activeSong;  // In this activity he get a initial value at "createTheFirstIntent".

    FileOutputStream fos;
    OutputStreamWriter osw;
    BufferedWriter bw;
    String currentDate;
    int currentHour;

    SQLiteDatabase sqdb;
    DBHelper my_db;

    FileInputStream is;
    InputStreamReader isr;
    BufferedReader br;

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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        LocalDateTime today = LocalDateTime.now();
        currentDate = dtf.format(today);

        Calendar calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        analogClock = (AnalogClock) findViewById(R.id.mainActivityAnalogClock);
        analogClock.setVisibility(View.INVISIBLE);
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
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                analogClock.setVisibility(View.INVISIBLE);
                textClock.setVisibility(View.INVISIBLE);

                if (itemId == R.id.sendToHome) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, homeFragment).commit();
                    // analogClock.setVisibility(View.VISIBLE);
                    textClock.setVisibility(View.VISIBLE);
                    FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(MainActivity.this, me);
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

        me = createTheFirstIntent(me);
        if (me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");

        FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(this, me);
        activeSong = fileAndDatabaseHelper.implementSettingsData();

        setCustomNetworkConnectionReceiver();
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

            CityTask cityTask = new CityTask(new CityTask.OnTaskCompleted() {
                @Override
                public void onTaskCompleted() {

                    WeatherTask weatherTask = new WeatherTask();
                    weatherTask.execute();
                }
            });
            cityTask.execute();
        }
        return me;
    }

    public String getFileData(String fileName) {
        String currentLine = "", allData = "";
        try {
            is = openFileInput(fileName);
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            currentLine = br.readLine();
            while (currentLine != null) {
                allData += currentLine + "\n";
                currentLine = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allData;
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

                    mac = new MyAsyncClass(MainActivity.this);
                    mac.execute();
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

    public void firstInitiateLocalUsersDatabase() {
        my_db = new DBHelper(this);
        sqdb = my_db.getWritableDatabase();
        sqdb.close();
    }

    public static class MyAsyncClass extends AsyncTask<Void, Void, Void> {
        FirebaseDatabase usersDb;
        DatabaseReference userReference;
        Context context;
        boolean isAlreadyRunning = false;
        DailyMenu todayMenu;

        public MyAsyncClass(Context context) {
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

    public void updateWeatherInfo(){
        if(city != null && !response.equals("")){
            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
                JSONObject sys = jsonObj.getJSONObject("sys");

                // CALL VALUE IN API :
                String cityName = sys.getString("country") + ", " + jsonObj.getString("name");

//                LocalDateTime currentTime = LocalDateTime.now(TimeZone.getDefault().toZoneId());
//                String date = currentTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
//                String time = currentTime.format(DateTimeFormatter.ofPattern("HH:mm"));
//                String updatedAtText = "Last Updated at: " + date + " " + time;
                String temperature = main.getString("temp");
                String forecast = weather.getString("description");

                long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Last Updated at: " + new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));

                // SET ALL VALUES IN TextViews :
                tvCityName.setText(cityName);
                tvCurrentTemperature.setText(temperature + "°C");
                tvUpdatedAt.setText(updatedAtText);
                tvForecast.setText(forecast);

            }
            catch (Exception e){
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("bet", "hg9ert8u");

        if (requestCode == locationPermissionId) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CityTask cityTask = new CityTask(new CityTask.OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted() {

                        WeatherTask weatherTask = new WeatherTask();
                        weatherTask.execute();
                    }
                });
                cityTask.execute();
            }
        }
    }

//    private void requestNewLocationData() {
//
//        Log.d("LocationMainActivity", "Here1");
//
//        // Initializing LocationRequest object with appropriate methods
//        LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
//        locationRequest.setInterval(5);
//        locationRequest.setFastestInterval(0);
//        locationRequest.setNumUpdates(1);
//
//        // setting LocationRequest on FusedLocationClient
//        locationProvider = LocationServices.getFusedLocationProviderClient(this);
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            return;
//
//        Log.d("LocationMainActivity", "Here10");
//        locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//        Log.d("LocationMainActivity", "Here11");
//    }
//
//    // method to check for permissions
//    private boolean checkPermissions() {
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
//            return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//
//        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//    }

    // method to check if location is enabled

    // If everything is alright then


    public class CityTask extends AsyncTask<Void, Void, Void>{

        private OnTaskCompleted listener;

        public CityTask(OnTaskCompleted listener) {
            this.listener = listener;
        }

        public interface OnTaskCompleted {
            void onTaskCompleted();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(isInternetConnectionAvailable)
                getUserCityName();

            return null;
        }

        private void getUserCityName() {
            // Get the user's current location
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, locationPermissionId);
                return;
            }

            if(!isLocationEnabled()) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

            else{
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // Get the user's city name from the location
                                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.ENGLISH);
                                    List<Address> addresses;
                                    try {
                                        Log.d("CityName", location.getLatitude() + " , " + location.getLongitude());
                                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                        Log.d("CityName", " " + addresses.get(0));

                                        if (!addresses.isEmpty()) {
                                            String cityName = addresses.get(0).getLocality();
                                            Log.d("CityName", "User's city name: " + cityName);

                                            if(cityName != null){
                                                city = cityName;

                                                if (listener != null) {
                                                    listener.onTaskCompleted();
                                                }
                                            }
                                        }
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        }

        private boolean isLocationEnabled() {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
    }

    public class WeatherTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(isInternetConnectionAvailable)
                    getWeatherData();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        public void getWeatherData() throws IOException {
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + weatherAPI;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            Log.d("LocationMainActivity", "CITY abc: " + city);

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

    @Override
    public void onBackPressed() {

    }
}