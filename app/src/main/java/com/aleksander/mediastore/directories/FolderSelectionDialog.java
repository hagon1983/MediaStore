package com.aleksander.mediastore.directories;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aleksander.mediastore.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexander on 5/12/16.
 */
public class FolderSelectionDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Collection<String>>  {

    public static final String KEY_RESULT = FolderSelectionDialog.class.getName() + ".KEY_RESULT";

    RecyclerView mRecyclerView;
    ProgressBar mProgressBar;
    FoldersAdapter adapter;

    public  interface onFragmentInteractionListener{
        void onPathSelected(String path);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_and_progress, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.list);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        adapter = new FoldersAdapter(listener, getString(R.string.option_all_folders));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        getDialog().setTitle(R.string.action_select_folder);

        return v;
    }

    final onFragmentInteractionListener listener = new onFragmentInteractionListener() {
        @Override
        public void onPathSelected(String path) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(KEY_RESULT, path);
            intent.putExtra(KEY_RESULT, path);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            dismiss();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Collection<String>> onCreateLoader(int id, Bundle args) {
        return new FoldersLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Collection<String>> loader, Collection<String> data) {
        adapter.setFolders(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Collection<String>> loader) {
    }


    public static class FoldersAdapter extends RecyclerView.Adapter<ViewHolder>{
        final onFragmentInteractionListener listener;
        final String allItemsLabel;

        final List<String> folders = new ArrayList<>();

        public FoldersAdapter(onFragmentInteractionListener listener, String allItemsLabel) {
            this.listener = listener;
            this.allItemsLabel = allItemsLabel;
        }

        public void setFolders(Collection<String> folders) {
            this.folders.clear();
            this.folders.add(allItemsLabel);
            this.folders.addAll(folders);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dialog_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.path = position> 0 ? folders.get(position) : null;
            holder.tv.setText(folders.get(position));
            holder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPathSelected(holder.path);
                }
            });
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView tv;
        String path;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView;
        }
    }
}
