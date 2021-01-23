package jp.eskmemorial.cellmesh.ui.reportlogs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReportLogsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ReportLogsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}