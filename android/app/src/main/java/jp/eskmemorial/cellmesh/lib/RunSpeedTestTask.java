package jp.eskmemorial.cellmesh.lib;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.Executor;

class RunSpeedTestTask extends Task<JSONArray> {

    protected Context mContext;
    protected JSONArray mSpeedTestResult;
    protected Exception mException;
    protected ArrayList<OnSuccessListener<? super JSONArray>> mOnSuccessListeners = new ArrayList<>();
    protected ArrayList<OnFailureListener> mOnFailureListeners = new ArrayList<>();

    RunSpeedTestTask(Context context) {
        mContext = context;
    }

    RunSpeedTestTask run() {

        try {
            WebView webView = new WebView(mContext);
            webView.clearCache(true);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {

                    webView.evaluateJavascript("JSON.stringify(performance.getEntries());", (perf) -> {
                        perf = perf.substring(1, perf.length() - 1).replace("\\\"", "\"");
                        try {
                            mSpeedTestResult = new JSONArray(perf);
                        } catch (JSONException e) {
                        }

                        for (OnSuccessListener<? super JSONArray> listener : mOnSuccessListeners) {
                            listener.onSuccess(mSpeedTestResult);
                        }
                    });
                }
            });
            webView.loadUrl(LoggerConfig.getInstance().speedTestUrl.toString());

        } catch (Exception e) {
            mException = e;
            for (OnFailureListener listener : mOnFailureListeners) {
                listener.onFailure(e);
            }
        }
        return this;
    }

    @Override
    public boolean isComplete() {
        return isSuccessful() || isCanceled();
    }

    @Override
    public boolean isSuccessful() {
        return mSpeedTestResult != null;
    }

    @Override
    public boolean isCanceled() {
        return mException != null;
    }

    @Override
    public JSONArray getResult() {
        return mSpeedTestResult;
    }

    @Override
    public <X extends Throwable> JSONArray getResult(@NonNull Class<X> aClass) throws X {
        return getResult();
    }

    @Nullable
    @Override
    public Exception getException() {
        return mException;
    }

    @NonNull
    @Override
    public RunSpeedTestTask addOnSuccessListener(@NonNull OnSuccessListener<? super JSONArray> onSuccessListener) {
        mOnSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public RunSpeedTestTask addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super JSONArray> onSuccessListener) {
        mOnSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public RunSpeedTestTask addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super JSONArray> onSuccessListener) {
        mOnSuccessListeners.add(onSuccessListener);
        return this;
    }

    @NonNull
    @Override
    public RunSpeedTestTask addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
        mOnFailureListeners.add(onFailureListener);
        return this;
    }

    @NonNull
    @Override
    public RunSpeedTestTask addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
        mOnFailureListeners.add(onFailureListener);
        return this;
    }

    @NonNull
    @Override
    public RunSpeedTestTask addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
        mOnFailureListeners.add(onFailureListener);
        return this;
    }
}
