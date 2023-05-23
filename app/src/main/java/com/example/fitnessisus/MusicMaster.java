package com.example.fitnessisus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;

public class MusicMaster extends AppCompatActivity implements View.OnClickListener {

    private MediaPlayer mediaPlayer;
    private VideoView videoView;

    Button btFinishMusicMaster, btShuffleMusicMaster;
    LinearLayout musicMasterLinearLayout;
    RadioGroup rgMusicChose;

    FileAndDatabaseHelper fileAndDatabaseHelper;
    ArrayList<Song> songs = Song.getSongs();

    Song activeSong;

    Intent me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_master);

        me = getIntent();
        if(me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");
        else {
            FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(MusicMaster.this);
            if(fileAndDatabaseHelper.hasCurrentActiveSong())
                activeSong = fileAndDatabaseHelper.getCurrentActiveSong();
        }

        videoView = (VideoView) findViewById(R.id.musicMasterVideoView);
        musicMasterLinearLayout = (LinearLayout) findViewById(R.id.musicMasterLinearLayout);

        initiateMediaPlayer();

        btShuffleMusicMaster = (Button) findViewById(R.id.btShuffleMusicMaster);
        btShuffleMusicMaster.setOnClickListener(this);
        btFinishMusicMaster = (Button) findViewById(R.id.btFinishMusicMaster);
        btFinishMusicMaster.setOnClickListener(this);

        rgMusicChose = (RadioGroup) findViewById(R.id.rgMusicChose);
        rgMusicChose.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                getRadioGroupOptionSelected();
            }
        });

        fileAndDatabaseHelper = new FileAndDatabaseHelper(this, me);
        activeSong = fileAndDatabaseHelper.implementSettingsData();

        if(fileAndDatabaseHelper.getShuffleStatus())
            btShuffleMusicMaster.setText("Disable shuffle");

        initiateRadioButtons();
        initiateVideoPlayer();
    }

    public void getRadioGroupOptionSelected(){
        int radioID = rgMusicChose.getCheckedRadioButtonId();

        activeSong = Song.getSongByName((String)(((RadioButton) findViewById(radioID)).getText()));
        changeMediaPlayerSong();
    }

    public void initiateRadioButtons(){
        for(int i = 0; i < rgMusicChose.getChildCount(); i++){
            RadioButton rbCurrent = (RadioButton) rgMusicChose.getChildAt(i);
            rbCurrent.setText(songs.get(i).getName().replaceAll("_", " "));
        }

        RadioButton rbSelected = (RadioButton) rgMusicChose.getChildAt(Song.getActiveSongIndex());
        rgMusicChose.check(rbSelected.getId());
    }

    public void shuffleStatusChange() {
        if(btShuffleMusicMaster.getText().toString().equals("Enable shuffle")){
            btShuffleMusicMaster.setText("Disable shuffle");

            Song.getSongs().get(((int)(Math.random() * Song.getSongs().size()))).playSong();
            activeSong = Song.getActiveSong();

            RadioButton rbSelected = (RadioButton) rgMusicChose.getChildAt(Song.getActiveSongIndex());
            rgMusicChose.check(rbSelected.getId());

            changeMediaPlayerSong();
        }
        else
            btShuffleMusicMaster.setText("Enable shuffle");
    }

    public void finishMusicMaster(){
        String cameToMusicMasterFrom = me.getStringExtra("cameToMusicMasterFrom");
        if(cameToMusicMasterFrom.equals("MainActivity"))
            me.setClass(MusicMaster.this, MainActivity.class);
        if(cameToMusicMasterFrom.equals("SettingsSetter"))
            me.setClass(MusicMaster.this, SettingsSetter.class);
        if(cameToMusicMasterFrom.equals("UserInfoScreen"))
            me.setClass(MusicMaster.this, UserInfoScreen.class);

        me.putExtra("activeSong", activeSong);

        boolean shuffle = btShuffleMusicMaster.getText().toString().equals("Disable shuffle");
        fileAndDatabaseHelper.updateSongSettings(activeSong.getName(), shuffle);
        startActivity(me);
    }

    public void initiateVideoPlayer(){
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.music_master_video_background);
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
        mediaPlayer = MediaPlayer.create(MusicMaster.this, activeSong.getId());
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void changeMediaPlayerSong(){
        if(mediaPlayer == null)
            initiateMediaPlayer();
        else{
            mediaPlayer.reset();
            initiateMediaPlayer();
        }
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
        if (itemID == R.id.resetToPreviousSettings) {
            resetToPreviousMusic();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetToPreviousMusic() {
        if(Song.getActiveSongIndex() != 0)
            songs.get(0).playSong();
        activeSong = Song.getActiveSong();
        rgMusicChose.check(R.id.rbMusicTrack1);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoView.resume();
        if(!me.getBooleanExtra("useVideos", true)){
            musicMasterLinearLayout.setBackground(getDrawable(R.drawable.music_master_background));
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

        if(viewId == btShuffleMusicMaster.getId())
            shuffleStatusChange();

        if(viewId == btFinishMusicMaster.getId())
            finishMusicMaster();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Press finish to exit!", Toast.LENGTH_SHORT).show();
    }
}