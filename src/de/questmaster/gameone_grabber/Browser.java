package de.questmaster.gameone_grabber;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel
 * Date: 19.01.11
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public class Browser extends JFrame {
    private JPanel panel1;
    private JTextField saveField;
    private JButton grabButton;
    private JButton selectButton;
    private JSpinner episodeSpinner;
    private JTextField rtmpLocationField;
    private JButton locateButton;

    private static final String RTMPDUMP_LOCATION = "rtmpdump.location";
    private static final String SAVE_PATH = "save.path";
    private static final String LAST_EPISODE = "last.episode";
    private static final String WIN_LOCATION_X = "window.location.x";
    private static final String WIN_LOCATION_Y = "window.location.y";

    private Component c = this;
    private String episodeNumber = "118";
    private Properties p = new Properties();

    public Browser() {
        super("GameOne Grabber v0.1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        setPreferredSize(new Dimension(450, 184));
        pack();

        // load properties
        try {
            p.load(new FileReader(new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".GameOneGraber.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // set loaded properties
        setLocation(Integer.parseInt(p.getProperty(WIN_LOCATION_X, "100")),
                Integer.parseInt(p.getProperty(WIN_LOCATION_Y, "100")));
        saveField.setText(p.getProperty(SAVE_PATH, "./") + "GameOne-XXX");
        episodeNumber = p.getProperty(LAST_EPISODE, "118");
        episodeSpinner.setValue(Integer.valueOf(episodeNumber));

        // check for rtmpdump executable
        File f = new File(p.getProperty(RTMPDUMP_LOCATION, "rtmpdump.exe"));
        if (f.exists()) {
            rtmpLocationField.setText(f.getAbsolutePath());
            setProperty(RTMPDUMP_LOCATION, f.getAbsolutePath());
            locateButton.setEnabled(false);
            selectButton.setEnabled(true);
            grabButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "RTMPdump executable not found. \nPlease select location of executable via 'Locate' button.\n You can download it from http://rtmpdump.mplayerhq.hu/.", "RTMPdump not found", JOptionPane.OK_OPTION);
        }


        // following are all the listeners...

        this.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {}

            public void windowClosing(WindowEvent e) {
                setProperty(WIN_LOCATION_X, String.valueOf(getX()));
                setProperty(WIN_LOCATION_Y, String.valueOf(getY()));
            }

            public void windowClosed(WindowEvent e) {}

            public void windowIconified(WindowEvent e) {}

            public void windowDeiconified(WindowEvent e) {}

            public void windowActivated(WindowEvent e) {}

            public void windowDeactivated(WindowEvent e) {}
        });

        selectButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                String curPath = saveField.getText();

                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(curPath));
                int returnVal = chooser.showSaveDialog(panel1);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    saveField.setText(f.getAbsolutePath());
                    setProperty(SAVE_PATH, f.getParent() + System.getProperty("file.separator"));
                }
            }
        });

        grabButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                saveField.setText(saveField.getText().replace("XXX", episodeNumber));
                saveField.setEnabled(false);
                selectButton.setEnabled(false);
                episodeSpinner.setEnabled(false);
                grabButton.setEnabled(false);

                Grabber g = new Grabber(episodeNumber, saveField.getText(), rtmpLocationField.getText());
                g.setLocationRelativeTo(c);
                g.setVisible(true);
                setProperty(LAST_EPISODE, episodeNumber);

                new Thread(g).start();

                saveField.setText(saveField.getText().replace(episodeNumber, "XXX"));
                saveField.setEnabled(true);
                selectButton.setEnabled(true);
                episodeSpinner.setEnabled(true);
                grabButton.setEnabled(true);
            }
        });

        episodeSpinner.addChangeListener(new ChangeListener() {
            /**
             * Invoked when the target of the listener has changed its state.
             *
             * @param e a ChangeEvent object
             */
            public void stateChanged(ChangeEvent e) {
                try {
                    episodeNumber = String.valueOf(episodeSpinner.getValue());
                    while (episodeNumber.length() < 3) {
                        episodeNumber = "0" + episodeNumber;
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });
        locateButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                String curPath = "rtmpdump.exe";

                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(curPath));
                int returnVal = chooser.showSaveDialog(panel1);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    if (f.exists()) {
                        rtmpLocationField.setText(f.getAbsolutePath());
                        setProperty(RTMPDUMP_LOCATION, f.getAbsolutePath());
                        locateButton.setEnabled(false);
                        selectButton.setEnabled(true);
                        grabButton.setEnabled(true);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Browser");
        frame.setContentPane(new Browser().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        episodeSpinner = new JSpinner(new SpinnerNumberModel(118, 102, 999, 1));
    }

    private void setProperty (String name, String val) {
        p.setProperty(name, val);
        try {
            File f = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".GameOneGraber.properties");
            if (!f.exists())
                f.createNewFile();
            p.store(new FileWriter(f), "Properties of GameOne Grabber");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
