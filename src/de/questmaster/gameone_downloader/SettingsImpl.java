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
 * To change this template use File | SettingsImpl | File Templates.
 */
public class SettingsImpl implements Settings {
    private static final String RTMPDUMP_LOCATION = "rtmpdump.location";
    private static final String SAVE_PATH = "save.path";
    private static final String LAST_EPISODE = "last.episode";
    private static final String WIN_LOCATION_X = "window.location.x";
    private static final String WIN_LOCATION_Y = "window.location.y";
    private static Properties mProperties = null;
    private static String mFilename = null;

    private SettingsImpl() {
    }

    public static void setSettingsFile(String settingsFilename) {
        if (mFilename == null) {
            mFilename = settingsFilename;
        } else {
            throw new IllegalStateException("SettingsImpl file already set.");
        }
    }

    public static Settings createSettings() {
        if (mFilename == null) {
            throw new IllegalStateException("SettingsImpl file not set.");
        }

        if (mProperties == null) {
            mProperties = new Properties();

            // load properties
            try {
                mProperties.load(new FileReader(new File(mFilename)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new SettingsImpl();
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

    @Override
    public String getRTMPdumpLocation() {
        return getSetting(RTMPDUMP_LOCATION, "rtmpdump.exe");
    }

    @Override
    public void setRTMPdumpLocation(String location) {
        setSetting(SettingsImpl.RTMPDUMP_LOCATION, location);
    }

    @Override
    public String getSaveLocation() {
        return getSetting(SettingsImpl.SAVE_PATH, "./") + "GameOne-XXX";
    }

    @Override
    public void setSaveLocation(String location) {
        setSetting(SAVE_PATH, location);
    }

    @Override
    public String getLastEpisode() {
        return getSetting(SettingsImpl.LAST_EPISODE, "118");
    }

    @Override
    public void setLastEpisode(String episode) {
        setSetting(LAST_EPISODE, episode);
    }

    @Override
    public Dimension getWindowLocation() {
        return new Dimension(Integer.parseInt(getSetting(SettingsImpl.WIN_LOCATION_X, "100")),
                Integer.parseInt(getSetting(SettingsImpl.WIN_LOCATION_Y, "100")));
    }

    @Override
    public void setWindowLocation(Dimension location) {
        setSetting(WIN_LOCATION_X, String.valueOf(location.width));
        setSetting(WIN_LOCATION_Y, String.valueOf(location.height));
    }
}
