package jp.eskmemorial.cellmesh.ui.pastlogs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PastLogsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PastLogsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}