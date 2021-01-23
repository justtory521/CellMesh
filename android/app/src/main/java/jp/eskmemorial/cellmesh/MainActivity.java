package jp.eskmemorial.cellmesh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;

import jp.eskmemorial.cellmesh.lib.LocationUpdatesService;
import jp.eskmemorial.cellmesh.lib.LocationUpdatesServiceConnection;
import jp.eskmemorial.cellmesh.lib.LoggerConfig;
import jp.eskmemorial.cellmesh.lib.SignalLogsManager;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static LocationUpdatesServiceConnection mLocationUpdatesServiceConnection = new LocationUpdatesServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread requestPermissions = new Thread(() -> {
            startActivity(new Intent(MainActivity.this, RequestPermissionsActivity.class));
        });
        requestPermissions.start();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_past_logs, R.id.nav_config)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        initializeApplication();
        try {
            requestPermissions.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initializeApplication() {
        Thread dbLoader = new Thread(() -> {
            SignalLogsManager.createInstance(getApplicationContext());
        });
        dbLoader.start();
        initializeTestConfig();
        bindService(new Intent(this, LocationUpdatesService.class), mLocationUpdatesServiceConnection, Context.BIND_AUTO_CREATE);
        initializeLocationUpdatesServiceSwitch();
        try {
            dbLoader.join();
        } catch (InterruptedException e) {
        }
    }

    private void initializeLocationUpdatesServiceSwitch() {
        ImageButton button = findViewById(R.id.switch_location_updates_service);
        button.setOnClickListener(v -> {
            if (MainActivity.mLocationUpdatesServiceConnection.isRunning()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.stopLocationUpdatesServiceDiglogTitle)
                        .setMessage(R.string.stopLocationUpdatesServiceDiglogMessage)
                        .setPositiveButton(R.string.stopLocationUpdatesServiceDiglogPositiveButton, (dialog, which) -> {
                            mLocationUpdatesServiceConnection.stop();
                            Toast.makeText(this, R.string.stopLocationUpdatesServiceToast, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.stopLocationUpdatesServiceDiglogNegativeButton, null)
                        .show();
            } else {
                mLocationUpdatesServiceConnection.start();
                Toast.makeText(this, R.string.startLocationUpdatesServiceToast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO mv to LoggerConfig
    private void initializeTestConfig() {
        StringBuilder json = new StringBuilder();
        try {
            File file = new File(getFilesDir(), LoggerConfig.SAVE_FILENAME);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileInputStream fis = openFileInput(LoggerConfig.SAVE_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
                json.append(System.getProperty("line.separator"));
            }
            reader.close();
            fis.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        } catch (UnsupportedCharsetException e) {

        }
        LoggerConfig.createInstance(new Gson().fromJson(json.toString(), LoggerConfig.class));
    }
}