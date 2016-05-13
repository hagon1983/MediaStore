package com.aleksander.mediastore.directories;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.aleksander.mediastore.data.Repository;

import java.util.Collection;

/**
 * Created by alexander on 5/13/16.
 */
public class FoldersLoader extends AsyncTaskLoader<Collection<String>> {
    private Collection<String> mData;

    public FoldersLoader(Context ctx) {
        super(ctx);
    }

    @Override
    public Collection<String> loadInBackground() {
        Repository repository = new Repository(getContext());
        return repository.getFoldersWithSongs();
    }

    @Override
    public void deliverResult(Collection<String> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            data = null;
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        Collection<String> oldData = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            oldData = null;
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        if (mData != null) {
            mData = null;
        }
    }

    @Override
    public void onCanceled(Collection<String> data) {
        super.onCanceled(data);
        data = null;
    }
}

