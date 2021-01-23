package jp.eskmemorial.cellmesh.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.function.Consumer;

import jp.eskmemorial.cellmesh.R;
import jp.eskmemorial.cellmesh.databinding.FragmentHomeBinding;
import jp.eskmemorial.cellmesh.databinding.FragmentHomeCellRowBinding;
import jp.eskmemorial.cellmesh.databinding.FragmentHomeSimBinding;
import jp.eskmemorial.cellmesh.lib.SignalLogsManager;
import jp.eskmemorial.cellmesh.lib.data.Cell;
import jp.eskmemorial.cellmesh.lib.data.SignalLog;
import jp.eskmemorial.cellmesh.lib.data.Sim;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        FragmentHomeBinding homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        Consumer<SignalLog> setSignalLogOnTable = (signalLog) -> {
            homeBinding.setSignalLog(signalLog);
            LinearLayout layout = homeBinding.getRoot().findViewById(R.id.sims_cells_container);
            layout.removeAllViews();
            for (Sim sim : signalLog.getSims()) {
                FragmentHomeSimBinding simBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_sim, layout, true);
                simBinding.setSim(sim);
                View cellView = inflater.inflate(R.layout.fragment_home_cell, layout, true);
                TableLayout cellTable = cellView.findViewById(R.id.cell_table);
                cellTable.setId(sim.getCid());
                for (Cell cell : sim.getCells()) {
                    FragmentHomeCellRowBinding cellRowBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_cell_row, cellView.findViewById(sim.getCid()), true);
                    cellRowBinding.setCell(cell);
                }
            }
        };

        SignalLog latestSignalLog = SignalLogsManager.getLatestSignalLog();
        if (latestSignalLog != null) {
            setSignalLogOnTable.accept(latestSignalLog);
        }
        SignalLogsManager.addOnAddNewSignalLogListener(setSignalLogOnTable);

        return homeBinding.getRoot();
    }
}