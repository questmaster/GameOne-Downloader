package de.questmaster.gameone_downloader;

import de.questmaster.gameone_downloader.utils.JHelper;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 20.06.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */
public class DownloadParserModel implements Runnable, DownloadParserModelInterface {

    private DownloaderControllerInterface controller = null;
    private ArrayList<DownloaderObserver> mObserverList = new ArrayList<DownloaderObserver>();

    private String episodeNumber;
    private String dumpLocation;
    private String rtmpDumpLocation;
    private Process pRtmpdump = null;
    private BufferedReader br = null;
    private StringBuffer mProcessData = new StringBuffer();
    private boolean mDownloadFinished = false;
    private boolean mDownloadActive = true;

    private ResourceBundle resBundle = ResourceBundle.getBundle("de.questmaster.gameone_downloader.i18n");

    public DownloadParserModel(String epNo, String loc, String rtmpdump) {
        episodeNumber = epNo;
        dumpLocation = loc;
        rtmpDumpLocation = rtmpdump;

        controller = new DownloaderController(this);

    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        int in, out;
        String playListFile = null, playListFileV2 = null, playListFileV3Id = null, embededSwf = null, magicWord = null, streamUrl = null, tcUrl = null, httpURL = null;
        boolean server = false, stream = false, skipRTMP = false;

        String sUrl = "http://www.gameone.de/tv/" + episodeNumber;
        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.grabbing.data.from.url"), sUrl));
        try {
            Desktop.getDesktop().browse(new URI(sUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // change user agent
            URLConnection connection = new URL(sUrl).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0");
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {

                // stream v1 config file
                if ((in = line.indexOf("playlistfile")) > -1) {
                    in += 15;
                    out = line.indexOf("\"", in);
                    playListFile = "http://assets.gameone.de" + line.substring(in, out);

                    appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.playlistfile"), playListFile));
                }

                // stream v2 config file
                if (line.contains("controls=\"controls\"") && (in = line.indexOf("src=\"")) > -1) {
                    in += 5;
                    out = line.indexOf("\"", in);
                    playListFileV2 = line.substring(in, out);

                    // check for content
                    if (playListFileV2.endsWith("mp4")) {
                        httpURL = playListFileV2;
                    } //else
                    //// cut file extension (m3u8)
                    //playListFileV2 = playListFileV2.substring(0, playListFileV2.lastIndexOf("."));

                    appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.playlistfilev2"), playListFileV2));
                }

                // get v2 rtmp server
                if ((in = line.indexOf("streamer")) > -1) {
                    in += 11;
                    out = line.indexOf("\"", in);
                    tcUrl = line.substring(in, out);

                    server = true;

                    appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.server.url"), tcUrl));
                }

                // get v3 rtmp-id
                if ((in = line.indexOf("riptide_video_id")) > -1) {
                    in += 19;
                    out = line.indexOf("\"", in);
                    playListFileV3Id = line.substring(in, out);

                    server = true;

                    appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.playlistid"), playListFileV3Id));
                }

                // player URL
                if ((in = line.indexOf("embedSWF")) > -1) {
                    in += 10;
                    out = line.indexOf(", ", in) - 1;
                    embededSwf = line.substring(in, out);

                    appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.embededswf"), embededSwf));

                    // player magic word
                    in = out + 4;
                    out = line.indexOf(", ", in) - 1;
                    magicWord = line.substring(in, out);

                    appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.magicword"), magicWord));
                }
            }
            br.close();

            // Nothing found? try media rss
            if (playListFile == null && playListFileV2 == null && playListFileV3Id == null) {
                String mrssUrl = "http://www.gameone.de/api/mrss/mgid:gameone:video:mtvnn.com:tv_show-" + episodeNumber;
                br = new BufferedReader(new InputStreamReader(new URL(mrssUrl).openStream()));
                while ((line = br.readLine()) != null) {

                    // get v3_1 rtmp-id
                    if ((in = line.indexOf("mediagen")) > -1) {
                        in += 9;
                        out = line.indexOf("?", in);
                        playListFileV3Id = line.substring(in, out);

                        server = true;

                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.playlistid"), playListFileV3Id));
                    }

                }
            }

            processDownloaderObserver();

            // parse v1 playlistfile, if no http source
            if (playListFile != null && httpURL == null) {
                // parse config file
                br = new BufferedReader(new InputStreamReader(new URL(playListFile).openStream()));
                while ((line = br.readLine()) != null) {

                    if ((in = line.indexOf("rtmp")) > -1) {
                        out = line.indexOf(",", in) - 1;
                        streamUrl = line.substring(in, out);
                        tcUrl = line.substring(in, out);

                        server = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.server.url"), streamUrl));
                    }

                    if ((in = line.indexOf("hqv")) > -1) {
                        in += 6;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.hq.stream.url"), streamUrl));
                    } else if ((in = line.indexOf("file\"")) > -1) {
                        in += 7;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.sd.stream.url"), streamUrl));
                    } else if ((in = line.indexOf("filename")) > -1) {
                        in += 11;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.lq.16.9.stream.url"), streamUrl));
                    }
                }
                br.close();
            }

            // parse v2 playlist file, if no http source
            if (!(server && stream) && playListFileV2 != null && httpURL == null) {

                // parse config file
                br = new BufferedReader(new InputStreamReader(new URL(playListFileV2).openStream()));
                while ((line = br.readLine()) != null) {

                    if (line.contains("1264k")) {          // V2_1
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.hq.stream.url"), streamUrl));
                        break;
                    } else if (line.contains("hls.mtvnn.com/i/_!/riptide-mtvn") && !stream) {         // V2_2
                        in = 0;
                        out = line.indexOf(".csmil", in);
                        streamUrl = line.substring(in, out); // Not really a stream URL, but I reuse the var

                        // format URL
                        streamUrl = streamUrl.replace(",.mp4", ".mp4");
                        streamUrl = streamUrl.replace(streamUrl.substring(streamUrl.indexOf(","), streamUrl.lastIndexOf(",") + 1), "");

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.sd.stream.url"), streamUrl));
                    } /*else if ((in = line.indexOf("filename")) > -1) {
                        in += 11;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.lq.16.9.stream.url"), streamUrl));
                    }   */
                }
                br.close();
                // get http stream
                if (stream) {
                    httpURL = "http://cdn.riptide-mtvn.com/" + streamUrl.substring(streamUrl.indexOf("r2"));
                }

            }

            // parse v3 playlist file, if no http source
            if (!(server && stream) && playListFileV3Id != null && httpURL == null) {

                // parse config file
                br = new BufferedReader(new InputStreamReader(new URL("http://videos.mtvnn.com/mediagen/" + playListFileV3Id).openStream()));
                while ((line = br.readLine()) != null) {

                    if (line.contains("640px")) {          // V3
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.hq.stream.url"), streamUrl));
                        break;
                    } else if (line.contains("webxl")) {          // V3_1
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.hq.stream.url"), streamUrl));
                        break;
                    } else if (line.contains("576k")) {          // V3
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.sd.stream.url"), streamUrl));
                    } else if (line.contains("webl")) {          // V3_1
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.sd.stream.url"), streamUrl));
                    } else if (line.contains("160k")) {          // V3
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.lq.16.9.stream.url"), streamUrl));
                    } else if (line.contains("webm")) {          // V3_1
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        appendParserOutput(MessageFormat.format(resBundle.getString("downloader.found.lq.16.9.stream.url"), streamUrl));
                    }
                }
                br.close();

                // get http stream
                if (stream) {
                    httpURL = "http://cdn.riptide-mtvn.com/" + streamUrl.substring(streamUrl.indexOf("r2"));
                }

            }
        } catch (MalformedURLException e) {
            appendParserOutput(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            appendParserOutput(e.getLocalizedMessage());
            e.printStackTrace();
        }

        // http is more reliable so try this before rtmp
        if (httpURL != null) {
            try {
                //Desktop.getDesktop().browse(new URI(httpURL));

                // TODO: Progress dialog
                dumpLocation += "_" + httpURL.substring(httpURL.lastIndexOf("/") + 1);
                appendParserOutput(MessageFormat.format(resBundle.getString("downloader.dumping.episode.http"), episodeNumber, httpURL));

                InputStream instream = new BufferedInputStream(new URL(httpURL).openStream());
                OutputStream outstream = new FileOutputStream(new File(dumpLocation));

                JHelper.copyStream(512 * 1024, instream, outstream, true);

                // success
                skipRTMP = true;
                appendParserOutput(MessageFormat.format(resBundle.getString("downloader.exit.value"), 0));
                mDownloadFinished = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        processDownloaderObserver();

        // rtmp fallback
        if (!skipRTMP && server && stream && embededSwf != null && magicWord != null) {
            if (magicWord.contains("gameone") || magicWord.contains("container")) {
                dumpLocation += "_" + streamUrl.substring(streamUrl.lastIndexOf("/") + 1);
                appendParserOutput(MessageFormat.format(resBundle.getString("downloader.dumping.episode"), episodeNumber, dumpLocation));

                // call rtmpdump
                try {
                    System.err.println("rtmpdump.exe -r \"" + streamUrl + "\" -o " + dumpLocation + " -W \"" + embededSwf + "\" -p \""
                            + sUrl + "\" -u \"" + magicWord + "\" -t \"" + tcUrl + "\"");
                    ProcessBuilder pb = new ProcessBuilder(rtmpDumpLocation,
                            "-r", "\"" + streamUrl + "\"",
                            "-o", dumpLocation,
                            "-W", "\"" + embededSwf + "\"",
                            "-p", "\"" + sUrl + "\"",
                            "-u", "\"" + magicWord + "\"",
                            "-t", "\"" + tcUrl + "\"");
                    pb.redirectErrorStream(true);
                    pRtmpdump = pb.start();

                    BufferedReader br = new BufferedReader(new InputStreamReader(pRtmpdump.getInputStream()));

                    boolean first = true;
                    int textLen = 0;
                    String line;
                    while ((line = br.readLine()) != null) {

                        // check to override download progress
                        if (line.length() > 0 && Character.isDigit(line.codePointAt(0))) {
                            if (first) {
                                textLen = mProcessData.length();
                                first = false;
                            } else {
                                // remove last line
                                mProcessData.replace(textLen, mProcessData.length(), line);
                            }
                        } else
                            appendParserOutput(line + "\n");

                        try {
                            int exit = pRtmpdump.exitValue();
                            if (exit == 0) {
                                // Process finished
                                break;
                            }
                        } catch (IllegalThreadStateException t) {
                            // Nothing to do
                        }
                    }
                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                appendParserOutput(MessageFormat.format(resBundle.getString("downloader.exit.value"), pRtmpdump.exitValue()));
                mDownloadFinished = true;
            } else {
                appendParserOutput(resBundle.getString("downloader.episode.does.not.exist"));
            }
        } else {
            if (skipRTMP) {
                appendParserOutput(resBundle.getString("downloader.rtmp.skipped"));
            } else {
                appendParserOutput(resBundle.getString("downloader.not.all.information"));
            }
        }
    }

    private void appendParserOutput(String line) {
        mProcessData.append(line);
        processDownloaderObserver();
    }

    @Override
    public String retrieveParserOutput() {
        String data = mProcessData.toString();
        mProcessData.delete(0, mProcessData.length());
        return data;
    }

    @Override
    public String getEpisode() {
        return episodeNumber;
    }

    @Override
    public boolean isDownloadFinished() {
        return mDownloadFinished;
    }

    @Override
    public void cancelDownload() {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (pRtmpdump != null) {
            pRtmpdump.destroy();
            try {
                pRtmpdump.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File f = new File(dumpLocation);
        if (f.exists()) {
            f.delete();
        }
    }

    @Override
    public void setDownloadInactive() {
        mDownloadActive = false;
    }

    @Override
    public boolean isDownloadActive() {
        return mDownloadActive;
    }

    @Override
    public void registerObserver(DownloaderObserver o) {
        mObserverList.add(o);
    }

    @Override
    public void removeObserver(DownloaderObserver o) {
        mObserverList.remove(o);
    }

    private void processDownloaderObserver() {
        for (DownloaderObserver o : mObserverList) {
            o.notifyDownloaderObserver();
        }
    }
}
