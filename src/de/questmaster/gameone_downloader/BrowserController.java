package de.questmaster.gameone_downloader;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 20.06.13
 * Time: 10:47
 * To change this template use File | Settings | File Templates.
 */
public class BrowserController implements BrowserControllerInterface {
    private DownloaderModelInterface model;
    private Browser view;

    public BrowserController(DownloaderModelInterface model) {
        this.model = model;

        view = new Browser(this, this.model);
    }

    @Override
    public void locateRTMPdump() {
        String curPath = "rtmpdump.exe";

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(curPath));
        int returnVal = chooser.showSaveDialog(view);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f.exists()) {
                model.setRTMPdumpLocation(f.getAbsolutePath());
                view.disableLocateButton();
                view.enableSelectButton();
                view.enableGrabButton();
            }
        }
    }

    @Override
    public void locateSaves() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(model.getSaveLocation()));
        int returnVal = chooser.showSaveDialog(view);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            model.setSaveLocation(f.getParent() + System.getProperty("file.separator"));
        }
    }

    @Override
    public void download() {
        view.disableSaveLocationField();
        view.disableSelectButton();
        view.disableEpisodeSpinner();
        view.disableGrabButton();

        model.initiateDownload();

        view.enableSaveLocationField();
        view.enableSelectButton();
        view.enableEpisodeSpinner();
        view.enableGrabButton();
    }

    @Override
    public void setRTMPdumpLocation(String location) {
        model.setRTMPdumpLocation(location);
    }

    @Override
    public void setSaveLocation(String location) {
        model.setSaveLocation(location);
    }

    @Override
    public void setEpisode(String episode) {
        while (episode.length() < 3) {
            episode = "0" + episode;
        }
        model.setEpisode(episode);
    }

    @Override
    public void setWindowPosition(Point location) {
        model.setWindowPosition(location);
    }
}
