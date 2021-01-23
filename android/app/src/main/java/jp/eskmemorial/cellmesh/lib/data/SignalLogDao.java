package jp.eskmemorial.cellmesh.lib.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class SignalLogDao {

    public ArrayList<SignalLog> select(long timestampSmallerThan, int limit) {
        return toSignalLogs(_select(timestampSmallerThan, limit));
    }

    public ArrayList<SignalLog> selectUnreportedLogs() {
        return toSignalLogs(_selectUnreportedLogs());
    }

    @Transaction
    public void insertAll(ArrayList<SignalLog> all) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            ArrayList<SignalLog> signalLogs = new ArrayList<>();
            ArrayList<Sim> sims = new ArrayList<>();
            ArrayList<Cell> cells = new ArrayList<>();
            for (SignalLog sl : all) {
                String signalLogId = bytesToString(md.digest((String.valueOf(sl.timestamp) + String.valueOf(sl.latitude) + String.valueOf(sl.longitude)).getBytes(StandardCharsets.US_ASCII)));
                sl.signalLogId = signalLogId;
                signalLogs.add(sl);
                for (Sim sim : sl.getSims()) {
                    String simId = bytesToString(md.digest((signalLogId + String.valueOf(sim.slot)).getBytes(StandardCharsets.US_ASCII)));
                    sim.simId = simId;
                    sim.signalLogId = signalLogId;
                    sims.add(sim);
                    for (Cell cell : sim.getCells()) {
                        cell.simId = simId;
                        cells.add(cell);
                    }
                }
            }

            _insertSignalLogs(signalLogs);
            _insertSims(sims);
            _insertCells(cells);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void update(ArrayList<SignalLog> signalLogs) {
        ArrayList<SignalLog> sls = new ArrayList<>();
        for (SignalLog sl : signalLogs) {
            sls.add(sl);
        }
        _update(sls);
    }

    @Transaction
    @Delete
    public abstract void delete(SignalLog signalLog);

    @Transaction
    @Query("SELECT * FROM  SignalLog WHERE timestamp < :timestampSmallerThan ORDER BY timestamp DESC LIMIT :limit")
    abstract List<SignalLogWithSims> _select(long timestampSmallerThan, int limit);

    @Transaction
    @Query("SELECT * FROM  SignalLog WHERE isReported = 0")
    abstract List<SignalLogWithSims> _selectUnreportedLogs();

    @Insert
    abstract void _insertSignalLogs(ArrayList<SignalLog> signalLogs);

    @Insert
    abstract void _insertSims(ArrayList<Sim> sims);

    @Insert
    abstract void _insertCells(ArrayList<Cell> cells);

    @Transaction
    @Update
    abstract void _update(ArrayList<SignalLog> signalLogs);

    private ArrayList<SignalLog> toSignalLogs(List<SignalLogWithSims> withSims) {
        ArrayList<SignalLog> signalLogs = new ArrayList<>();
        for (SignalLogWithSims sl : withSims) {
            ArrayList<Sim> sims = new ArrayList<>();
            for (SimWithCells sim : sl.sims) {
                ArrayList<Cell> cells = new ArrayList<>();
                for (Cell cell : sim.cells) {
                    cells.add(cell);
                }
                sim.sim.cells = cells;
                sims.add(sim.sim);
            }
            sl.signalLog.sims = sims;
            signalLogs.add(sl.signalLog);
        }
        return signalLogs;
    }

    private String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
