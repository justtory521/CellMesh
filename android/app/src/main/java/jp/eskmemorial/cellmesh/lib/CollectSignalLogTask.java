package jp.eskmemorial.cellmesh.lib;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;

import jp.eskmemorial.cellmesh.lib.data.SignalLog;
import jp.eskmemorial.cellmesh.lib.data.Sim;

class CollectSignalLogTask extends Task<SignalLog> {

    Context mContext;
    SignalLog mLastCellInfoSignalLog;
    SignalLog mLastSpeedTestSignalLog;

    Exception mException;
    private ArrayList<OnSuccessListener<? super SignalLog>> mOnSuccessListeners = new ArrayList<>();
    private ArrayList<OnFailureListener> mOnFailureListeners = new ArrayList<>();

    CollectSignalLogTask(Context context) {
        mContext = context;
    }

    //TODO 異常系の処理
    CollectSignalLogTask doCollectSignalLog(Location currentLocation) {
        Date currentDate = new Date();

        if (!needToCollectCellInfo(currentDate, currentLocation)) {
            return this;
        }

        new TakeSimInfoSnapshotTask()
                .addOnSuccessListener(sims -> {
                    if (needToRunSpeedTest(currentDate, currentLocation, getActiveCid(sims))) {
                        new RunSpeedTestTask(mContext)
                                .addOnSuccessListener(speedTestResult -> {
                                    mLastSpeedTestSignalLog = new SignalLog(currentDate, currentLocation, false, speedTestResult, sims);
                                    mLastCellInfoSignalLog = mLastSpeedTestSignalLog;
                                    SignalLogsManager.addNewSignalLog(mLastSpeedTestSignalLog);
                                    for (OnSuccessListener listener : mOnSuccessListeners) {
                                        listener.onSuccess(mLastSpeedTestSignalLog);
                                    }
                                })
                                .run();
                    } else {
                        mLastCellInfoSignalLog = new SignalLog(currentDate, currentLocation, false, null, sims);
                        SignalLogsManager.addNewSignalLog(mLastCellInfoSignalLog);
                        for (OnSuccessListener listener : mOnSuccessListeners) {
                            listener.onSuccess(mLastCellInfoSignalLog);
                        }
                    }
                })
                .doTakeSnapshot(mContext);
        return this;
    }

    private boolean needToCollectCellInfo(Date currentDate, Location currentLocation) {
        if (mLastCellInfoSignalLog == null) {
            return true;
        }

        if (currentDate.getTime() - mLastCellInfoSignalLog.getTimestamp() >= 1000 * LoggerConfig.getInstance().collectCellsMinIntervalSec) {
            return true;
        }

        float[] results = new float[]{3};
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), mLastCellInfoSignalLog.getLatitude(), mLastCellInfoSignalLog.getLongitude(), results);
        if (results[0] >= LoggerConfig.getInstance().collectCellsMinDistanceMeter) {
            return true;
        }
        return false;
    }

    private boolean needToRunSpeedTest(Date currentDate, Location currentLocation, int cid) {
        if (!LoggerConfig.getInstance().runSpeedTest) {
            return false;
        }

        if (mLastSpeedTestSignalLog == null) {
            return true;
        }

        if (mLastCellInfoSignalLog == null || mLastCellInfoSignalLog.getTimestamp() < mLastSpeedTestSignalLog.getTimestamp()) {
            if (cid != getActiveCid(mLastSpeedTestSignalLog.getSims())) {
                return true;
            }
        } else {
            if (cid != getActiveCid(mLastCellInfoSignalLog.getSims())) {
                return true;
            }
        }

        if (currentDate.getTime() - mLastSpeedTestSignalLog.getTimestamp() >= 1000 * LoggerConfig.getInstance().runSpeedTestMinIntervalSec) {
            return true;
        }

        float[] results = new float[]{3};
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), mLastSpeedTestSignalLog.getLatitude(), mLastSpeedTestSignalLog.getLongitude(), results);

        if (results[0] >= LoggerConfig.getInstance().runSpeedTestMinDistanceMeter) {
            return true;
        }
        return false;
    }

    //TODO throw exception
    private int getActiveCid(ArrayList<Sim> sims) {
        for (Sim sim : sims) {
            if (sim.getIsActive()) {
                return sim.getCid();
            }
        }
        return -1;
    }

    @Override
    public boolean isComplete() {
        return isSuccessful() || isCanceled();
    }

    //TODO 間違ってる
    @Override
    public boolean isSuccessful() {
        return mLastCellInfoSignalLog != null || mLastSpeedTestSignalLog != null;
    }

    //TODO 間違ってる
    @Override
    public boolean isCanceled() {
        return mException != null;
    }

    @Override
    public SignalLog getResult() {
        if (mLastCellInfoSignalLog.getTimestamp() - mLastSpeedTestSignalLog.getTimestamp() > 0) {
            return mLastCellInfoSignalLog;
        } else {
            return mLastSpeedTestSignalLog;
        }
    }

    @Override
    public <X extends Throwable> SignalLog getResult(@NonNull Class<X> aClass) throws X {
        return getResult();
    }

    @Nullable
    @Override
    public Exception getException() {
        return mException;
    }

    @NonNull
    @Override
    public CollectSignalLogTask addOnSuccessListener(@NonNull OnSuccessListener<? super SignalLog> onSuccessListener) {
        mOnSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public CollectSignalLogTask addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super SignalLog> onSuccessListener) {
        mOnSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public CollectSignalLogTask addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super SignalLog> onSuccessListener) {
        mOnSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public CollectSignalLogTask addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
        mOnFailureListeners.add(onFailureListener);
        return this;
    }

    @NonNull
    @Override
    public CollectSignalLogTask addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
        mOnFailureListeners.add(onFailureListener);
        return this;
    }

    @NonNull
    @Override
    public CollectSignalLogTask addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
        mOnFailureListeners.add(onFailureListener);
        return this;
    }
}
