package jp.eskmemorial.cellmesh.lib;

import android.app.Activity;
import android.content.Context;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import jp.eskmemorial.cellmesh.lib.data.Cell;
import jp.eskmemorial.cellmesh.lib.data.Sim;

class TakeSimInfoSnapshotTask extends Task<ArrayList<Sim>> {
    private int mNumSims = -1;
    private ArrayList<Sim> mSims = new ArrayList<>();

    private Exception mException;
    ArrayList<OnSuccessListener<? super ArrayList<Sim>>> mSuccessListeners = new ArrayList<>();
    ArrayList<OnFailureListener> mFailureListeners = new ArrayList<>();

    //TODO LTE以外にも対応
    //TODO 異常系の処理
    TakeSimInfoSnapshotTask doTakeSnapshot(Context context) {
        try {
            List<SubscriptionInfo> subscriptions = context.getSystemService(SubscriptionManager.class).getActiveSubscriptionInfoList();
            mNumSims = subscriptions.size();
            for (SubscriptionInfo subscription : subscriptions) {
                TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class).createForSubscriptionId(subscription.getSubscriptionId());
                telephonyManager.requestCellInfoUpdate(context.getMainExecutor(), new TelephonyManager.CellInfoCallback() {
                    @Override
                    public void onCellInfo(@NonNull List<CellInfo> cellInfos) {
                        ArrayList<Cell> cells = new ArrayList<>();
                        int cid = Integer.MAX_VALUE;
                        int tac = Integer.MAX_VALUE;
                        for (CellInfo cellInfo : cellInfos) {
                            if (cellInfo instanceof CellInfoLte) {
                                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                                String generation = "LTE";
                                CellIdentityLte identity = cellInfoLte.getCellIdentity();
                                if (cellInfo.isRegistered()) {
                                    cid = identity.getCi();
                                    tac = identity.getTac();
                                }
                                CellSignalStrengthLte strength = cellInfoLte.getCellSignalStrength();
                                cells.add(new Cell(cellInfo.isRegistered(), generation,
                                        identity.getEarfcn(), identity.getBandwidth(), identity.getPci(),
                                        strength.getTimingAdvance(), strength.getCqi(), strength.getRsrp(), strength.getRssi(), strength.getRsrq(), strength.getRssnr()));
                            }
                        }
                        boolean isActive = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId()) == subscription.getSimSlotIndex() && !subscription.isOpportunistic();
                        mSims.add(new Sim(subscription.getSimSlotIndex(),
                                telephonyManager.getSimCarrierIdName().toString(),
                                telephonyManager.getNetworkOperatorName(), subscription.getMccString(), subscription.getMncString(),
                                cid, tac, cells, isActive));
                        if (mSims.size() == mNumSims) {
                            mSims.sort((sim1, sim2) -> sim1.getSlot() - sim2.getSlot());
                            for (OnSuccessListener<? super ArrayList<Sim>> successListener : mSuccessListeners) {
                                successListener.onSuccess(mSims);
                            }
                        }
                    }//end of callback
                });
            }
        } catch (SecurityException e) {
            mException = e;
        }
        return this;
    }

    @Override
    public boolean isComplete() {
        return isSuccessful() || isCanceled();
    }

    @Override
    public boolean isSuccessful() {
        return mNumSims != -1 && mSims.size() == mNumSims;
    }

    @Override
    public boolean isCanceled() {
        return mException != null;
    }

    @Nullable
    @Override
    public ArrayList<Sim> getResult() {
        return mSims;
    }

    @Nullable
    @Override
    public <X extends Throwable> ArrayList<Sim> getResult(@NonNull Class<X> aClass) throws X {
        return getResult();
    }

    @Nullable
    @Override
    public Exception getException() {
        return mException;
    }

    @NonNull
    @Override
    public TakeSimInfoSnapshotTask addOnSuccessListener(@NonNull OnSuccessListener<? super ArrayList<Sim>> onSuccessListener) {
        mSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public TakeSimInfoSnapshotTask addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super ArrayList<Sim>> onSuccessListener) {
        mSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public TakeSimInfoSnapshotTask addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super ArrayList<Sim>> onSuccessListener) {
        mSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public TakeSimInfoSnapshotTask addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
        mFailureListeners.add(onFailureListener);
        return this;
    }

    @NonNull
    @Override
    public TakeSimInfoSnapshotTask addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
        mFailureListeners.add(onFailureListener);
        return this;
    }

    @NonNull
    @Override
    public TakeSimInfoSnapshotTask addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
        mFailureListeners.add(onFailureListener);
        return this;
    }
}
