package jp.eskmemorial.cellmesh.lib.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

class SignalLogWithSims {
    @Embedded
    SignalLog signalLog;
    @Relation(
            entity = Sim.class,
            parentColumn = "signalLogId",
            entityColumn = "signalLogId"
    )
    List<SimWithCells> sims;

    SignalLogWithSims() {
    }
}
