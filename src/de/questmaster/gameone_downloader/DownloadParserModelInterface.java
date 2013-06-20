package de.questmaster.gameone_downloader;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 20.06.13
 * Time: 14:42
 * To change this template use File | Settings | File Templates.
 */
public interface DownloadParserModelInterface {

    public String retrieveParserOutput();

    public String getEpisode();

    public boolean isDownloadFinished();

    public void cancelDownload();

    public void setDownloadInactive();

    public boolean isDownloadActive();

    public void registerObserver(DownloaderObserver o);

    public void removeObserver(DownloaderObserver o);

}
