package de.questmaster.gameone_grabber;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel
 * Date: 19.01.11
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public class Browser extends JFrame {
    private JPanel panel1;
    private JTextField episodeField;
    private JTextField saveField;
    private JButton grabButton;
    private JButton selectButton;

    public Browser() {
        super("GameOne Grabber");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        pack();
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
                    saveField.setText(chooser.getSelectedFile().getAbsolutePath());
                }

            }
        });
        episodeField.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                if (episodeField.getText().startsWith("http://www.gameone.de/tv/") && episodeField.getText().length() > 26) {
                    String episodeNumber = episodeField.getText().substring(25);

                    int i = 0;
                    while (i < episodeNumber.length() && Character.isDigit(episodeNumber.charAt(i))) {
                        i++;
                    }

                    if (i > 0) {
                        episodeNumber = episodeNumber.substring(0, i);
                        saveField.setText(saveField.getText().replace("XXX", episodeNumber));
                        grabButton.setEnabled(true);
                        episodeField.setEnabled(false);
                    }
                }
            }
        });
        grabButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                saveField.setEnabled(false);
                selectButton.setEnabled(false);

                // TODO: start grabing

                grabButton.setEnabled(false);
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

}
