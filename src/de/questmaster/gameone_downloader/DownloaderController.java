package de.questmaster.gameone_downloader;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 20.06.13
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class DownloaderController implements DownloaderControllerInterface {
    private Downloader view;
    private DownloadParserModelInterface model;

    public DownloaderController(DownloadParserModelInterface model) {
        this.model = model;

        view = new Downloader(this, this.model);
    }

    @Override
    public void onOk() {
        view.dispose();
        model.setDownloadInactive();
    }

    @Override
    public void onCancel() {
        model.cancelDownload();

        view.dispose();
        model.setDownloadInactive();
    }
}
