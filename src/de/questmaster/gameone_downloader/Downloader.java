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
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Downloader extends JDialog implements DownloaderObserver {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea dumpOutput;

    private ResourceBundle resBundle = ResourceBundle.getBundle("de.questmaster.gameone_downloader.i18n");

    private DownloaderControllerInterface controller;
    private DownloadParserModelInterface model;

    public Downloader(DownloaderControllerInterface controller, DownloadParserModelInterface model) {
        this.model = model;
        this.controller = controller;

        this.model.registerObserver(this);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(MessageFormat.format(resBundle.getString("downloader.gameone.episode"), this.model.getEpisode()));
        setPreferredSize(new Dimension(800, 500));
        pack();

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

        setVisible(true);
    }

    private void onOK() {
        controller.onOk();
    }

    private void onCancel() {
        controller.onCancel();
    }

    public void enableOk() {
        buttonOK.setEnabled(true);
    }

    public void disableOk() {
        buttonOK.setEnabled(false);
    }

    public void enableCancel() {
        buttonCancel.setEnabled(true);
    }

    public void disableCancel() {
        buttonCancel.setEnabled(false);
    }

    @Override
    public void notifyDownloaderObserver() {

        // scroll to the end if on last position
        boolean scroll = false;
        if (dumpOutput.getCaretPosition() >= dumpOutput.getDocument().getLength() - 2)
            scroll = true;

        // appendData
        dumpOutput.append(model.retrieveParserOutput());

        // show last line
        if (scroll)
            dumpOutput.setCaretPosition(dumpOutput.getDocument().getLength());

        if (model.isDownloadFinished()) {
            disableCancel();
            enableOk();
        }
    }
}
