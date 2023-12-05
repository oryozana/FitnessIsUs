package com.example.fitnessisus;

import static com.example.fitnessisus.SendNotificationReceiver.CHANNEL_1_ID;
import static com.example.fitnessisus.SettingsSetter.requestsCodes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.VideoView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Calendar;

public class LoginAndRegister extends AppCompatActivity {

    private NetworkConnectionReceiver networkConnectionReceiver;
    BottomNavigationView bottomNavigationView;

    int notificationsPermissionId = 152;
    Calendar calendar;

    private MediaPlayer mediaPlayer;
    private VideoView videoView;

    RegisterFragment registerFragment = new RegisterFragment();
    LoginFragment loginFragment = new LoginFragment();

    Song activeSong;

    Intent me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_register);

        me = getIntent();
        if(me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");
        else {
            FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(LoginAndRegister.this);
            if(fileAndDatabaseHelper.hasCurrentActiveSong())
                activeSong = fileAndDatabaseHelper.getCurrentActiveSong();
            else{
                Song.initiateSongs();
                activeSong = Song.getActiveSong();
            }
        }

        bottomNavigationView = findViewById(R.id.bnvLoginAndRegister);
        getSupportFragmentManager().beginTransaction().replace(R.id.loginAndRegisterFragmentHolder, loginFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.sendToHome) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.loginAndRegisterFragmentHolder, loginFragment).commit();
                    return true;
                }

                if(itemId == R.id.sendToFoodSelection) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.loginAndRegisterFragmentHolder, registerFragment).commit();
                    return true;
                }

                return false;
            }
        });

        videoView = (VideoView) findViewById(R.id.loginAndRegisterVideoView);
        networkConnectionReceiver = new NetworkConnectionReceiver();

        if(ActivityCompat.checkSelfPermission(LoginAndRegister.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(LoginAndRegister.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, notificationsPermissionId);

        initiateVideoPlayer();
        initiateMediaPlayer();
    }

    public boolean isAlarmSet(int requestCode) {
        Intent intent = new Intent(LoginAndRegister.this, SendNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(LoginAndRegister.this, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent != null;
    }

    public void initiateAlarms(){
        if(!(isAlarmSet(requestsCodes[0]) && isAlarmSet(requestsCodes[1]) && isAlarmSet(requestsCodes[2]))){
            createNotificationChannels();

            calendar = Calendar.getInstance();

            Intent after = new Intent(LoginAndRegister.this, SendNotificationReceiver.class);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(LoginAndRegister.this, requestsCodes[0], after, PendingIntent.FLAG_IMMUTABLE);
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND,0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent1);

            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(LoginAndRegister.this, requestsCodes[1], after, PendingIntent.FLAG_IMMUTABLE);
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            calendar.set(Calendar.MINUTE, 30);
            calendar.set(Calendar.SECOND,0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent2);

            PendingIntent pendingIntent3 = PendingIntent.getBroadcast(LoginAndRegister.this, requestsCodes[2], after, PendingIntent.FLAG_IMMUTABLE);
            calendar.set(Calendar.HOUR_OF_DAY, 22);
            calendar.set(Calendar.MINUTE, 30);
            calendar.set(Calendar.SECOND,0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent3);
        }
    }

    private void createNotificationChannels() {
        android.app.NotificationChannel channel1 = new android.app.NotificationChannel(CHANNEL_1_ID, "Channel 1", NotificationManager.IMPORTANCE_DEFAULT);
        channel1.setDescription("This is Channel for the FitnessIsUs app");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == notificationsPermissionId){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateAlarms();
            }
            else {
                FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(LoginAndRegister.this);
                fileAndDatabaseHelper.setSendNotificationsStatus(false);
            }
        }
    }

    public void initiateVideoPlayer(){
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.login_and_register_background_video);
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
        mediaPlayer = MediaPlayer.create(LoginAndRegister.this, activeSong.getId());
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
            videoView.setBackground(getDrawable(R.drawable.login_and_register_background));
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

        IntentFilter networkConnectionFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkConnectionReceiver, networkConnectionFilter);

        mediaPlayer.start();
        if(!me.getBooleanExtra("playMusic", true)){
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onPause() {

        try{
            unregisterReceiver(networkConnectionReceiver);
        }
        catch (IllegalArgumentException e){
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}