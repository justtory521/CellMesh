package jp.eskmemorial.cellmesh.lib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import jp.eskmemorial.cellmesh.R;

public class LocationUpdatesService extends Service {

    private int NOTIFICATION_ID = 20200806;
    private String NOTIFICATION_CHANNEL_NAME = "LocationUpdates";
    private String NOTIFICATION_CHANNEL_ID = "CELLMESH_LOCATION_UPDATES_SERVICE_NOTIFICATION_CHANNEL";

    private final IBinder mBinder = new LocalBinder();

    private NotificationManager mNotificationManager;

    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        CollectSignalLogTask signalLogManager = new CollectSignalLogTask(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();
                mNotificationManager.notify(NOTIFICATION_ID, createLocationUpdateNotification());
                signalLogManager.doCollectSignalLog(mLastLocation);
            }
        };

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel mChannel =
                new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        startForeground(NOTIFICATION_ID, createLocationUpdateNotification());
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public void startLocationUpdates() {
        try {
            setLocationRequest();
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } catch (SecurityException e) {
        }
    }

    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private Notification createLocationUpdateNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(new Date().toString())
                .setContentText("@" + mLastLocation.getLatitude() + " , " + mLastLocation.getLongitude())
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis());

        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        return builder.build();
    }

    private void setLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000 * LoggerConfig.getInstance().locationUpdateIntervalSec);
        mLocationRequest.setFastestInterval(1000 * LoggerConfig.getInstance().locationUpdateMinIntervalSec);
        mLocationRequest.setMaxWaitTime(1000 * LoggerConfig.getInstance().locationUpdateMaxIntervalSec);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }
}
