package com.example.uvmplayer;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class playerActivity extends AppCompatActivity {

    Button btnplay, btnnext, btnprev, btnff, btnfr;
    TextView txtsname, txtsstart, txtsstop;
    SeekBar seekmusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String Extra_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null)
        {
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
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnplay = findViewById(R.id.playbtn);
        btnnext = findViewById(R.id.btnnext);
        btnprev = findViewById(R.id.btnprev);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);

        imageView = findViewById(R.id.imageView);

        txtsname = findViewById(R.id.txtsn);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);

        seekmusic = findViewById(R.id.seekbar);

        visualizer = findViewById(R.id.blast);

        if (mediaPlayer !=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos",0);
        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname= mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateseekbar = new Thread()
        {
            @Override
            public void run() {
                int totalDuration= mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition<totalDuration)
                {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentPosition);
                    }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary),PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

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
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final  int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this,delay);

            }
        },delay);

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    btnplay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    mediaPlayer.pause();
                }
                else
                {
                    btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnnext.performClick();
            }
        });

        int audiosessionId= mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1)
        {
            visualizer.setAudioSessionId(audiosessionId);
        }

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                startAnimation(imageView);
                int audiosessionId= mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1)
                {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });
        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);

                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                startAnimation(imageView);
                int audiosessionId= mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1)
                {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }
    public void startAnimation(View view)
    {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration)
    {
        String time= "";
        int min = duration/1000/60;
        int sec  = duration/1000%60;

        time+=min+":";

        if (sec<10)
        {
            time+="0";
        }
        time+=sec;

        return time;
    }

}