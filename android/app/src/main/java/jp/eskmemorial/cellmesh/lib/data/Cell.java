package jp.eskmemorial.cellmesh.lib.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = Sim.class, parentColumns = "simId", childColumns = "simId", onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE))
public class Cell {
    @PrimaryKey(autoGenerate = true)
    long primaryKey;

    @NonNull
    String simId;

    boolean isRegistered;

    String generation;
    int earfcn;
    int pci;

    int bandwidth;
    int timingAdvance;

    int cqi;
    int rsrp;
    int rssi;
    int rsrq;
    int rssnr;

    public Cell(boolean isRegistered, String generation, int earfcn, int bandwidth, int pci, int timingAdvance, int cqi, int rsrp, int rssi, int rsrq, int rssnr) {
        this.isRegistered = isRegistered;
        this.generation = generation;
        this.earfcn = earfcn;
        this.pci = pci;
        this.bandwidth = bandwidth;
        this.timingAdvance = timingAdvance;
        this.cqi = cqi;
        this.rsrp = rsrp;
        this.rssi = rssi;
        this.rsrq = rsrq;
        this.rssnr = rssnr;
    }

    public boolean getIsRegistered() {
        return isRegistered;
    }

    public String getGeneration() {
        return generation;
    }

    public int getEarfcn() {
        return earfcn;
    }

    public int getPci() {
        return pci;
    }

    public int getTimingAdvance() {
        return timingAdvance;
    }

    public int getCqi() {
        return cqi;
    }

    public int getRsrp() {
        return rsrp;
    }

    public int getRssi() {
        return rssi;
    }

    public int getRsrq() {
        return rsrq;
    }

    public int getRssnr() {
        return rssnr;
    }
}
