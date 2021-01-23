package jp.eskmemorial.cellmesh.lib.data;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Entity
public class SignalLog {
    @PrimaryKey
    @NonNull
    String signalLogId;

    long timestamp;
    double longitude;
    double latitude;

    boolean isReported = false;
    boolean isPrivateLog = false;

    @TypeConverters({SignalLogSpeedTestResultTypeConverter.class})
    JSONArray speedTestResult;

    @Ignore
    ArrayList<Sim> sims;

    SignalLog() {
    }

    public SignalLog(Date date, Location location, boolean isReported, JSONArray speedTestResult, ArrayList<Sim> sims) {
        timestamp = date.getTime();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        this.isReported = isReported;
        this.speedTestResult = speedTestResult;

        this.sims = sims;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean getIsReported() {
        return isReported;
    }

    public void setIsReported(boolean isReported) {
        this.isReported = isReported;
    }

    public boolean getIsPrivateLog() {
        return isPrivateLog;
    }

    public void setPrivateLog(boolean isPrivateLog) {
        this.isPrivateLog = isPrivateLog;
    }

    public ArrayList<Sim> getSims() {
        return sims;
    }

    public String formatDate() {
        return new SimpleDateFormat("yy/MM/dd-kk:mm:ss").format(new Date(timestamp));
    }

    public double calculateBitrate() {
        double bitrate = -1;
        try {
            JSONArray json = new JSONArray(speedTestResult);
            bitrate = json.getJSONObject(0).getInt("transferSize") * 8
                    / json.getJSONObject(0).getDouble("responseEnd");
        } catch (JSONException e) {
        } finally {
            return bitrate;
        }
    }
}
