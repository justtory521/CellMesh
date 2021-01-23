package jp.eskmemorial.cellmesh.ui.config;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.eskmemorial.cellmesh.R;
import jp.eskmemorial.cellmesh.databinding.FragmentConfigBinding;
import jp.eskmemorial.cellmesh.lib.LoggerConfig;

public class ConfigFragment extends Fragment {

    private ConfigViewModel configViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        configViewModel =
                ViewModelProviders.of(this).get(ConfigViewModel.class);
        View root = inflater.inflate(R.layout.fragment_config, container, false);

        FragmentConfigBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_config, container, false);
        binding.setConfig(LoggerConfig.getInstance());

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            File file = new File(getActivity().getFilesDir(), LoggerConfig.SAVE_FILENAME);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = getActivity().openFileOutput(LoggerConfig.SAVE_FILENAME, Context.MODE_PRIVATE);
            fos.write(new Gson().toJson(LoggerConfig.getInstance()).getBytes("UTF-8"));
            fos.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }
    }
}