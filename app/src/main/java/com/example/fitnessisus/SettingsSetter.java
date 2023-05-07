package com.example.fitnessisus;

import static com.example.fitnessisus.SendNotificationReceiver.CHANNEL_1_ID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingsSetter extends AppCompatActivity implements View.OnClickListener {

    public static final String CHANNEL_1_ID = "Lens_app_channel_1";
    int notificationsPermissionId = 152;

    private VideoView videoView;
    private MediaPlayer mediaPlayer;

    RadioGroup rgPlayMusic, rgUseVideos, rgSendNotifications, rgChooseClockOrWeather;
    Button btReturnToRecentActivity, btChangeMusic;
    RadioButton rbWeatherInfo;

    boolean playMusic, useVideos, sendNotifications, showDigitalClock;
    boolean wantToSave = false, chooseIfWantToSave = false, needSave = true;
    boolean playMusicAtStart, useVideosAtStart, sendNotificationsAtStart, showDigitalClockAtStart;
    LinearLayout settingsSetterLinearLayout;
    Song activeSong = Song.getSongs().get(0);
    TextView tvCurrentSongName;

    Calendar calendar;
    NotificationManagerCompat nmc;

    FileAndDatabaseHelper fileAndDatabaseHelper;
    Intent me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_setter);

        ActivityCompat.requestPermissions(SettingsSetter.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, notificationsPermissionId);

        me = getIntent();
        if(me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");

        fileAndDatabaseHelper = new FileAndDatabaseHelper(SettingsSetter.this, me);

        rgPlayMusic = (RadioGroup) findViewById(R.id.rgPlayMusic);
        rgUseVideos = (RadioGroup) findViewById(R.id.rgUseVideos);
        rgSendNotifications = (RadioGroup) findViewById(R.id.rgSendNotifications);
        rgChooseClockOrWeather = (RadioGroup) findViewById(R.id.rgChooseClockOrWeather);

        rbWeatherInfo = (RadioButton) findViewById(R.id.rbWeatherInfo);
        rbWeatherInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCapitalAlertDialog();
            }
        });

        btReturnToRecentActivity = (Button) findViewById(R.id.btReturnToRecentActivity);
        btReturnToRecentActivity.setOnClickListener(this);
        btChangeMusic = (Button) findViewById(R.id.btChangeMusic);
        btChangeMusic.setOnClickListener(this);

        settingsSetterLinearLayout = (LinearLayout) findViewById(R.id.settingsSetterLinearLayout);
        videoView = (VideoView) findViewById(R.id.settingsSetterVideoView);

        tvCurrentSongName = (TextView) findViewById(R.id.tvCurrentSongName);

        fileAndDatabaseHelper.implementSettingsData();
        setInitialChoices();
        initiateVideoPlayer();
        initiateMediaPlayer();

        calendar = Calendar.getInstance();
        createNotificationChannels();
        initiateAlarms();
    }

    private void createNotificationChannels() {
        android.app.NotificationChannel channel1 = new android.app.NotificationChannel(CHANNEL_1_ID, "Channel 1", NotificationManager.IMPORTANCE_DEFAULT);
        channel1.setDescription("This is Channel for the FitnessIsUs app");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);
    }

    public void initiateAlarms(){
        Intent after = new Intent(SettingsSetter.this, SendNotificationReceiver.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(SettingsSetter.this, 17, after, PendingIntent.FLAG_IMMUTABLE);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent1);

        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(SettingsSetter.this, 18, after, PendingIntent.FLAG_IMMUTABLE);
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent2);

        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(SettingsSetter.this, 19, after, PendingIntent.FLAG_IMMUTABLE);
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent3);

        Toast.makeText(SettingsSetter.this, "Alarms are now active!", Toast.LENGTH_LONG).show();
    }

    public void setInitialChoices(){
        playMusicAtStart = me.getBooleanExtra("playMusic", true);
        if(!playMusicAtStart)
            rgPlayMusic.check(R.id.rbMuteMusic);

        useVideosAtStart = me.getBooleanExtra("useVideos", true);
        if(!useVideosAtStart)
            rgUseVideos.check(R.id.rbUseImages);

        sendNotificationsAtStart = me.getBooleanExtra("sendNotifications", true);
        if(!sendNotificationsAtStart)
            rgSendNotifications.check(R.id.rbDisableNotifications);

        showDigitalClockAtStart = me.getBooleanExtra("showDigitalClock", true);
        if(!showDigitalClockAtStart)
            rgChooseClockOrWeather.check(R.id.rbWeatherInfo);

        tvCurrentSongName.setText("Current song: " + fileAndDatabaseHelper.getCurrentActiveSong().getName());
    }

    public void saveSettings(){
        getRadioGroupsOptionsSelected();

        boolean[] settings = {playMusic, useVideos, sendNotifications, showDigitalClock};
        fileAndDatabaseHelper.updateAppSettings(settings);
    }

    public void getRadioGroupsOptionsSelected(){
        int radioID;

        radioID = rgPlayMusic.getCheckedRadioButtonId();
        playMusic = radioID == R.id.rbPlayMusic;

        radioID = rgUseVideos.getCheckedRadioButtonId();
        useVideos = radioID == R.id.rbUseVideos;

        radioID = rgSendNotifications.getCheckedRadioButtonId();
        sendNotifications = radioID == R.id.rbEnableNotifications;

        radioID = rgChooseClockOrWeather.getCheckedRadioButtonId();
        showDigitalClock = radioID == R.id.rbDigitalClock;
    }

    public void goToMusicMaster(){
        me.setClass(SettingsSetter.this, MusicMaster.class);
        me.putExtra("cameToMusicMasterFrom", getLocalClassName());
        startActivity(me);
    }

    public void returnToRecentActivity(){
        getRadioGroupsOptionsSelected();
        if(!chooseIfWantToSave || needSave){
            if(((playMusicAtStart != playMusic) || (useVideosAtStart != useVideos) || (sendNotificationsAtStart != sendNotifications) || (showDigitalClockAtStart != showDigitalClock)) && !chooseIfWantToSave)
                checkIfWantToSave();
            else {
                needSave = false;
                chooseIfWantToSave = true; // Don't need to save because nothing changed.
                returnToRecentActivity();
            }
        }
        else{
            if(wantToSave)
                saveSettings();

            String cameToSettingsFrom = me.getStringExtra("cameToSettingsFrom");
            if(cameToSettingsFrom.equals("MainActivity"))
                me.setClass(SettingsSetter.this, MainActivity.class);
            if(cameToSettingsFrom.equals("musicMaster"))
                me.setClass(SettingsSetter.this, MusicMaster.class);
            if(cameToSettingsFrom.equals("UserInfoScreen"))
                me.setClass(SettingsSetter.this, UserInfoScreen.class);

            startActivity(me);
        }
    }

    public void checkIfWantToSave(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(this);
        adb.setTitle("Your settings are: ");
        adb.setMessage("Play music ?: " + playMusic + "\n" + "Use videos ?: " + useVideos + "\n" + "Send notifications ?: " + sendNotifications + "\n" + "Show digital clock ?: " + showDigitalClock);
        adb.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        adb.setNeutralButton("Don't save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chooseIfWantToSave = true;
                wantToSave = false;
                returnToRecentActivity();
            }
        });

        adb.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chooseIfWantToSave = true;
                wantToSave = true;
                returnToRecentActivity();
            }
        });

        ad = adb.create();
        ad.show();
    }

    public void resetToPreviousSettings(){
        rgPlayMusic.check(R.id.rbPlayMusic);
        if(!playMusicAtStart)
            rgPlayMusic.check(R.id.rbMuteMusic);

        rgUseVideos.check(R.id.rbUseVideos);
        if(!useVideosAtStart)
            rgUseVideos.check(R.id.rbUseImages);

        rgSendNotifications.check(R.id.rbEnableNotifications);
        if(!sendNotificationsAtStart)
            rgSendNotifications.check(R.id.rbDisableNotifications);

        rgChooseClockOrWeather.check(R.id.rbDigitalClock);
        if(!showDigitalClockAtStart)
            rgChooseClockOrWeather.check(R.id.rbWeatherInfo);
    }

    public void getCapitalAlertDialog(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(SettingsSetter.this);
        adb.setCancelable(false);

        View customAlertDialog = LayoutInflater.from(SettingsSetter.this).inflate(R.layout.alert_dialog_find_capital, null);

        EditText etEnterCountryOrCapitalName = (EditText) customAlertDialog.findViewById(R.id.etEnterCountryOrCapitalName);
        ListView lvCountiesAndCapitalsNames = (ListView) customAlertDialog.findViewById(R.id.lvCountiesAndCapitalsNames);
        Button btCancelCapitalSearch = (Button) customAlertDialog.findViewById(R.id.btCancelCapitalSearch);

        ArrayList<String[]> countryCapitalList = new ArrayList<>();
        countryCapitalList.add(new String[]{"Afghanistan", "Kabul"});
        countryCapitalList.add(new String[]{"Albania", "Tirana"});
        countryCapitalList.add(new String[]{"Algeria", "Algiers"});
        countryCapitalList.add(new String[]{"Angola", "Luanda"});
        countryCapitalList.add(new String[]{"Argentina", "Buenos Aires"});
        countryCapitalList.add(new String[]{"Australia", "Canberra"});
        countryCapitalList.add(new String[]{"Austria", "Vienna"});
        countryCapitalList.add(new String[]{"Bahamas", "Nassau"});
        countryCapitalList.add(new String[]{"Bahrain", "Manama"});
        countryCapitalList.add(new String[]{"Barbados", "Bridgetown"});
        countryCapitalList.add(new String[]{"Belarus", "Minsk"});
        countryCapitalList.add(new String[]{"Belgium", "Brussels"});
        countryCapitalList.add(new String[]{"Bermuda", "Hamilton"});
        countryCapitalList.add(new String[]{"Bhutan", "Thimphu"});
        countryCapitalList.add(new String[]{"Bolivia", "Sucre"});
        countryCapitalList.add(new String[]{"Brazil", "Brasilia"});
        countryCapitalList.add(new String[]{"Bulgaria", "Sofia"});
        countryCapitalList.add(new String[]{"Cambodia", "Phnom Penh"});
        countryCapitalList.add(new String[]{"Cameroon", "Yaoundé"});
        countryCapitalList.add(new String[]{"Canada", "Ottawa"});
        countryCapitalList.add(new String[]{"Chile", "Santiago"});
        countryCapitalList.add(new String[]{"China", "Beijing"});
        countryCapitalList.add(new String[]{"Colombia", "Bogota"});
        countryCapitalList.add(new String[]{"Croatia", "Zagreb"});
        countryCapitalList.add(new String[]{"Cuba", "Havana"});
        countryCapitalList.add(new String[]{"Cyprus", "Nicosia"});
        countryCapitalList.add(new String[]{"Czech Republic", "Prague"});
        countryCapitalList.add(new String[]{"Denmark", "Copenhagen"});
        countryCapitalList.add(new String[]{"Egypt", "Cairo"});
        countryCapitalList.add(new String[]{"Ethiopia", "Addis Ababa"});
        countryCapitalList.add(new String[]{"Fiji", "Suva"});
        countryCapitalList.add(new String[]{"Finland", "Helsinki"});
        countryCapitalList.add(new String[]{"France", "Paris"});
        countryCapitalList.add(new String[]{"Gambia", "Banjul"});
        countryCapitalList.add(new String[]{"Georgia", "Tbilisi"});
        countryCapitalList.add(new String[]{"Germany", "Berlin"});
        countryCapitalList.add(new String[]{"Ghana", "Accra"});
        countryCapitalList.add(new String[]{"Greece", "Athens"});
        countryCapitalList.add(new String[]{"Hungary", "Budapest"});
        countryCapitalList.add(new String[]{"Iceland", "Reykjavik"});
        countryCapitalList.add(new String[]{"Indonesia", "Jakarta"});
        countryCapitalList.add(new String[]{"Iran", "Tehran"});
        countryCapitalList.add(new String[]{"Iraq", "Baghdad"});
        countryCapitalList.add(new String[]{"Ireland", "Dublin"});
        countryCapitalList.add(new String[]{"Israel", "Jerusalem"});
        countryCapitalList.add(new String[]{"Italy", "Rome"});
        countryCapitalList.add(new String[]{"Jamaica", "Kingston"});
        countryCapitalList.add(new String[]{"Japan", "Tokyo"});
        countryCapitalList.add(new String[]{"Kazakhstan", "Astana"});
        countryCapitalList.add(new String[]{"Kenya", "Nairobi"});
        countryCapitalList.add(new String[]{"Kyrgyzstan", "Bishkek"});
        countryCapitalList.add(new String[]{"Laos", "Vientiane"});
        countryCapitalList.add(new String[]{"Latvia", "Riga"});
        countryCapitalList.add(new String[]{"Lebanon", "Beirut"});
        countryCapitalList.add(new String[]{"Liberia", "Monrovia"});
        countryCapitalList.add(new String[]{"Libya", "Tripoli"});
        countryCapitalList.add(new String[]{"Lithuania", "Vilnius"});
        countryCapitalList.add(new String[]{"Luxembourg", "Luxembourg"});
        countryCapitalList.add(new String[]{"Madagascar", "Antananarivo"});
        countryCapitalList.add(new String[]{"Malaysia", "Kuala Lumpur"});
        countryCapitalList.add(new String[]{"Maldives", "Male"});
        countryCapitalList.add(new String[]{"Mali", "Bamako"});
        countryCapitalList.add(new String[]{"Malta", "Valletta"});
        countryCapitalList.add(new String[]{"Mauritius", "Port Louis"});
        countryCapitalList.add(new String[]{"Mexico", "Mexico"});
        countryCapitalList.add(new String[]{"Monaco", "Monaco"});
        countryCapitalList.add(new String[]{"Mongolia", "Ulaanbaatar"});
        countryCapitalList.add(new String[]{"Morocco", "Rabat"});
        countryCapitalList.add(new String[]{"Mozambique", "Maputo"});
        countryCapitalList.add(new String[]{"Myanmar", "Naypyidaw"});
        countryCapitalList.add(new String[]{"Namibia", "Windhoek"});
        countryCapitalList.add(new String[]{"Nepal", "Kathmandu"});
        countryCapitalList.add(new String[]{"Netherlands", "Amsterdam"});
        countryCapitalList.add(new String[]{"Nigeria", "Abuja"});
        countryCapitalList.add(new String[]{"New Zealand", "Wellington"});
        countryCapitalList.add(new String[]{"North Korea", "Pyongyang"});
        countryCapitalList.add(new String[]{"Norway", "Oslo"});
        countryCapitalList.add(new String[]{"Oman", "Muscat"});
        countryCapitalList.add(new String[]{"Pakistan", "Islamabad"});
        countryCapitalList.add(new String[]{"Philippines", "Manila"});
        countryCapitalList.add(new String[]{"Poland", "Warsaw"});
        countryCapitalList.add(new String[]{"Portugal", "Lisbon"});
        countryCapitalList.add(new String[]{"Qatar", "Doha"});
        countryCapitalList.add(new String[]{"Romania", "Bucharest"});
        countryCapitalList.add(new String[]{"Russia", "Moscow"});
        countryCapitalList.add(new String[]{"Rwanda", "Kigali"});
        countryCapitalList.add(new String[]{"Saudi Arabia", "Riyadh"});
        countryCapitalList.add(new String[]{"Serbia", "Belgrade"});
        countryCapitalList.add(new String[]{"Seychelles", "Victoria"});
        countryCapitalList.add(new String[]{"Sierra Leone", "Freetown"});
        countryCapitalList.add(new String[]{"Somalia", "Mogadishu"});
        countryCapitalList.add(new String[]{"South Africa", "Cape town"});
        countryCapitalList.add(new String[]{"South Korea", "Seoul"});
        countryCapitalList.add(new String[]{"South Sudan", "Juba"});
        countryCapitalList.add(new String[]{"Spain", "Madrid"});
        countryCapitalList.add(new String[]{"Sri Lanka", "Sri Jayawardenepura Kotte"});
        countryCapitalList.add(new String[]{"Sudan", "Khartoum"});
        countryCapitalList.add(new String[]{"Sweden", "Stockholm"});
        countryCapitalList.add(new String[]{"Switzerland", "Bern"});
        countryCapitalList.add(new String[]{"Syria", "Damascus"});
        countryCapitalList.add(new String[]{"Taiwan", "Taipei"});
        countryCapitalList.add(new String[]{"Tajikistan", "Dushanbe"});
        countryCapitalList.add(new String[]{"Tanzania", "Dodoma"});
        countryCapitalList.add(new String[]{"Thailand", "Bangkok"});
        countryCapitalList.add(new String[]{"Tunisia", "Tunis"});
        countryCapitalList.add(new String[]{"Turkey", "Ankara"});
        countryCapitalList.add(new String[]{"Turkmenistan", "Ashgabat"});
        countryCapitalList.add(new String[]{"Uganda", "Kampala"});
        countryCapitalList.add(new String[]{"United Arab Emirates", "Abu Dhabi"});
        countryCapitalList.add(new String[]{"United States of America", "Washington, D.C."});
        countryCapitalList.add(new String[]{"Uruguay", "Montevideo"});
        countryCapitalList.add(new String[]{"Uzbekistan", "Tashkent"});
        countryCapitalList.add(new String[]{"Zambia", "Lusaka"});
        countryCapitalList.add(new String[]{"Zimbabwe", "Harare"});

        ArrayList<String> tmp = new ArrayList<>();
        for(String[] countryAndCapital : countryCapitalList)
            tmp.add(countryAndCapital[0] + ", " + countryAndCapital[1]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(SettingsSetter.this, android.R.layout.simple_list_item_1, tmp);
        lvCountiesAndCapitalsNames.setAdapter(adapter);

        adb.setView(customAlertDialog);
        ad = adb.create();

        etEnterCountryOrCapitalName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        lvCountiesAndCapitalsNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String countryAndCapital = (String) parent.getItemAtPosition(position);
                fileAndDatabaseHelper.updateCityName(countryAndCapital);
                Toast.makeText(SettingsSetter.this, countryAndCapital + " chosen successfully.", Toast.LENGTH_SHORT).show();
                ad.cancel();
            }
        });

        btCancelCapitalSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rgChooseClockOrWeather.check(R.id.rbDigitalClock);
                ad.cancel();
            }
        });

        ad.show();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if(itemID == R.id.resetToPreviousSettings)
            resetToPreviousSettings();
        return super.onOptionsItemSelected(item);
    }

    public void initiateVideoPlayer(){
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.settings_setter_video_background);
        videoView.setVideoURI(uri);

        if(me.getBooleanExtra("useVideos", true))
            videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    public void initiateMediaPlayer(){
        mediaPlayer = MediaPlayer.create(SettingsSetter.this, activeSong.getId());
        mediaPlayer.setLooping(true);
        if(me.getBooleanExtra("playMusic", true)){
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoView.resume();
        if(!me.getBooleanExtra("useVideos", true)){
            settingsSetterLinearLayout.setBackground(getDrawable(R.drawable.settings_setter_background));
            videoView.stopPlayback();
        }
        else
            videoView.start();
    }

    @Override
    protected void onRestart() {
        videoView.start();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
        if(!me.getBooleanExtra("playMusic", true)){
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onPause() {
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
    public void onClick(View v) {
        int viewId = v.getId();

        if(viewId == btChangeMusic.getId())
            goToMusicMaster();

        if(viewId == btReturnToRecentActivity.getId())
            returnToRecentActivity();
    }

    @Override
    public void onBackPressed() {
        returnToRecentActivity();
    }
}