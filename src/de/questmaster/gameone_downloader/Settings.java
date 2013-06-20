package de.questmaster.gameone_downloader;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 19.06.13
 * Time: 22:53
 * To change this template use File | Settings | File Templates.
 */
public class Settings {
    private static final String RTMPDUMP_LOCATION = "rtmpdump.location";
    private static final String SAVE_PATH = "save.path";
    private static final String LAST_EPISODE = "last.episode";
    private static final String WIN_LOCATION_X = "window.location.x";
    private static final String WIN_LOCATION_Y = "window.location.y";

    private Properties mProperties = null;
    private static String mFilename = null;
    private static Settings mInstance = null;

    private Settings() {
        if (mFilename == null) {
            throw new IllegalStateException("Settings file not set.");
        }

        mProperties = new Properties();

        // load properties
        try {
            mProperties.load(new FileReader(new File(mFilename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSettingsFile(String settingsFilename) {
        if (mFilename == null) {
            mFilename = settingsFilename;
        } else {
            throw new IllegalStateException("Settings file already set.");
        }
    }

    public static Settings createSettings() {
        if (mInstance == null) {
            mInstance = new Settings();
        }
        return mInstance;
    }

    private String getSetting(String name, String defaultVal) {
        return mProperties.getProperty(name, defaultVal);
    }

    private void setSetting(String name, String val) {
        mProperties.setProperty(name, val);
        try {
            File f = new File(mFilename);
            if (!f.exists())
                f.createNewFile();
            mProperties.store(new FileWriter(f), "Properties of GameOne Downloader");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRTMPdumpLocation() {
        return getSetting(RTMPDUMP_LOCATION, "rtmpdump.exe");
    }

    public void setRTMPdumpLocation(String location) {
        setSetting(Settings.RTMPDUMP_LOCATION, location);
    }

    public String getSaveLocation() {
        return getSetting(Settings.SAVE_PATH, "./");
    }

    public void setSaveLocation(String location) {
        setSetting(SAVE_PATH, location);
    }

    public String getLastEpisode() {
        return getSetting(Settings.LAST_EPISODE, "118");
    }

    public void setLastEpisode(String episode) {
        setSetting(LAST_EPISODE, episode);
    }

    public Point getWindowLocation() {
        return new Point(Integer.parseInt(getSetting(Settings.WIN_LOCATION_X, "100")),
                Integer.parseInt(getSetting(Settings.WIN_LOCATION_Y, "100")));
    }

    public void setWindowLocation(Point position) {
        setSetting(WIN_LOCATION_X, String.valueOf(position.x));
        setSetting(WIN_LOCATION_Y, String.valueOf(position.y));
    }
}
