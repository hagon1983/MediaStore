package com.aleksander.mediastore;

import com.aleksander.mediastore.data.Song;

/**
 * Created by alexander on 5/12/16.
 */
public class SongProgressEvent {
    final Song song;
    final int progress;

    public SongProgressEvent(Song song, int progress) {
        this.song = song;
        this.progress = progress;
    }

    public Song getSong() {
        return song;
    }

    public int getProgress() {
        return progress;
    }
}
