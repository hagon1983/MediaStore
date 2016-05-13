package com.aleksander.mediastore.songs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.aleksander.mediastore.PermissionHelper;
import com.aleksander.mediastore.R;
import com.aleksander.mediastore.controller.ControlFragment;
import com.aleksander.mediastore.controller.ControllerContract;
import com.aleksander.mediastore.controller.ControllerPresenter;
import com.aleksander.mediastore.data.Repository;
import com.aleksander.mediastore.service.ServiceInteractor;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;

public class SongsActivity extends PermisoActivity implements PermissionHelper {

    private static SongsContract.Presenter songsPresenter;
    private static ControllerContract.Presenter controlPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        SongFragment songFragment = (SongFragment) getSupportFragmentManager().findFragmentById(R.id.flSongs);
        if (songFragment == null) {
            songFragment = SongFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.flSongs, songFragment)
                    .commit();
        }
        if (songsPresenter == null) {
            songsPresenter = new SongsPresenter(new Repository(getApplicationContext()), this);
        }
        songsPresenter.setPermissionHelper(this);
        songFragment.setPresenter(songsPresenter);


        ControlFragment controlFragment = (ControlFragment) getSupportFragmentManager().findFragmentById(R.id.fControls);
        if (controlPresenter == null) {
            controlPresenter = new ControllerPresenter(new ServiceInteractor(this), (SequenceController) songsPresenter);
        }
        controlFragment.setPresenter(controlPresenter);

        handleIncomingIntent();
    }

    private void handleIncomingIntent() {
        final Intent intent = getIntent();
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_VIEW)) {
            final Uri uri = intent.getData();
            if (uri != null) {
                songsPresenter.setDirectPath(uri.getPath());
            }
            intent.setData(null);
        }
    }

    @Override
    public boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void askForPermissions(String... permissions) {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
//                if (resultSet.areAllPermissionsGranted()) {
//                } else {
//                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                Permiso.getInstance().showRationaleInDialog(getString(R.string.permission_request_title), combineRationale(permissions), null, callback);
            }
        }, permissions);
    }

    private String combineRationale(String... permissions) {
        StringBuilder sb = new StringBuilder();
        for (String permission : permissions) {
            if (Manifest.permission.READ_EXTERNAL_STORAGE.equalsIgnoreCase(permission)) {
                sb.append(getString(R.string.permission_read_external_rationale));
            }
        }
        return sb.toString();
    }
}
