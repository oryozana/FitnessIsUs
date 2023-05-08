package com.example.fitnessisus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.VideoView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class LoginAndRegister extends AppCompatActivity {

    private NetworkConnectionReceiver networkConnectionReceiver;
    BottomNavigationView bottomNavigationView;

    int notificationsPermissionId = 152;  // Not really needed for now...

    private MediaPlayer mediaPlayer;
    private VideoView videoView;

    RegisterFragment registerFragment = new RegisterFragment();
    LoginFragment loginFragment = new LoginFragment();

    Song activeSong = Song.getSongs().get(0);

    Intent me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_register);

        me = getIntent();
        if(me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");

        bottomNavigationView = findViewById(R.id.bnvLoginAndRegister);
        getSupportFragmentManager().beginTransaction().replace(R.id.loginAndRegisterFragmentHolder, loginFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
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

    public void initiateVideoPlayer(){
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.world_saved_custom_meals_background_video);
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
            videoView.setBackground(getDrawable(R.drawable.world_saved_custom_meals_background));
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            registerReceiver(networkConnectionReceiver, networkConnectionFilter);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            registerReceiver(networkConnectionReceiver, networkConnectionFilter);
        }

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
    public void onBackPressed() {

    }
}