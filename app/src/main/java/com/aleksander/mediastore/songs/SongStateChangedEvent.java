package com.aleksander.mediastore.songs;

import com.aleksander.mediastore.data.Song;

/**
 * Created by alexander on 5/12/16.
 */
public class SongStateChangedEvent {
    public enum EventType{
        STARTED,
        PAUSED,
        RESUMED,
        STOPPED,
        COMPLETED;

        public boolean isPlaying(){
            return  this == STARTED || this == RESUMED;
        }
    }
    final Song mSong;
    final EventType mEventType;

    public SongStateChangedEvent(Song song, EventType eventType) {
        mSong = song;
        mEventType = eventType;
    }

    public Song getSong() {
        return mSong;
    }

    public EventType getEventType() {
        return mEventType;
    }
}
