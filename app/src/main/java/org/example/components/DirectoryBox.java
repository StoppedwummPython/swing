package org.example.components;

import javax.swing.*;

public class DirectoryBox {
    private JPanel panel;
    private JTextField directoryField;
    private JButton browseButton;

    public DirectoryBox() {
        panel = new JPanel();
        directoryField = new JTextField(20);
        browseButton = new JButton("Browse");

        panel.add(directoryField);
        panel.add(browseButton);

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(panel);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                directoryField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getDirectory() {
        return directoryField.getText();
    }
    
}
