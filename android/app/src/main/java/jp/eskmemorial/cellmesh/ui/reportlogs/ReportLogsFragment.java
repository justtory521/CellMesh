package jp.eskmemorial.cellmesh.ui.reportlogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import jp.eskmemorial.cellmesh.R;
import jp.eskmemorial.cellmesh.lib.SignalLogsManager;
import jp.eskmemorial.cellmesh.lib.data.SignalLog;

public class ReportLogsFragment extends Fragment {

    private final float NOT_PRIVATE_LOG_MARKER_COLOR = BitmapDescriptorFactory.HUE_RED;
    private final float PRIVATE_LOG_MARKER_COLOR = BitmapDescriptorFactory.HUE_AZURE;
    private final float NOT_PRIVATE_LOG_MARKER_ALPHA = 1;
    private final float PRIVATE_LOG_MARKER_ALPHA = 0.4f;
    private final double MARKER_CLICK_PRECISION = 0.000001;

    private ReportLogsViewModel reportLogsViewModel;
    private MapView mMap;
    private FloatingActionButton mButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        reportLogsViewModel =
                ViewModelProviders.of(this).get(ReportLogsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_report_logs, container, false);
        mButton = root.findViewById(R.id.report_logs_button);
        mMap = root.findViewById(R.id.past_logs_map);
        mMap.onCreate(savedInstanceState);
        mMap.onStart();
        initializeMap();
        return root;
    }

    private void initializeMap() {
        AtomicReference<HashMap<Marker, SignalLog>> markers = new AtomicReference<>(new HashMap<>());
        AtomicReference<ArrayList<SignalLog>> unreportedSignalLogs = new AtomicReference<>(new ArrayList<>());
        mMap.getMapAsync(map -> {
            Thread signalLogLoader = new Thread(() -> {
                unreportedSignalLogs.set(SignalLogsManager.loadUnreportedSignalLogs());
            });
            signalLogLoader.start();

            map.setOnMarkerClickListener(clickedMarker -> {
                for (Map.Entry<Marker, SignalLog> marker : markers.get().entrySet()) {
                    if (Math.abs(marker.getKey().getPosition().latitude - clickedMarker.getPosition().latitude) < MARKER_CLICK_PRECISION
                            && Math.abs(marker.getKey().getPosition().longitude - clickedMarker.getPosition().longitude) < MARKER_CLICK_PRECISION) {
                        if (markers.get().get(clickedMarker).getIsPrivateLog()) {
                            marker.getKey().setIcon(BitmapDescriptorFactory.defaultMarker(NOT_PRIVATE_LOG_MARKER_COLOR));
                            marker.getKey().setAlpha(NOT_PRIVATE_LOG_MARKER_ALPHA);
                            markers.get().get(marker.getKey()).setPrivateLog(false);
                        } else {
                            marker.getKey().setIcon(BitmapDescriptorFactory.defaultMarker(PRIVATE_LOG_MARKER_COLOR));
                            marker.getKey().setAlpha(PRIVATE_LOG_MARKER_ALPHA);
                            markers.get().get(marker.getKey()).setPrivateLog(true);
                        }
                    }
                }
                return true;
            });

            mButton.setOnClickListener(view -> {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.reportSignalLogsDialogTitle)
                        .setMessage(R.string.reportSignalLogsDiglogMessage)
                        .setPositiveButton(R.string.stopLocationUpdatesServiceDiglogPositiveButton, (dialog, which) -> {
                            ArrayList<SignalLog> signalLogsToReport = new ArrayList<>();
                            for (Map.Entry<Marker, SignalLog> marker : markers.get().entrySet()) {
                                signalLogsToReport.add(marker.getValue());
                            }
                            Thread report = new Thread(() -> {
                                SignalLogsManager.report(signalLogsToReport);
                            });
                            report.start();
                            try {
                                report.join();
                                Toast.makeText(getContext(), R.string.reportSignalLogsCompleted, Toast.LENGTH_SHORT).show();
                                HashMap<Marker, SignalLog> tmp = (HashMap<Marker, SignalLog>) markers.get().clone();
                                markers.set(new HashMap<>());
                                for (Map.Entry<Marker, SignalLog> marker : tmp.entrySet()) {
                                    if (marker.getValue().getIsPrivateLog()) {
                                        markers.get().put(marker.getKey(), marker.getValue());
                                    } else {
                                        marker.getKey().remove();
                                    }
                                }
                            } catch (InterruptedException e) {
                            }
                        })
                        .setNegativeButton(R.string.stopLocationUpdatesServiceDiglogNegativeButton, null)
                        .show();
            });

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            try {
                boolean isCameraInitialized = false;
                signalLogLoader.join();
                for (SignalLog signalLog : unreportedSignalLogs.get()) {
                    markers.get().put(map.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(signalLog.getIsPrivateLog() ? PRIVATE_LOG_MARKER_COLOR : NOT_PRIVATE_LOG_MARKER_COLOR))
                            .position(new LatLng(signalLog.getLatitude(), signalLog.getLongitude()))
                            .alpha(signalLog.getIsPrivateLog() ? PRIVATE_LOG_MARKER_ALPHA : NOT_PRIVATE_LOG_MARKER_ALPHA)
                    ), signalLog);
                    if (!isCameraInitialized) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(signalLog.getLatitude(), signalLog.getLongitude()), 12));
                        isCameraInitialized = true;
                    }
                }
            } catch (InterruptedException e) {
            }
        });
    }
}