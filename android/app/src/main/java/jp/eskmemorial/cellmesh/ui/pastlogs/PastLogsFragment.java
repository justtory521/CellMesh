package jp.eskmemorial.cellmesh.ui.pastlogs;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.function.Consumer;

import jp.eskmemorial.cellmesh.R;
import jp.eskmemorial.cellmesh.lib.SignalLogsManager;
import jp.eskmemorial.cellmesh.lib.data.Cell;
import jp.eskmemorial.cellmesh.lib.data.SignalLog;
import jp.eskmemorial.cellmesh.lib.data.Sim;

public class PastLogsFragment extends Fragment {

    private PastLogsViewModel pastLogsViewModel;
    private MapView mMap;
    private static boolean isMapReadyFirstTime = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        pastLogsViewModel =
                ViewModelProviders.of(this).get(PastLogsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_past_logs, container, false);
        mMap = root.findViewById(R.id.past_logs_map);
        mMap.onCreate(savedInstanceState);
        mMap.onStart();
        initializeMap();
        return root;
    }

    private void initializeMap() {
        mMap.getMapAsync(map -> {
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    LinearLayout info = new LinearLayout(getContext());
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(getContext());
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(getContext());
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });

            ArrayList<Marker> markers = new ArrayList<>();

            map.setOnMapLongClickListener(
                    latLng -> {
                        double distSq = Double.MAX_VALUE;
                        Marker nearest = markers.get(0);
                        for (Marker marker : markers) {
                            double tmpDistSq = Math.pow(marker.getPosition().latitude - latLng.latitude, 2) + Math.pow(marker.getPosition().longitude - latLng.longitude, 2);
                            if (distSq > tmpDistSq) {
                                nearest = marker;
                                distSq = tmpDistSq;
                            }
                        }
                        if (distSq < 0.0001) {
                            SignalLog target = (SignalLog) nearest.getTag();
                            Marker finalNearest = nearest;
                            new AlertDialog.Builder(getContext())
                                    .setTitle(R.string.reportSignalLogsDialogTitle)
                                    .setMessage(R.string.reportSignalLogsDiglogMessage)
                                    .setPositiveButton(R.string.stopLocationUpdatesServiceDiglogPositiveButton, (dialog, which) -> {
                                        Thread deleter = new Thread(() -> {
                                            SignalLogsManager.delete(target);
                                        });
                                        deleter.start();
                                        try {
                                            deleter.join();
                                            finalNearest.remove();
                                        } catch (InterruptedException e) {
                                        }
                                    })
                                    .setNegativeButton(R.string.stopLocationUpdatesServiceDiglogNegativeButton, null)
                                    .show();
                        }
                    }
            );

            Consumer<SignalLog> addMarker = signalLog -> {
                StringBuilder snippet = new StringBuilder();
                for (Sim sim : signalLog.getSims()) {
                    snippet.append("${cid}, ${tac}\n".replace("${cid}", String.valueOf(sim.getCid())).replace("${tac}", String.valueOf(sim.getTac())));
                    snippet.append("${carrier}, ${operator}, ${mcc}/${mnc}\n".replace("${carrier}", sim.getCarrier()).replace("${operator}", sim.getOperator()).replace("${mcc}", sim.getMcc()).replace("${mnc}", sim.getMnc()));
                    for (Cell cell : sim.getCells()) {
                        snippet.append("${pci}@${earfcn}, ".replace("${pci}", String.valueOf(cell.getPci())).replace("${earfcn}", String.valueOf(cell.getEarfcn())));
                    }
                    snippet.append("\n");
                }

                Marker marker = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(new LatLng(signalLog.getLatitude(), signalLog.getLongitude()))
                        .title(signalLog.formatDate())
                        .snippet(snippet.toString())
                );
                marker.setTag(signalLog);
                markers.add(marker);
            };

            if (isMapReadyFirstTime) {
                SignalLogsManager.addOnAddNewSignalLogListener(addMarker);
                isMapReadyFirstTime = false;
            }

            Thread signalLogLoader = new Thread(() -> {
                SignalLogsManager.loadSignalLogs(10000);
            });
            signalLogLoader.start();

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            SignalLog latestSignalLog = SignalLogsManager.getLatestSignalLog();
            if (latestSignalLog != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latestSignalLog.getLatitude(), latestSignalLog.getLongitude()), 12));
            }
            try {
                signalLogLoader.join();
                for (SignalLog signalLog : SignalLogsManager.getCachedSignalLogs()) {
                    addMarker.accept(signalLog);
                }
            } catch (InterruptedException e) {
            }
        });
    }
}