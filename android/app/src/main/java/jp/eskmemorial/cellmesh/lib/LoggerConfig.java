package jp.eskmemorial.cellmesh.lib;

import java.net.MalformedURLException;
import java.net.URL;

public class LoggerConfig {
    public static final String SAVE_FILENAME = "logger.config";

    private static LoggerConfig mInstance;

    public int locationUpdateIntervalSec = 90;
    public int locationUpdateMinIntervalSec = 90;
    public int locationUpdateMaxIntervalSec = 90;

    public URL speedTestUrl;
    public URL resultReportUrl;

    public int collectCellsMinIntervalSec = 30;
    public int collectCellsMinDistanceMeter = 100;

    public boolean runSpeedTest = true;

    public int runSpeedTestMinIntervalSec = 60 * 30;
    public int runSpeedTestMinDistanceMeter = 1000;

    public boolean runSpeedTestIfCellChanged = true;

    public static void createInstance(LoggerConfig loggerConfig) {
        if (loggerConfig == null) {
            mInstance = new LoggerConfig();
        } else {
            mInstance = loggerConfig;
        }

        try {
            mInstance.speedTestUrl = new URL("https://www.eskmemorial.jp/works/CellMesh/benchmark/");
            mInstance.resultReportUrl = new URL("https://www.eskmemorial.jp/works/CellMesh/report");
        } catch (MalformedURLException e) {
        }
    }

    public static LoggerConfig getInstance() {
        if (mInstance == null) {
            mInstance = new LoggerConfig();
        }
        return mInstance;
    }
}
