package org.example.windows;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Installing {
    private JFrame frame;
    public Installing() {}
    public void createWindow() {
        // Create a new window for the installation process
        frame = new JFrame("Installing Software");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null); // Center the window on the screen
        JLabel label = new JLabel("Installing... Please wait.", JLabel.CENTER);
        frame.add(label);
        frame.setVisible(true);
    }
    public void closeWindow() {
        if (frame != null) {
            frame.dispose(); // Close the window
        }
    }
}