package de.questmaster.gameone_grabber;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class Grabber extends JDialog implements Runnable {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea dumpOutput;

    private String episodeNumber;
    private String dumpLocation;
    private Process pRtmpdump = null;

    public Grabber(String epNo, String loc) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("GameOne Episode " + epNo);
        setPreferredSize(new Dimension(800, 500));
        pack();
        setVisible(true);

        episodeNumber = epNo;
        dumpLocation = loc;

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
        if (pRtmpdump != null)
            pRtmpdump.destroy();
        File f = new File(dumpLocation);
        if (f.exists())
            f.delete();

        dispose();
    }

    public static void main(String[] args) {
        Grabber dialog = new Grabber("118", "./out.mp4");
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
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
        dumpOutput.append("Grabbing data from URL: " + sUrl + "\n\n");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(sUrl).openStream()));

            String line;
            while ((line = br.readLine()) != null) {

                // stream config file
                if ((in = line.indexOf("playlistfile")) > -1) {
                    in += 15;
                    out = line.indexOf(", ", in) - 1;
                    playListFile = "http://assets.gameone.de" + line.substring(in, out);

                    dumpOutput.append("Found playlistfile: " + playListFile + "\n");
                }

                // player URL
                if ((in = line.indexOf("embedSWF")) > -1) {
                    in += 10;
                    out = line.indexOf(", ", in) - 1;
                    embededSwf = line.substring(in, out);

                    dumpOutput.append("Found embededSWF: " + embededSwf + "\n");

                    // player magic word
                    in = out + 4;
                    out = line.indexOf(", ", in) - 1;
                    magicWord = line.substring(in, out);

                    dumpOutput.append("Found magicWord: " + magicWord + "\n");
                }
            }
            br.close();

            if (playListFile != null) {
                // parse config file
                br = new BufferedReader(new InputStreamReader(new URL(playListFile).openStream()));
                while ((line = br.readLine()) != null) {

                    // stream config file
                    if ((in = line.indexOf("rtmp")) > -1) {
                        out = line.indexOf("}", in) - 1;
                        streamUrl = line.substring(in, out);

                        server = true;
                        dumpOutput.append("Found server url: " + streamUrl + "\n");
                    }

                    if ((in = line.indexOf("hqv")) > -1) {
                        in += 6;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        dumpOutput.append("Found HQ stream url: " + streamUrl + "\n");
                    } else if ((in = line.indexOf("file\"")) > -1) {
                        in += 7;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        dumpOutput.append("Found SD stream url: " + streamUrl + "\n");
                    } else if ((in = line.indexOf("filename")) > -1) {
                        in += 11;
                        out = line.indexOf(",", in) - 1;
                        streamUrl += line.substring(in, out);

                        stream = true;
                        dumpOutput.append("Found LQ/16:9 stream url: " + streamUrl + "\n");
                    }
                }
                br.close();
            }
        } catch (MalformedURLException e) {
//            dumpOutput.append("Incorrect URL: " + sUrl + "\n");
            dumpOutput.append(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            dumpOutput.append(e.getLocalizedMessage());
            e.printStackTrace();
        }

        if (server && stream && embededSwf != null && magicWord != null) {
            if (magicWord.contains("gameone")) {
                dumpLocation += "_" + streamUrl.substring(streamUrl.lastIndexOf("/") + 1);
                dumpOutput.append("\nDumping Episode " + episodeNumber + " to: " + dumpLocation + "\n\n");

                // call rtmpdump
                try {
/*                    String cmdline =  "rtmpdump.exe -r \"" + streamUrl
                            + "\" -o " + dumpLocation
                            + " -W \"" + embededSwf
                            + "\" -p \"" + sUrl
                            + "\" -u \"" + magicWord
                            + "\"";
*/
                    ProcessBuilder pb = new ProcessBuilder("rtmpdump.exe",
                            "-r", "\"" + streamUrl + "\"",
                            "-o", dumpLocation,
                            "-W", "\"" + embededSwf + "\"",
                            "-p", "\"" + sUrl + "\"",
                            "-u", "\"" + magicWord + "\"");
                    pb.directory(new File("C:\\Users\\Daniel\\IdeaProjects\\GameOne-Grabber\\"));
                    pb.redirectErrorStream(true);
                    pRtmpdump = pb.start();

                    BufferedReader br = new BufferedReader(new InputStreamReader(pRtmpdump.getInputStream()));

                    String line;
                    while ((line = br.readLine()) != null) {
/*                        if (line.length() > 0 && Character.isDigit(line.codePointAt(0))) {
                                // TODO: remove last line
                                String text = dumpOutput.getText();
                                text.substring(0, text.lastIndexOf('\n', text.lastIndexOf('\n') - 1) + 1); // remove last line, keep \n
                                dumpOutput.setText(text);
                        }
*/                        dumpOutput.append(line + "\n");

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

                dumpOutput.append("\nExit value: " + pRtmpdump.exitValue() + "\nDone.\n");
                buttonOK.setEnabled(true);
            } else {
                dumpOutput.append("\nThe given episode does not exist. Stream of different episode was received.\nExiting.\n");
            }
        } else {
            dumpOutput.append("\nNot all information needed could be parsed.\nExiting.\n");
        }
    }
}
