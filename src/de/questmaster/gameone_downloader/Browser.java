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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * Date: 19.01.11
 * Time: 20:13
 * To change this template use File | SettingsImpl | File Templates.
 */
public class Browser extends JFrame {
    private JPanel panel1;
    private JTextField saveField;
    private JButton grabButton;
    private JButton selectButton;
    private JSpinner episodeSpinner;
    private JTextField rtmpLocationField;
    private JButton locateButton;

    private Component c = this;
    private String episodeNumber = "118";
    private Settings p = null;
    private ResourceBundle resBundle = ResourceBundle.getBundle("de.questmaster.gameone_downloader.i18n");

    public Browser() {
        super("GameOne Downloader v0.1.7");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        setPreferredSize(new Dimension(450, 154));
        pack();

        SettingsImpl.setSettingsFile(System.getProperty("user.home") + System.getProperty("file.separator") + ".GameOneDownloader.properties");
        p = SettingsImpl.createSettings();

        // set loaded properties
        setLocation(p.getWindowLocation().width, p.getWindowLocation().height);
        saveField.setText(p.getSaveLocation());
        episodeNumber = p.getLastEpisode();
        episodeSpinner.setValue(Integer.valueOf(episodeNumber));

        // check for rtmpdump executable
        File f = new File(p.getRTMPdumpLocation());
        if (f.exists()) {
            rtmpLocationField.setText(f.getAbsolutePath());
            p.setRTMPdumpLocation(f.getAbsolutePath());
            locateButton.setEnabled(false);
            selectButton.setEnabled(true);
            grabButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, resBundle.getString("browser.rtmpdump.executable.not.found"), resBundle.getString("browser.rtmpdump.not.found"), JOptionPane.OK_OPTION);
        }


        // following are all the listeners...

        this.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
                p.setWindowLocation(new Dimension(getX(), getY()));
            }

            public void windowClosed(WindowEvent e) {
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }
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
                    p.setSaveLocation(f.getParent() + System.getProperty("file.separator"));
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

                Downloader g = new Downloader(episodeNumber, saveField.getText(), rtmpLocationField.getText());
                g.setLocationRelativeTo(c);
                g.setVisible(true);
                p.setLastEpisode(episodeNumber);

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
                        p.setRTMPdumpLocation(f.getAbsolutePath());
                        locateButton.setEnabled(false);
                        selectButton.setEnabled(true);
                        grabButton.setEnabled(true);
                    }
                }
            }
        });
    }

    private void createUIComponents() {
        episodeSpinner = new JSpinner(new SpinnerNumberModel(118, 102, 999, 1));
    }


}
