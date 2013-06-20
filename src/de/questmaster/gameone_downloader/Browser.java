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
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * Date: 19.01.11
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public class Browser extends JFrame implements BrowserObserver {
    private JPanel panel1;
    private JTextField saveField;
    private JButton grabButton;
    private JButton selectButton;
    private JSpinner episodeSpinner;
    private JTextField rtmpLocationField;
    private JButton locateButton;

    private BrowserControllerInterface controller;
    private DownloaderModelInterface model;

    private ResourceBundle resBundle = ResourceBundle.getBundle("de.questmaster.gameone_downloader.i18n");

    public Browser(BrowserControllerInterface controller, DownloaderModelInterface model) {
        setTitle(resBundle.getString("browser.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        setPreferredSize(new Dimension(450, 154));
        setLocation(model.getWindowPosition().x, model.getWindowPosition().y);
        pack();

        // set model + controller
        this.controller = controller;
        this.model = model;

        // register as model observer, update data
        this.model.registerObserver(this);
        notifyBrowserObserver();

        // following are all the listeners...

        this.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
                browser_WindowListener();
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
                selectButton_ActionListener();
            }
        });
        grabButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                grabButton_ActionListener();
            }
        });
        episodeSpinner.addChangeListener(new ChangeListener() {
            /**
             * Invoked when the target of the listener has changed its state.
             *
             * @param e a ChangeEvent object
             */
            public void stateChanged(ChangeEvent e) {
                episodeSpinner_ChangeListener();
            }
        });
        locateButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                locateButton_ActionListener();
            }
        });

        setVisible(true);
    }

    private void locateButton_ActionListener() {
        controller.locateRTMPdump();
    }

    private void grabButton_ActionListener() {
        controller.download();
    }

    private void selectButton_ActionListener() {
        controller.locateSaves();
    }

    private void episodeSpinner_ChangeListener() {
        String episodeNumber = "";
        try {
            episodeNumber = String.valueOf(episodeSpinner.getValue());
            controller.setEpisode(episodeNumber);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    private void browser_WindowListener() {
        controller.setWindowPosition(new Point(getX(), getY()));
    }

    private void createUIComponents() {
        episodeSpinner = new JSpinner(new SpinnerNumberModel(118, 102, 999, 1));
    }

    public void enableSelectButton() {
        selectButton.setEnabled(true);
    }

    public void disableSelectButton() {
        selectButton.setEnabled(false);
    }

    public void enableGrabButton() {
        grabButton.setEnabled(true);
    }

    public void disableGrabButton() {
        grabButton.setEnabled(false);
    }

    public void enableLocateButton() {
        locateButton.setEnabled(true);
    }

    public void disableLocateButton() {
        locateButton.setEnabled(false);
    }

    public void enableSaveLocationField() {
        saveField.setEnabled(true);
    }

    public void disableSaveLocationField() {
        saveField.setEnabled(false);
    }

    public void enableEpisodeSpinner() {
        episodeSpinner.setEnabled(true);
    }

    public void disableEpisodeSpinner() {
        episodeSpinner.setEnabled(false);
    }

    @Override
    public void notifyBrowserObserver() {
        rtmpLocationField.setText(model.getRTMPdumpLocation());
        saveField.setText(model.getSaveLocationFile());
        episodeSpinner.setValue(Integer.decode(model.getEpisode()));

        panel1.updateUI();
    }
}
