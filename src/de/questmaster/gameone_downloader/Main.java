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

/**
 * Created by IntelliJ IDEA.
 * Date: 19.01.11
 * Time: 19:25
 * To change this template use File | Settings | File Templates.
 */

package de.questmaster.gameone_downloader;


import javax.swing.*;
import java.awt.*;

public class Main extends JApplet {

    private JApplet applet = this;

    public void init() {
        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.

        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Browser b = new Browser();
                    applet.add(b.getContentPane());
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't successfully complete");
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {

        // open main screen
        Browser b = new Browser();
        b.setVisible(true);
    }
}
