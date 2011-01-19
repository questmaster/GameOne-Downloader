package de.questmaster.gameone_grabber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel
 * Date: 19.01.11
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public class Browser extends JFrame {
    private JEditorPane browserPane;
    private JPanel panel1;
    private JTextField adressField;
    private JButton getEpisodeButton;

    public Browser() {
        super("GameOne Grabber");

        setContentPane(panel1);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        adressField.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                try{
                    browserPane.setPage(new URL(adressField.getText()));
                } catch (IOException ex) {
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

}
