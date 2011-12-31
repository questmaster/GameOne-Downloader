/*
 * Copyright (C) 2011 Daniel Jacobi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.questmaster.gameone_downloader;

import de.questmaster.gameone_downloader.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Downloader extends JDialog implements Runnable {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea dumpOutput;

    private String episodeNumber;
    private String dumpLocation;
    private String rtmpDumpLocation;
    private Process pRtmpdump = null;
    private BufferedReader br = null;
    private ResourceBundle resBundle = ResourceBundle.getBundle("de.questmaster.gameone_downloader.i18n");

    public Downloader(String epNo, String loc, String rtmpdump) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(MessageFormat.format(resBundle.getString("downloader.gameone.episode"), epNo));
        setPreferredSize(new Dimension(800, 500));
        pack();
        setVisible(true);

        episodeNumber = epNo;
        dumpLocation = loc;
        rtmpDumpLocation = rtmpdump;

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
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

        dispose();
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
        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.grabbing.data.from.url"), sUrl));
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

                    dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.playlistfile"), playListFile));
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

                    dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.playlistfilev2"), playListFileV2));
                }

                // get v2 rtmp server
                if ((in = line.indexOf("streamer")) > -1) {
                    in += 11;
                    out = line.indexOf("\"", in);
                    tcUrl = line.substring(in, out);

                    server = true;

                    dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.server.url"), tcUrl));
                }

                // get v3 rtmp-id
                if ((in = line.indexOf("riptide_video_id")) > -1) {
                    in += 19;
                    out = line.indexOf("\"", in);
                    playListFileV3Id = line.substring(in, out);

                    server = true;

                    dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.playlistid"), playListFileV3Id));
                }

                // player URL
                if ((in = line.indexOf("embedSWF")) > -1) {
                    in += 10;
                    out = line.indexOf(", ", in) - 1;
                    embededSwf = line.substring(in, out);

                    dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.embededswf"), embededSwf));

                    // player magic word
                    in = out + 4;
                    out = line.indexOf(", ", in) - 1;
                    magicWord = line.substring(in, out);

                    dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.magicword"), magicWord));
                }
            }
            br.close();

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
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.server.url"), streamUrl));
                    }

                    if ((in = line.indexOf("hqv")) > -1) {
                        in += 6;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.hq.stream.url"), streamUrl));
                    } else if ((in = line.indexOf("file\"")) > -1) {
                        in += 7;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.sd.stream.url"), streamUrl));
                    } else if ((in = line.indexOf("filename")) > -1) {
                        in += 11;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.lq.16.9.stream.url"), streamUrl));
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
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.hq.stream.url"), streamUrl));
                        break;
                    } else if (line.contains("hls.mtvnn.com/i/_!/riptide-mtvn") && !stream) {         // V2_2
                        in = 0;
                        out = line.indexOf(".csmil", in);
                        streamUrl = line.substring(in, out); // Not really a stream URL, but I reuse the var

                        // format URL
                        streamUrl = streamUrl.replace(",.mp4", ".mp4");
                        streamUrl = streamUrl.replace(streamUrl.substring(streamUrl.indexOf(","), streamUrl.lastIndexOf(",") + 1), "");

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.sd.stream.url"), streamUrl));
                    } /*else if ((in = line.indexOf("filename")) > -1) {
                        in += 11;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.lq.16.9.stream.url"), streamUrl));
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

                    if (line.contains("640px")) {          // V2_1
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.hq.stream.url"), streamUrl));
                        break;
                    } else if (line.contains("576k")) {
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.sd.stream.url"), streamUrl));
                    } else if (line.contains("160k")) {
                        in = line.indexOf(">") + 1;
                        out = line.lastIndexOf("<");
                        streamUrl = line.substring(in, out);

                        stream = true;
                        dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.lq.16.9.stream.url"), streamUrl));
                   }
                }
                br.close();

                // get http stream
                if (stream) {
                    httpURL = "http://cdn.riptide-mtvn.com/" + streamUrl.substring(streamUrl.indexOf("r2"));
                }

            }
        } catch (MalformedURLException e) {
            dumpOutput.append(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            dumpOutput.append(e.getLocalizedMessage());
            e.printStackTrace();
        }

        // http is more reliable so try this before rtmp
        if (httpURL != null) {
            try {
                //Desktop.getDesktop().browse(new URI(httpURL));

                // TODO: Progress dialog
                dumpLocation += "_" + httpURL.substring(httpURL.lastIndexOf("/") + 1);
                dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.dumping.episode.http"), episodeNumber, httpURL));

                InputStream instream = new BufferedInputStream(new ProgressMonitorInputStream(contentPane,"Reading " + dumpLocation.substring(dumpLocation.lastIndexOf(System.getProperty("file.separator"))), new URL(httpURL).openStream()));
                OutputStream outstream = new FileOutputStream(new File(dumpLocation));

                JHelper.copyStream(512*1024, instream, outstream, true);

                // success
                skipRTMP = true;
                dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.exit.value"), 0));
                buttonOK.setEnabled(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // rtmp fallback
        if (!skipRTMP && server && stream && embededSwf != null && magicWord != null) {
            if (magicWord.contains("gameone")) {
                dumpLocation += "_" + streamUrl.substring(streamUrl.lastIndexOf("/") + 1);
                dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.dumping.episode"), episodeNumber, dumpLocation));

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
                    while ((line = br.readLine()) != null) { // TODO: fix scrolling
                        // scroll to the end if on last position
                        boolean scroll = false;
                        if (dumpOutput.getCaretPosition() >= dumpOutput.getDocument().getLength()-2)
                            scroll = true;

                        // check to override download progress
                        if (line.length() > 0 && Character.isDigit(line.codePointAt(0))) {
                            if (first) {
                                textLen = dumpOutput.getText().length();
                                first = false;
                            } else {
                                // remove last line
                                dumpOutput.replaceRange(line, textLen, dumpOutput.getText().length());
                            }
                        } else
                            dumpOutput.append(line + "\n");

                        // show last line
                        if (scroll)
                        dumpOutput.setCaretPosition(dumpOutput.getDocument().getLength());

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

                dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.exit.value"), pRtmpdump.exitValue()));
                buttonOK.setEnabled(true);
            } else {
                dumpOutput.append(resBundle.getString("downloader.episode.does.not.exist"));
            }
        } else {
            if (skipRTMP) {
                dumpOutput.append(resBundle.getString("downloader.rtmp.skipped"));
            } else {
                dumpOutput.append(resBundle.getString("downloader.not.all.information"));
            }
        }
    }

}
