package jp.eskmemorial.cellmesh.lib.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

class SimWithCells {
    @Embedded
    Sim sim;
    @Relation(
            entity = Cell.class,
            parentColumn = "simId",
            entityColumn = "simId"
    )
    List<Cell> cells;

    SimWithCells() {
    }
}
