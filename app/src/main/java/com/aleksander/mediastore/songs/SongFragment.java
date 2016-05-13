package com.aleksander.mediastore.songs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.aleksander.mediastore.R;
import com.aleksander.mediastore.data.Song;
import com.aleksander.mediastore.directories.FolderSelectionDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongFragment extends Fragment implements SongsContract.View, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public static final int REQUEST_CHOOSE_FOLDER = 3;

    private static final String KEY_QUERY = "query";

    private SongsContract.Presenter mPresenter;
    private SongRecyclerViewAdapter mAdapter = null;

    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    private SearchView searchView;
    private CharSequence initialQuery;


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mPresenter.setFilter(newText);
        return true;
    }

    @Override
    public boolean onClose() {
        mPresenter.setFilter(null);
        return true;
    }

    public interface OnSongFragmentInteractionListener {
        void onSongClicked(Song song);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!searchView.isIconified()){
            outState.putCharSequence(KEY_QUERY, searchView.getQuery());
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongFragment() {
    }

    public static SongFragment newInstance() {
        return new SongFragment();
    }

    private final OnSongFragmentInteractionListener mListener = new OnSongFragmentInteractionListener() {
        @Override
        public void onSongClicked(Song song) {
            mPresenter.startSong(song);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onViewResumed(this);
    }

    @Override
    public void onPause() {
        mPresenter.onViewPaused();
        super.onPause();
    }

    @Override
    public void setPresenter(SongsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_and_progress, container, false);
        ButterKnife.bind(this, view);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mAdapter = new SongRecyclerViewAdapter(new ArrayList<Song>(), mListener);
            mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_QUERY)){
            initialQuery = savedInstanceState.getCharSequence(KEY_QUERY);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.songs_fragment, menu);
        MenuItem search=menu.findItem(R.id.menu_search);
        searchView= (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.setIconifiedByDefault(true);
        searchView.setQueryHint(getString(R.string.action_filter));

        if (initialQuery != null) {
            searchView.setIconified(false);
            search.expandActionView();
            searchView.setQuery(initialQuery, false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort:
                showOrderingPopup();
                return true;
            case R.id.menu_select_folder:
                mPresenter.onShowFolderSelect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showProgress() {
//        mRecyclerView.setVisibility(View.GONE);
//        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSongs(List<Song> songs) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mAdapter.setValues(songs);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void setPlayingSong(Song song) {
        mAdapter.setActiveSong(song);
    }

    @Override
    public void showOrderingPopup() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_sort));
        popup.getMenuInflater().inflate(R.menu.songs_sort_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.album:
                        mPresenter.setOrdering(SongsOrderType.ALBUM);
                        break;
                    case R.id.title:
                        mPresenter.setOrdering(SongsOrderType.TITLE);
                        break;
                    case R.id.author:
                        mPresenter.setOrdering(SongsOrderType.ARTIST);
                    default:
                        mPresenter.setOrdering(SongsOrderType.RUNNING_TIME);
                        break;
                }
                mPresenter.loadSongs();
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void showFolderSelect() {
        FolderSelectionDialog dlg = new FolderSelectionDialog();
        dlg.setTargetFragment(this, REQUEST_CHOOSE_FOLDER);
        dlg.show(getFragmentManager(), "FolderSelectionDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CHOOSE_FOLDER){
            if(resultCode == Activity.RESULT_OK){
                mPresenter.setFolder(data.getStringExtra(FolderSelectionDialog.KEY_RESULT));
            }
        }
    }
}
