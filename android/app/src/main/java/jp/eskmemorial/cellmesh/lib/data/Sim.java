package jp.eskmemorial.cellmesh.lib.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(foreignKeys = @ForeignKey(entity = SignalLog.class, parentColumns = "signalLogId", childColumns = "signalLogId", onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE))
public class Sim {
    @PrimaryKey
    @NonNull
    String simId;

    @NonNull
    String signalLogId;

    int slot;

    String mcc;
    String mnc;
    String carrier;
    String operator;

    int cid;
    int tac;

    @Ignore
    ArrayList<Cell> cells;

    boolean isActive;

    Sim() {
    }

    public Sim(int slot, String carrier, String operator, String mcc, String mnc, int cid, int tac, ArrayList<Cell> cells, boolean isActive) {
        this.slot = slot;
        this.carrier = carrier;
        this.operator = operator;
        this.mcc = mcc;
        this.mnc = mnc;
        this.cid = cid;
        this.tac = tac;
        this.cells = cells;
        this.isActive = isActive;
    }

    public int getSlot() {
        return slot;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getOperator() {
        return operator;
    }

    public String getMcc() {
        return mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public int getCid() {
        return cid;
    }

    public int getTac() {
        return tac;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public boolean getIsActive() {
        return isActive;
    }
}
