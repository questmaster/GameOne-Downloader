/**
 * Created by IntelliJ IDEA.
 * User: Daniel
 * Date: 19.01.11
 * Time: 19:25
 * To change this template use File | Settings | File Templates.
 */

package de.questmaster.gameone_grabber;


import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {
        // open main screen
        Browser b = new Browser();

        b.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        b.setSize(600, 800);
        b.setVisible(true);
    }
}
