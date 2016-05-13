package com.aleksander.mediastore;

/**
 * Created by alexander on 5/13/16.
 */
public interface PermissionHelper {
    boolean hasPermissions(String... permissions);
    void askForPermissions(String... permissions);
}
