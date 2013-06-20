package de.questmaster.gameone_downloader;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 20.06.13
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public interface DownloaderModelInterface {

    public void setSaveLocation(String location);

    public String getSaveLocation();

    public String getSaveLocationFile();

    public void setEpisode(String episode);

    public String getEpisode();

    public void setRTMPdumpLocation(String location);

    public String getRTMPdumpLocation();

    public void setWindowPosition(Point position);

    public Point getWindowPosition();

    public void initiateDownload();

    public void registerObserver(BrowserObserver o);

    public void removeObserver(BrowserObserver o);

    public void waitForThreadsToFinish();
}
