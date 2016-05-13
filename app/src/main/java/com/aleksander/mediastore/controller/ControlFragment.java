package com.aleksander.mediastore.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aleksander.mediastore.R;
import com.aleksander.mediastore.data.Song;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ControlFragment extends Fragment implements ControllerContract.View{

    private ControllerContract.Presenter mPresenter;
    @BindView(R.id.seekBar)
    SeekBar mSeekBar;
    @BindView((R.id.txtProgress))
    TextView mTxtProgress;
    @BindView(R.id.txtTitle)
    TextView mTxtTitle;
    @BindView(R.id.txtArtist)
    TextView txtArtist;
    @BindView(R.id.txtAlbum)
    TextView txtAlbum;
    @BindView(R.id.btnPlayPause)
    ImageButton btnPlayPause;

    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        ButterKnife.bind(this, view);

        mSeekBar.setOnSeekBarChangeListener(lister);
        return view;
    }

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
    public void ensureMaxProgress(int maxProgress) {
        if(maxProgress != mSeekBar.getMax()){
            mSeekBar.setMax(maxProgress);
        }
    }

    @Override
    public void updateProgress(int progress) {
        mSeekBar.setProgress(progress);
        final int maxSeconds = mSeekBar.getMax() / 1000;
        final int nowSeconds = progress / 1000;

        mTxtProgress.setText(getString(R.string.template_duration_progress, nowSeconds/60, nowSeconds%60, maxSeconds/60, maxSeconds%60));
    }

    @Override
    public void setPresenter(ControllerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void setSong(Song song) {
        txtArtist.setText(getString(R.string.template_Artist, song.getArtist()));
        mTxtTitle.setText(getString(R.string.template_title, song.getTitle()));
        txtAlbum.setText(getString(R.string.template_Album, song.getAlbum()));
    }

    @Override
    public void updatePlayPauseButton(boolean isPlaying) {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.btn_pause : R.drawable.btn_play);
    }

    @OnClick(R.id.btnPrev)
    public void playPrevious(){
        mPresenter.playPrevious();
    }
    @OnClick(R.id.btnPlayPause)
    public void playPause(){
        mPresenter.playPause();
    }
    @OnClick(R.id.btnStop)
    public void stopPlaying(){
        mPresenter.stopPlaying();
    }
    @OnClick(R.id.btnNext)
    public void playNext(){
        mPresenter.playNext();
    }

    final SeekBar.OnSeekBarChangeListener lister = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                mPresenter.seekToPosition(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}
