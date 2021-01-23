package jp.eskmemorial.cellmesh.lib.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SignalLog.class, Sim.class, Cell.class}, version = 3, exportSchema = false)
public abstract class SignalLogRoomDatabase extends RoomDatabase {
    public abstract SignalLogDao signalLogDao();
}
