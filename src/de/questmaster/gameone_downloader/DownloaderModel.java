package de.questmaster.gameone_downloader;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 20.06.13
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public class DownloaderModel implements DownloaderModelInterface {

    private Settings mSettings = null;
    private ArrayList<BrowserObserver> mBrowserObserver = new ArrayList<BrowserObserver>();
    private ArrayList<DownloadParserModelInterface> mDownloader = new ArrayList<DownloadParserModelInterface>();

    public DownloaderModel(String settingsFile) {
        Settings.setSettingsFile(settingsFile);
        mSettings = Settings.createSettings();

        // check for rtmpdump executable
        File f = new File(mSettings.getRTMPdumpLocation());
        if (f.exists()) {
            mSettings.setRTMPdumpLocation(f.getAbsolutePath());
        } else {
// TODO           JOptionPane.showMessageDialog(this, resBundle.getString("browser.rtmpdump.executable.not.found"), resBundle.getString("browser.rtmpdump.not.found"), JOptionPane.OK_OPTION);
        }
    }

    @Override
    public void setSaveLocation(String location) {
        mSettings.setSaveLocation(location);
        processBrowserObserver();
    }

    @Override
    public String getSaveLocation() {
        return mSettings.getSaveLocation();
    }

    public String getSaveLocationFile() {
        return getSaveLocation() + "GameOne-XXX";
    }

    @Override
    public void setEpisode(String episode) {
        mSettings.setLastEpisode(episode);
        processBrowserObserver();
    }

    @Override
    public String getEpisode() {
        return mSettings.getLastEpisode();
    }

    @Override
    public void setRTMPdumpLocation(String location) {
        mSettings.setRTMPdumpLocation(location);
        processBrowserObserver();
    }

    @Override
    public String getRTMPdumpLocation() {
        return mSettings.getRTMPdumpLocation();
    }

    @Override
    public void setWindowPosition(Point position) {
        mSettings.setWindowLocation(position);
        processBrowserObserver();
    }

    @Override
    public Point getWindowPosition() {
        return mSettings.getWindowLocation();
    }

    @Override
    public void registerObserver(BrowserObserver o) {
        mBrowserObserver.add(o);
    }

    @Override
    public void removeObserver(BrowserObserver o) {
        mBrowserObserver.remove(o);
    }

    private void processBrowserObserver() {
        for (BrowserObserver o : mBrowserObserver) {
            o.notifyBrowserObserver();
        }
    }

    @Override
    public void initiateDownload() {
        String downloadLocation = getSaveLocationFile().replace("XXX", getEpisode());

        DownloadParserModel parser = new DownloadParserModel(getEpisode(), downloadLocation, getRTMPdumpLocation());

        mDownloader.add(parser); // TODO Where to remove this reference?

        new Thread(parser).start();
    }

    @Override
    public void waitForThreadsToFinish() {
        boolean done = false;
        while (!done) {
            done = true;
            for (DownloadParserModelInterface i : mDownloader) {
                if (i.isDownloadActive()) {
                    done = false;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
