package com.aleksander.mediastore.songs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.aleksander.mediastore.BaseView;
import com.aleksander.mediastore.PermissionHelper;
import com.aleksander.mediastore.data.Repository;
import com.aleksander.mediastore.data.Song;
import com.aleksander.mediastore.service.ServiceInteractor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by alexander on 5/10/16.
 */
public class SongsPresenter implements SongsContract.Presenter, SequenceController {
    private final Context mContext;
    private SongsContract.View mView;
    private final Repository mRepository;
    private WeakReference<PermissionHelper> mHelperWeakReference;
    private SongsOrderType mOrdering = SongsOrderType.ARTIST;
    private String filter;
    private String directPath;
    private String directory;
    private List<Song> mSongs;
    private Song mPlayingSong;
    private Song mLastPlayedSong;

    public SongsPresenter(Repository repository, Context context) {
        mRepository = repository;
        mContext = context.getApplicationContext();
    }

    @Override
    public void onViewResumed(BaseView view) {
        mView = (SongsContract.View) view;
        if (mSongs == null || !TextUtils.isEmpty(directPath)) {
            loadSongs();
        } else {
            mView.showSongs(mSongs);
        }

        if (mPlayingSong != null) {
            mView.setPlayingSong(mPlayingSong);
        }

        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewPaused() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setPermissionHelper(PermissionHelper permissionHelper) {
        mHelperWeakReference = new WeakReference<>(permissionHelper);
    }

    @Override
    public void setFilter(String filter) {
        if (!TextUtils.equals(this.filter, filter)) {
            this.filter = filter;
            loadSongs();
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    public void loadSongs() {
        PermissionHelper helper = mHelperWeakReference.get();
        if (helper == null) {
            return;
        }
        if (!helper.hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            helper.askForPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
            return;
        }
        if (mView != null) {
            mView.showProgress();
        }
        new AsyncTask<Void, Void, List<Song>>() {
            @Override
            protected List<Song> doInBackground(Void... params) {
                Repository.QueryBuilder queryBuilder = new Repository.QueryBuilder();
                queryBuilder.setFilter(filter);
                queryBuilder.setOrdering(mOrdering);
                queryBuilder.setDirectPath(directPath);
                queryBuilder.setFolder(directory);
                return mRepository.getSongs(queryBuilder);
            }

            @Override
            protected void onPostExecute(List<Song> songs) {
                mSongs = songs;
                if (!TextUtils.isEmpty(directPath)) {
                    directPath = null;
                    startSong(songs.get(0));
                }
                if (mView != null) {
                    mView.showSongs(mSongs);
                }
            }
        }.execute();
    }

    @Override
    public void onShowFolderSelect() {
        PermissionHelper helper = mHelperWeakReference.get();
        if (helper == null) {
            return;
        }
        if (!helper.hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            helper.askForPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
            return;
        }

        if (mView != null) {
            mView.showFolderSelect();
        }

    }

    @Override
    public void startSong(Song song) {
        ServiceInteractor interactor = new ServiceInteractor(mContext);
        interactor.startSong(song);
    }

    @Override
    public void setOrdering(SongsOrderType orderType) {
        if (mOrdering != orderType) {
            mOrdering = orderType;
            loadSongs();
        }
    }

    @Override
    public void setFolder(String folder) {
        if (!TextUtils.equals(this.directory, folder)) {
            this.directory = folder;
            loadSongs();
        }
    }

    @Override
    public void setDirectPath(String path) {
        directPath = path;
    }

    @Override
    public void playPrevious() {
        if (mSongs != null) {
            final int currPos = mSongs.indexOf(mLastPlayedSong);
            startSong(mSongs.get(getNextSongPosition(currPos, mSongs.size(), false)));
        }
    }

    @Override
    public void playNext() {
        if (mSongs != null) {
            final int currPos = mSongs.indexOf(mLastPlayedSong);
            startSong(mSongs.get(getNextSongPosition(currPos, mSongs.size(), true)));
        }
    }

    @Override
    public void playAny() {
        if(mPlayingSong != null){
            return;
        }
        if(mSongs != null){
            startSong(mSongs.get(getNextSongPosition(-1, mSongs.size(), false)));
        }
    }

    /**
     * function to determ the next song to play
     *
     * @param currPos - position of the currently active song
     * @param size    - num of songs
     * @param up      - true-next, false - previous
     */
    // TODO: convert 'up' to enum and utilize the Algorithm pattern ?
    private int getNextSongPosition(int currPos, int size, boolean up) {
        if (currPos == -1) {
            return 0;
        }
        int pos = currPos + (up ? 1 : -1);
        if (pos >= 0 && pos < size) {
            return pos;
        }
        return 0;
    }

    @Subscribe
    public void onSongEvent(SongStateChangedEvent event) {
        if(event.getEventType().isPlaying()){
            mPlayingSong = event.getSong();
            mLastPlayedSong = event.getSong();
        }else{
            mPlayingSong = null;
        }
        if (mView != null) {
            mView.setPlayingSong(mPlayingSong);
        }
    }

}
