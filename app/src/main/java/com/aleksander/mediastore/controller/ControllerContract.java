package com.aleksander.mediastore.controller;

import com.aleksander.mediastore.BasePresenter;
import com.aleksander.mediastore.BaseView;
import com.aleksander.mediastore.data.Song;

/**
 * Created by alexander on 5/10/16.
 */
public interface ControllerContract {
    interface Presenter extends BasePresenter{
        void playPrevious();
        void playPause();
        void stopPlaying();
        void playNext();
        void seekToPosition(int progress);
    }

    interface  View extends BaseView<ControllerContract.Presenter>{
        void ensureMaxProgress(int maxProgress);
        void updateProgress(int progress);
        void setSong(Song song);
        void updatePlayPauseButton(boolean isPlaying);
    }
}
