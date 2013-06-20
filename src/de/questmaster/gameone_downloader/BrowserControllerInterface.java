package de.questmaster.gameone_downloader;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 20.06.13
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
public interface BrowserControllerInterface {

    public void locateRTMPdump();

    public void locateSaves();

    public void download();

    public void setRTMPdumpLocation(String location);

    public void setSaveLocation(String location);

    public void setEpisode(String episode);

    public void setWindowPosition(Point location);

}
