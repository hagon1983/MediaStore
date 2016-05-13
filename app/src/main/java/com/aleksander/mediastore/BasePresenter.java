package com.aleksander.mediastore;

/**
 * Created by alexander on 5/10/16.
 */
public interface BasePresenter {
    void onViewResumed(BaseView view);
    void onViewPaused();
}
