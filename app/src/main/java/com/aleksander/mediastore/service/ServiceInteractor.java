package com.aleksander.mediastore.service;

import android.content.Context;

import com.aleksander.mediastore.data.Song;

/**
 * Created by alexander on 5/10/16.
 */
public class ServiceInteractor {
    private final Context mContext;

    public ServiceInteractor(Context context) {
        mContext = context.getApplicationContext();
    }

    public void startSong(Song song){
        mContext.startService(MusicService.getStartCommand(mContext,song));
    }

    public void playPauseSong(Song song){
        mContext.startService(MusicService.getPauseCommand(mContext,song));
    }

    public void stopPlayer(){
        mContext.startService(MusicService.getStopCommand(mContext));
    }

    public void seekToPosition(int position){
        mContext.startService(MusicService.getSeekCommand(mContext, position));
    }

    public void redeliverStatus(){
        mContext.startService(MusicService.getRedeliverStatusCommand(mContext));
    }
}
