package org.example.windows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.example.utils.*;

import javax.swing.*;

public class Console {
    public static void createWindow(String[][] commands) {
        JFrame frame = new JFrame("Console");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.add(scrollPane);

        frame.setVisible(true);

        SoftwareInstaller softwareInstaller = new SoftwareInstaller();
        try {
            softwareInstaller.installSoftware("https://nodejs.org/dist/v22.16.0/node-v22.16.0-win-x64.zip", "Node", ".");
        }  catch (Exception e) {
            e.printStackTrace();
            textArea.append("Error installing software: " + e.getMessage() + "\n");
        }

        for (String[] command : commands) {
            try {
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(command);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                // Read the output from the command
                System.out.println("Here is the standard output of the command:\n");
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                    textArea.append(s + "\n"); // Append output to the text area
                }

                // Read any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                    textArea.append("Error: " + s + "\n"); // Append error to the text area
                }

            } catch (Exception e) {
                e.printStackTrace();
                textArea.append("Error executing command: " + e.getMessage() + "\n");
            }
        }

        
    }
}
