package jp.eskmemorial.cellmesh.lib;

import android.content.Context;

import androidx.room.Room;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

import jp.eskmemorial.cellmesh.lib.data.SignalLog;
import jp.eskmemorial.cellmesh.lib.data.SignalLogRoomDatabase;

public class SignalLogsManager {
    private static SignalLogsManager mInstance;
    private ArrayList<SignalLog> mSignalLogs = new ArrayList<>();
    private ArrayList<Consumer<SignalLog>> mOnAddLatestSignalLogListener = new ArrayList<>();

    SignalLogRoomDatabase mDb;

    private SignalLogsManager(Context context) {
        mDb = Room.databaseBuilder(context, SignalLogRoomDatabase.class, "SignalLog").build();
    }

    public static void createInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SignalLogsManager(context);
            loadSignalLogs(1);
        }
    }

    public static void addNewSignalLog(SignalLog newSignalLog) {
        mInstance.mSignalLogs.add(newSignalLog);
        new Thread(() -> {
            mInstance.save(newSignalLog);
        }).start();
        for (Consumer<SignalLog> listener : mInstance.mOnAddLatestSignalLogListener) {
            listener.accept(newSignalLog);
        }
    }

    public static void addOnAddNewSignalLogListener(Consumer<SignalLog> listener) {
        mInstance.mOnAddLatestSignalLogListener.add(listener);
    }

    public static SignalLog getLatestSignalLog() {
        if (mInstance.mSignalLogs.size() == 0) {
            return null;
        }
        return mInstance.mSignalLogs.get(mInstance.mSignalLogs.size() - 1);
    }

    public static ArrayList<SignalLog> getCachedSignalLogs() {
        return mInstance.mSignalLogs;
    }

    public static ArrayList<SignalLog> loadSignalLogs(int num) {
        if (mInstance.mSignalLogs.size() < num) {
            int limit = num - mInstance.mSignalLogs.size();

            long timestampSmallerThan = new Date().getTime();
            if (mInstance.mSignalLogs.size() > 0) {
                timestampSmallerThan = mInstance.mSignalLogs.get(0).getTimestamp();
            }
            mInstance.mSignalLogs.addAll(mInstance.mDb.signalLogDao().select(timestampSmallerThan, limit));

            mInstance.mSignalLogs.sort((o1, o2) -> {
                if (o1.getTimestamp() > o2.getTimestamp()) {
                    return 1;
                }
                if (o1.getTimestamp() < o2.getTimestamp()) {
                    return -1;
                }
                return 0;
            });
        }
        return mInstance.mSignalLogs;
    }

    public static ArrayList<SignalLog> loadUnreportedSignalLogs() {
        return mInstance.mDb.signalLogDao().selectUnreportedLogs();
    }

    public static void report(ArrayList<SignalLog> signalLogs) {
        try {
            ArrayList<SignalLog> signalLogsToReports = new ArrayList<>();
            for (SignalLog sl : signalLogs) {
                if (!sl.getIsPrivateLog()) {
                    signalLogsToReports.add(sl);
                }
            }
            HttpURLConnection connection = (HttpURLConnection) LoggerConfig.getInstance().resultReportUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            OutputStream httpBody = connection.getOutputStream();
            httpBody.write(("signalLogs=" + new Gson().toJson(signalLogsToReports)).getBytes(StandardCharsets.UTF_8));
            httpBody.flush();
            int status = connection.getResponseCode();
            connection.disconnect();

            if (status == HttpURLConnection.HTTP_OK) {
                for (SignalLog sl : signalLogs) {
                    if (!sl.getIsPrivateLog()) {
                        sl.setIsReported(true);
                    }
                }
                mInstance.mDb.signalLogDao().update(signalLogs);
            }
        } catch (IOException e) {
        }
    }

    public static void delete(SignalLog signalLog) {
        mInstance.mDb.signalLogDao().delete(signalLog);
        mInstance.mSignalLogs.remove(signalLog);
    }

    private void save(ArrayList<SignalLog> signalLogs) {
        mDb.signalLogDao().insertAll(signalLogs);
    }

    private void save(SignalLog signalLog) {
        ArrayList<SignalLog> sls = new ArrayList<>();
        sls.add(signalLog);
        save(sls);
    }
}
