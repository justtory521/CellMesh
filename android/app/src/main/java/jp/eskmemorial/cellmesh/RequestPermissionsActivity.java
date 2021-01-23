package jp.eskmemorial.cellmesh;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.ArrayUtils;

public class RequestPermissionsActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 48645;
    private final String[] mEssentialPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE};
    private final String[] mOptionalPermissions = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }
        requestPermission();
    }

    //TODO fine locationが許可されているときはbackground locationを要求しない
    private void requestPermission() {
        if (permissionsAcquired()) {
            finish();
        } else {
            requestPermissions(ArrayUtils.concat(mEssentialPermissions, mOptionalPermissions), PERMISSION_REQUEST_CODE);
        }
    }

    private boolean permissionsAcquired() {
        for (String permission : mEssentialPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
