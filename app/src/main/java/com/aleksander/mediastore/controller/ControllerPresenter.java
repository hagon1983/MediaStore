package com.aleksander.mediastore.controller;

import com.aleksander.mediastore.BaseView;
import com.aleksander.mediastore.SongProgressEvent;
import com.aleksander.mediastore.data.Song;
import com.aleksander.mediastore.service.ServiceInteractor;
import com.aleksander.mediastore.songs.SequenceController;
import com.aleksander.mediastore.songs.SongStateChangedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by alexander on 5/10/16.
 */
public class ControllerPresenter implements ControllerContract.Presenter {
    private ControllerContract.View mView;
    private final ServiceInteractor mServiceInteractor;
    private final SequenceController mSequenceController;
    private Song mSong;

    public ControllerPresenter(ServiceInteractor serviceInteractor, SequenceController sequenceController) {
        mServiceInteractor = serviceInteractor;
        mSequenceController = sequenceController;
    }

    @Override
    public void playPrevious() {
        mSequenceController.playPrevious();
    }

    @Override
    public void playPause() {
        if(mSong == null){
            mSequenceController.playAny();
        }else{
            mServiceInteractor.playPauseSong(mSong);
        }
    }

    @Override
    public void stopPlaying() {
        mServiceInteractor.stopPlayer();
    }

    @Override
    public void playNext() {
        mSequenceController.playNext();
    }

    @Override
    public void seekToPosition(int progress) {
        mServiceInteractor.seekToPosition(progress);
    }

    @Override
    public void onViewResumed(BaseView view) {
        mView = (ControllerContract.View) view;
        if(mSong != null){
            mView.setSong(mSong);
        }
        EventBus.getDefault().register(this);
        mServiceInteractor.redeliverStatus();
    }

    @Override
    public void onViewPaused() {
        EventBus.getDefault().unregister(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSongEvent(SongStateChangedEvent event){
        final boolean isPlaying = event.getEventType().isPlaying();

        if(isPlaying){
            mSong = event.getSong();
        }

        if(mView != null){
            mView.updatePlayPauseButton(isPlaying);

            if(event.getEventType().equals(SongStateChangedEvent.EventType.STARTED)){
                mView.ensureMaxProgress(event.getSong().getDuration());
                mView.updateProgress(0);
                mView.setSong(event.getSong());
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSongProgress(SongProgressEvent event){
        if(mView != null){
            mView.ensureMaxProgress(event.getSong().getDuration());
            mView.updateProgress(event.getProgress());
        }
    }
}
