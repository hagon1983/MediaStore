package com.aleksander.mediastore.songs;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aleksander.mediastore.R;
import com.aleksander.mediastore.songs.SongFragment.OnSongFragmentInteractionListener;
import com.aleksander.mediastore.data.Song;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnSongFragmentInteractionListener}.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    private List<Song> mValues;
    private Song mActiveSong;
    private final OnSongFragmentInteractionListener mListener;

    public SongRecyclerViewAdapter(List<Song> items, OnSongFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    public void setValues(List<Song> values) {
        mValues = values;
    }

    public void setActiveSong(Song song) {
        int oldActive = mValues.indexOf(mActiveSong);
        int newActive = mValues.indexOf(song);
        mActiveSong = song;
        if (oldActive != newActive) {
            notifyItemChanged(oldActive);
            notifyItemChanged(newActive);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        final Song song = mValues.get(position);
        final Context context = holder.mView.getContext();
        final int duration = song.getDuration() / 1000;
        if (song.equals(mActiveSong)) {
            holder.icon.setImageResource(R.drawable.ic_song_playing);
        } else {
            holder.icon.setImageResource(R.drawable.ic_song);
        }
        holder.titleView.setText(song.getTitle());
        holder.duration.setText(context.getString(R.string.template_duration, duration / 60, duration % 60));
        holder.artist.setText(song.getArtist());
        holder.album.setText(song.getAlbum());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onSongClicked(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView icon;
        public final TextView titleView;
        public final TextView artist;
        public final TextView album;
        public final TextView duration;
        public Song mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            icon = (ImageView) view.findViewById(R.id.imgIcon);
            titleView = (TextView) view.findViewById(R.id.txtTitle);
            artist = (TextView) view.findViewById(R.id.txtArtist);
            album = (TextView) view.findViewById(R.id.txtAlbum);
            duration = (TextView) view.findViewById(R.id.txtDuration);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + artist.getText() + "'";
        }
    }
}
