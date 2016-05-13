package com.aleksander.mediastore.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.aleksander.mediastore.songs.SongsOrderType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by alexander on 5/6/16.
 */
public class Repository {
    private static final String TAG = "Repository";
    private static final Uri EXTERNAL_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final Uri INTERNAL_URI = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
    final Context context;

    public Repository(Context context) {
        this.context = context.getApplicationContext();
    }

    public static class QueryBuilder {
        private SongsOrderType ordering = SongsOrderType.ALBUM;
        private Uri startUri = EXTERNAL_URI;
        private String folder = null;
        private String filter;
        private String directPath;

        private String selection;
        private String[] selectionArgs;

        public QueryBuilder setOrdering(SongsOrderType ordering) {
            this.ordering = ordering;
            return this;
        }

        public QueryBuilder setStartUri(Uri startUri) {
            this.startUri = startUri;
            return this;
        }

        public QueryBuilder setFolder(String folder) {
            this.folder = folder;
            return this;
        }

        public void setDirectPath(String directPath) {
            this.directPath = directPath;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public String getOrdering(){
            switch (ordering) {
                case ALBUM:
                    return MediaStore.Audio.AlbumColumns.ALBUM;
                case ARTIST:
                    return MediaStore.Audio.ArtistColumns.ARTIST;
                case RUNNING_TIME:
                    return MediaStore.Audio.Media.DURATION;
                default:
                    return MediaStore.Audio.Media.TITLE;
            }
        }

        public String getSelection() {
            if (selection == null) {
                calcSelectionAndArgs();
            }
            return selection;
        }

        public String[] getSelectionArgs() {
            if (selectionArgs == null) {
                calcSelectionAndArgs();
            }
            return selectionArgs;
        }

        private void calcSelectionAndArgs() {
            StringBuilder sbSelection = new StringBuilder();
            List<String> args = new ArrayList<>();


            if (!TextUtils.isEmpty(directPath)) {
                // we have the direct Uri, so query is used
                // to get details about the media
                sbSelection.append(MediaStore.Audio.Media.DATA);
                sbSelection.append(" like ?");
                args.add('%' + directPath + '%');
            }
            else{
                // we don't care about other filtering params in
                // case we have the direct Uri to the media
                if (!TextUtils.isEmpty(folder)) {
                    sbSelection.append(MediaStore.Audio.Media.DATA);
                    sbSelection.append(" like ?");
                    args.add(folder + '%');
                }

                if (!TextUtils.isEmpty(filter)) {
                    if (sbSelection.length() > 0) {
                        sbSelection.append(" AND ");
                    }
                    sbSelection.append(MediaStore.Audio.Media.TITLE);
                    sbSelection.append(" like ?");
                    args.add('%' + filter + '%');
                }

            }

            selection = sbSelection.toString();
            if (args.size() > 0) {
                selectionArgs = new String[args.size()];
                args.toArray(selectionArgs);
            }
        }

    }

    public Collection<String> getFoldersWithSongs() {
        return getFoldersWithSongs(EXTERNAL_URI);
    }

    public List<Song> getSongs(QueryBuilder queryBuilder) {
        List<Song> ret = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.AlbumColumns.ALBUM,
                MediaStore.Audio.Media.DURATION};
        Cursor c = context.getContentResolver().query(queryBuilder.startUri, projection, queryBuilder.getSelection(), queryBuilder.getSelectionArgs(), queryBuilder.getOrdering());
        if (c != null) {
            Log.d(TAG, "num songs: " + c.getCount());
            try {
                while (c.moveToNext()) {
                    Song model = new Song();
                    model.setUri(c.getString(0));
                    model.setTitle(c.getString(1));
                    model.setArtist(c.getString(2));
                    model.setAlbum(c.getString(3));
                    model.setDuration(c.getInt(4));
                    ret.add(model);
                }
            }finally {
                c.close();
            }
        }
        return ret;
    }

    private Collection<String> getFoldersWithSongs(@SuppressWarnings("SameParameterValue") Uri uri) {
        Collection<String> ret = new LinkedHashSet<>();
        String[] projection = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME};
        Cursor c = context.getContentResolver().query(uri, projection, null, null, MediaStore.Audio.Media.DATA);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    String data = c.getString(0);
                    String filename = c.getString(1);
                    String path = data.substring(0, data.length() - filename.length());
                    ret.add(path);
                }
            }finally {
                c.close();
            }
        }
        return ret;
    }
}
