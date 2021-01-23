package jp.eskmemorial.cellmesh.lib;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class LocationUpdatesServiceConnection implements ServiceConnection {

    private boolean mIsRunning = false;
    private LocationUpdatesService mService;

    public LocationUpdatesServiceConnection() {
    }

    public void start() {
        mService.startLocationUpdates();
        mIsRunning = true;
    }

    public void stop() {
        mService.stopLocationUpdates();
        mIsRunning = false;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
        mService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }
}
