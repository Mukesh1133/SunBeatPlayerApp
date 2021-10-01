package com.example.sunbeatplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button playBtn, playNxt, playPrev, btnFF, btnFR;
    TextView txtSong, txtStart, txtStop;
    SeekBar seekBar;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       if(item.getItemId()==android.R.id.home) {
           onBackPressed();
       }
       return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        playPrev = findViewById(R.id.playPrev);
        playNxt = findViewById(R.id.playNxt);
        playBtn = findViewById(R.id.playBtn);
        btnFF = findViewById(R.id.btnFF);
        btnFR = findViewById(R.id.btnFR);
        txtSong = findViewById(R.id.txtSong);
        txtStart = findViewById(R.id.txtStart);
        txtStop = findViewById(R.id.txtStop);
        seekBar = findViewById(R.id.seekBar);
        visualizer = findViewById(R.id.visualizer);
        imageView = findViewById(R.id.imageView);

        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs= (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtSong.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtSong.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        updateSeekbar = new Thread(){
            @Override
            public void run() {
               int totalDuration = mediaPlayer.getDuration();
               int currentPosition = 0;

               while (currentPosition<totalDuration) {
                   try{
                       sleep(500);
                       currentPosition = mediaPlayer.getCurrentPosition();
                       seekBar.setProgress(currentPosition);
                   }
                   catch (InterruptedException | IllegalStateException e) {
                       e.printStackTrace();
                   }
               }
            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary),PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtStop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtStart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        },delay);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    playBtn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    mediaPlayer.pause();
                }
                else {
                    playBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    mediaPlayer.start();
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNxt.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1) {
            visualizer.setAudioSessionId(audioSessionId);
        }

        playNxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtSong.setText(sname);
                mediaPlayer.start();
                playBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                startAnimation(imageView);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1) {
                    visualizer.setAudioSessionId(audioSessionId);
                }

            }
        });

        playPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);

                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName();
                txtSong.setText(sname);
                mediaPlayer.start();
                playBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                startAnimation(imageView);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1) {
                    visualizer.setAudioSessionId(audioSessionId);
                }

            }
        });

        btnFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        btnFR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-1000);
                }
            }
        });


    }
    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation", 0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";

        if (sec<10) {
            time+="0";
        }
        time+=sec;
        return time;
    }
}