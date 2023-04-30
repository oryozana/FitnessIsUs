package com.example.fitnessisus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SettingsSetter extends AppCompatActivity implements View.OnClickListener {

    private VideoView videoView;
    private MediaPlayer mediaPlayer;

    RadioGroup rgPlayMusic, rgUseVideos, rgSendNotifications, rgChooseClock;
    Button btReturnToRecentActivity, btChangeMusic;
    boolean playMusic, useVideos, sendNotifications, useDigitalClock;
    boolean wantToSave = false, chooseIfWantToSave = false, needSave = true;
    boolean playMusicAtStart, useVideosAtStart, sendNotificationsAtStart, useDigitalClockAtStart;
    LinearLayout settingsSetterLinearLayout;
    Song activeSong = Song.getSongs().get(0);
    TextView tvCurrentSongName;

    FileAndDatabaseHelper fileAndDatabaseHelper;
    Intent me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_setter);

        me = getIntent();
        if(me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");

        fileAndDatabaseHelper = new FileAndDatabaseHelper(SettingsSetter.this, me);

        rgPlayMusic = (RadioGroup) findViewById(R.id.rgPlayMusic);
        rgUseVideos = (RadioGroup) findViewById(R.id.rgUseVideos);
        rgSendNotifications = (RadioGroup) findViewById(R.id.rgSendNotifications);
        rgChooseClock = (RadioGroup) findViewById(R.id.rgChooseClock);

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

        useDigitalClockAtStart = me.getBooleanExtra("useDigitalClock", true);
        if(!useDigitalClockAtStart)
            rgChooseClock.check(R.id.rbAnalogClock);

        tvCurrentSongName.setText("Current song: " + fileAndDatabaseHelper.getCurrentActiveSong().getName());
    }

    public void saveSettings(){
        getRadioGroupsOptionsSelected();

        boolean[] settings = {playMusic, useVideos, sendNotifications, useDigitalClock};
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

        radioID = rgChooseClock.getCheckedRadioButtonId();
        useDigitalClock = radioID == R.id.rbDigitalClock;
    }

    public void goToMusicMaster(){
        me.setClass(SettingsSetter.this, MusicMaster.class);
        me.putExtra("cameToMusicMasterFrom", getLocalClassName());
        startActivity(me);
    }

    public void returnToRecentActivity(){
        getRadioGroupsOptionsSelected();
        if(!chooseIfWantToSave || needSave){
            if(((playMusicAtStart != playMusic) || (useVideosAtStart != useVideos) || (sendNotificationsAtStart != sendNotifications) || (useDigitalClockAtStart != useDigitalClock)) && !chooseIfWantToSave)
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
        adb.setMessage("Play music ?: " + playMusic + "\n" + "Use videos ?: " + useVideos + "\n" + "Send notifications ?: " + sendNotifications + "\n" + "Use digital clock ?: " + useDigitalClock);
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

        rgChooseClock.check(R.id.rbDigitalClock);
        if(!useDigitalClockAtStart)
            rgChooseClock.check(R.id.rbAnalogClock);
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