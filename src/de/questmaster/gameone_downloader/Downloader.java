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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
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
        String playListFile = null, embededSwf = null, magicWord = null, streamUrl = null;
        boolean server = false, stream = false;

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
            connection.addRequestProperty("User-Agent", "Opera/9.80 (Windows NT 6.1; U; de) Presto/2.7.62 Version/11.01");
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {

                // stream config file
                if ((in = line.indexOf("playlistfile")) > -1) {
                    in += 15;
                    out = line.indexOf("\"", in);
                    playListFile = "http://assets.gameone.de" + line.substring(in, out);

                    dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.found.playlistfile"), playListFile));
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

            if (playListFile != null) {
                // parse config file
                br = new BufferedReader(new InputStreamReader(new URL(playListFile).openStream()));
                while ((line = br.readLine()) != null) {

                    if ((in = line.indexOf("rtmp")) > -1) {
                        out = line.indexOf(",", in) - 1;
                        streamUrl = line.substring(in, out);

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
        } catch (MalformedURLException e) {
            dumpOutput.append(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            dumpOutput.append(e.getLocalizedMessage());
            e.printStackTrace();
        }

        if (server && stream && embededSwf != null && magicWord != null) {
            if (magicWord.contains("gameone")) {
                dumpLocation += "_" + streamUrl.substring(streamUrl.lastIndexOf("/") + 1);
                dumpOutput.append(MessageFormat.format(resBundle.getString("downloader.dumping.episode"), episodeNumber, dumpLocation));

                // call rtmpdump
                try {
                    System.err.println("rtmpdump.exe -r \"" + streamUrl + "\" -o " + dumpLocation + " -W \"" + embededSwf + "\" -p \""
                            + sUrl + "\" -u \"" + magicWord + "\"");
                    ProcessBuilder pb = new ProcessBuilder(rtmpDumpLocation,
                            "-r", "\"" + streamUrl + "\"",
                            "-o", dumpLocation,
                            "-W", "\"" + embededSwf + "\"",
                            "-p", "\"" + sUrl + "\"",
                            "-u", "\"" + magicWord + "\"");
                    pb.redirectErrorStream(true);
                    pRtmpdump = pb.start();

                    BufferedReader br = new BufferedReader(new InputStreamReader(pRtmpdump.getInputStream()));

                    boolean first = true;
                    int textLen = 0;
                    String line;
                    while ((line = br.readLine()) != null) {
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
            dumpOutput.append(resBundle.getString("downloader.not.all.information"));
        }
    }

}
