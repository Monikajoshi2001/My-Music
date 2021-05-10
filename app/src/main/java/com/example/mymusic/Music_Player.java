package com.example.mymusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
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

public class Music_Player extends AppCompatActivity {
    Button PlayBtn,NextBtn,PrevBtn,FastForwardBtn,RevindBtn;
    TextView Name_Song,txt_Start,txt_End;
    SeekBar seekBar;
    BarVisualizer barVisualizer;
    ImageView imageView;

    String Sname;
    public static final String EXTRA_NAME = "song_name";
    int position;
    ArrayList<File> mySongs;
    MediaPlayer mediaPlayer;
    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (barVisualizer != null)
        {
            barVisualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music__player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        PlayBtn = (Button)findViewById(R.id.playbtn);
        NextBtn = (Button)findViewById(R.id.btnNext);
        PrevBtn = (Button)findViewById(R.id.btnPrev);
        FastForwardBtn = (Button)findViewById(R.id.btnFastForward);
        RevindBtn = (Button)findViewById(R.id.btnRevind);
        Name_Song = (TextView)findViewById(R.id.text1);
        txt_Start = (TextView)findViewById(R.id.txtStart);
        txt_End = (TextView)findViewById(R.id.txtStop);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        barVisualizer = (BarVisualizer)findViewById(R.id.blast);
        imageView = (ImageView) findViewById(R.id.song_icon);


        Intent i = getIntent();
        Bundle bundle=i.getExtras();
        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");
        String SONG_NAME = (String)bundle.getString("song_name");
        position= (int)bundle.getInt("pos",0);
        Name_Song.setSelected(true);
        Uri uri= Uri.parse(mySongs.get(position).toString());
        Sname = mySongs.get(position).getName();
        Name_Song.setText(Sname);

        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();



        updateSeekBar= new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while(currentPosition<totalDuration)
                {
                    try {
                        sleep(500);
                        currentPosition= mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }

                }
            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
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
        txt_End.setText(endTime);

        final Handler handler= new Handler();
        final int Delay =1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txt_Start.setText(currentTime);
                handler.postDelayed(this,Delay);
            }
        },Delay);

        PlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    PlayBtn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    mediaPlayer.pause();
                }
                else
                {
                    PlayBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                NextBtn.performClick();
            }
        });
        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId!=-1)
        {
            barVisualizer.setAudioSessionId(audioSessionId);
        }

        NextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (((position+1)==mySongs.size())?0:position+1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                Sname = mySongs.get(position).getName();
                Name_Song.setText(Sname);
                mediaPlayer.start();
                PlayBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                StartAnimation(imageView);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId!=-1)
                {
                    barVisualizer.setAudioSessionId(audioSessionId);
                }
            }
        });
        PrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0)?mySongs.size()-1:(position-1);
                Uri uri1 = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri1);
                Sname = mySongs.get(position).getName();
                Name_Song.setText(Sname);
                mediaPlayer.start();
                PlayBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                StartAnimation(imageView);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId!=-1)
                {
                    barVisualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        FastForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        RevindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

    }
    public void StartAnimation(View view)
    {
        ObjectAnimator animator= ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    public String createTime(int duration)
    {
        String time ="";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time+=min+":";
        if(sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }
}