package de.questmaster.gameone_grabber;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
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
    private JTextField saveField;
    private JButton grabButton;
    private JButton selectButton;
    private JSpinner episodeSpinner;

    private String episodeNumber = "118";

    public Browser() {
        super("GameOne Grabber");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        setPreferredSize(new Dimension(400, 124));
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

                Grabber g = new Grabber(episodeNumber, saveField.getText());
                g.setVisible(true);

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
                   episodeNumber = String.valueOf((Integer) episodeSpinner.getValue());
                    while (episodeNumber.length() < 3) {
                        episodeNumber = "0" + episodeNumber;
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
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
}
