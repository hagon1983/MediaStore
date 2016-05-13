package com.aleksander.mediastore.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import com.aleksander.mediastore.SongProgressEvent;
import com.aleksander.mediastore.data.Song;
import com.aleksander.mediastore.songs.SongStateChangedEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {

    private static final String TAG = "MusicService";

    private static final String ACTION_START = MusicService.class.getName() + ".ACTION_START";
    private static final String ACTION_PLAY_PAUSE = MusicService.class.getName() + ".ACTION_PLAY_PAUSE";
    private static final String ACTION_STOP = MusicService.class.getName() + ".ACTION_STOP";
    private static final String ACTION_SEEK = MusicService.class.getName() + ".ACTION_SEEK";
    private static final String ACTION_REDELIVER_STATUS = MusicService.class.getName() + ".ACTION_REDELIVER_STATUS";

    private final static String KEY_ACTION = "action";
    private final static String KEY_SONG = "song";
    private final static String KEY_OFFSET = "offset";

    private Song currentSong;

    protected static Intent getStartCommand(Context context, Song song) {
        Intent intent = new Intent(context.getApplicationContext(), MusicService.class);
        intent.putExtra(KEY_ACTION, ACTION_START);
        intent.putExtra(KEY_SONG, song);
        return intent;
    }

    protected static Intent getPauseCommand(Context context, Song song) {
        Intent intent = new Intent(context.getApplicationContext(), MusicService.class);
        intent.putExtra(KEY_SONG, song);
        intent.putExtra(KEY_ACTION, ACTION_PLAY_PAUSE);
        return intent;
    }

    protected static Intent getStopCommand(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), MusicService.class);
        intent.putExtra(KEY_ACTION, ACTION_STOP);
        return intent;
    }

    protected static Intent getSeekCommand(Context context, int offset) {
        Intent intent = new Intent(context.getApplicationContext(), MusicService.class);
        intent.putExtra(KEY_ACTION, ACTION_SEEK);
        intent.putExtra(KEY_OFFSET, offset);
        return intent;
    }

    protected static Intent getRedeliverStatusCommand(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), MusicService.class);
        intent.putExtra(KEY_ACTION, ACTION_REDELIVER_STATUS);
        return intent;
    }

    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                reportSongStateChangedEvent(SongStateChangedEvent.EventType.COMPLETED);
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                reportSongStateChangedEvent(SongStateChangedEvent.EventType.STARTED);
            }
        });

        TimerTask reportTask = new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    publishProgress(mMediaPlayer.getCurrentPosition());
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(reportTask, 100, 300);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getStringExtra(KEY_ACTION);
        if (ACTION_START.equalsIgnoreCase(action)) {
            final Song song = intent.getParcelableExtra(KEY_SONG);
            playSong(song);
        } else if (ACTION_PLAY_PAUSE.equalsIgnoreCase(action)) {
            playPause();
        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
            stop();
        } else if (ACTION_SEEK.equalsIgnoreCase(action)) {
            final int offset = intent.getIntExtra(KEY_OFFSET, 0);
            seekTo(offset);
        } else if (ACTION_REDELIVER_STATUS.equalsIgnoreCase(action)) {
            redeliverStatus();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void playSong(final Song song) {
        currentSong = song;
        Uri myUri = Uri.fromFile(new File(song.getUri()));
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), myUri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playPause() {
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            reportSongStateChangedEvent(SongStateChangedEvent.EventType.PAUSED);
        }else{
            mMediaPlayer.start();
            reportSongStateChangedEvent(SongStateChangedEvent.EventType.RESUMED);
        }
    }

    private void stop() {
        if(currentSong != null){
            reportSongStateChangedEvent(SongStateChangedEvent.EventType.STOPPED);
        }
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(0);
        if(currentSong != null){
            publishProgress(0);
        }
    }

    private void seekTo(int offset) {
        mMediaPlayer.seekTo(offset);
    }

    private void redeliverStatus(){
        if(currentSong != null && mMediaPlayer != null){
            reportSongStateChangedEvent(mMediaPlayer.isPlaying()? SongStateChangedEvent.EventType.STARTED : SongStateChangedEvent.EventType.PAUSED);
            publishProgress(mMediaPlayer.getCurrentPosition());
        }
    }

    private void reportSongStateChangedEvent(SongStateChangedEvent.EventType eventType){
        EventBus.getDefault().post( new SongStateChangedEvent(currentSong, eventType));
    }


    private void publishProgress(int progress){
        if(EventBus.getDefault().hasSubscriberForEvent(SongProgressEvent.class)){
            EventBus.getDefault().post(new SongProgressEvent(currentSong, progress));
        }
    }

}
