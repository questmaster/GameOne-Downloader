package de.questmaster.gameone_downloader;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 19.06.13
 * Time: 23:56
 * To change this template use File | SettingsImpl | File Templates.
 */
public interface Settings {
    public String getRTMPdumpLocation();
    public void   setRTMPdumpLocation(String location);

    public String getSaveLocation();
    public void   setSaveLocation(String location);

    public String  getLastEpisode();
    public void setLastEpisode(String episode);

    public Dimension getWindowLocation();
    public void setWindowLocation(Dimension location);
}
