package com.aleksander.mediastore.songs;

import com.aleksander.mediastore.BasePresenter;
import com.aleksander.mediastore.BaseView;
import com.aleksander.mediastore.PermissionHelper;
import com.aleksander.mediastore.data.Song;

import java.util.List;

/**
 * Created by alexander on 5/10/16.
 */
public interface SongsContract {

    interface View extends BaseView<Presenter>{
        void showProgress();
        void showSongs(List<Song> songs);
        void setPlayingSong(Song song);
        void showOrderingPopup();
        void showFolderSelect();
    }

    interface Presenter extends BasePresenter{
        void setPermissionHelper(PermissionHelper permissionHelper);
        void loadSongs();
        void startSong(Song song);
        void setFilter(String filter);
        void setOrdering(SongsOrderType orderType);
        void setDirectPath(String path);
        void setFolder(String folder);
        void onShowFolderSelect();
    }
}
