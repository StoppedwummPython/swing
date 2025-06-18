package org.example.windows;

import java.util.function.BiConsumer; // Use BiConsumer for two arguments
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

// Assuming these are custom components
import org.example.components.DirectoryBox;
import org.example.components.RadioSel;
// import org.example.components.Img; // If you uncomment this line later

public class StartWindow {
    private JFrame frame;
    private DirectoryBox directoryBox;
    private RadioSel radioSel;
    private JButton submitButton;

    // Field to store the callback function
    private BiConsumer<String, String> submitCallback;

    public StartWindow() {
        frame = new JFrame("Example Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        directoryBox = new DirectoryBox();
        radioSel = new RadioSel(new String[] { "Install Client", "Create FAT Installer" });

        // frame.add(Img.createImageLabel("/img/Minecraft-New-Logo.png", 400, 200));

        frame.add(directoryBox.getPanel());
        frame.add(radioSel.getPanel());

        submitButton = new JButton("Submit");

        // Add the action listener to the button created in the constructor
        submitButton.addActionListener(e -> {
            String selectedOption = radioSel.getSelectedOption();
            String directory = directoryBox.getDirectory();

            // You can optionally keep the JOptionPane for debugging or remove it
            // JOptionPane.showMessageDialog(frame, "Selected Option: " + selectedOption + "\nDirectory: " + directory);

            // Call the callback if it has been set
            if (submitCallback != null) {
                try {
                    submitCallback.accept(selectedOption, directory);
                } catch (Exception ex) {
                    // Handle potential exceptions from the callback if needed
                    ex.printStackTrace(); // Or log the error
                    JOptionPane.showMessageDialog(frame, "Error during submit action: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Optional: provide feedback if no callback is set
                System.out.println("Submit button clicked, but no callback was registered.");
            }
        });

        frame.add(submitButton);
    }

    public void showWindow() {
        // Ensure the frame is packed and centered before showing (optional but good practice)
        frame.pack(); // Packs components nicely based on their preferred sizes
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }

    public void closeWindow() {
        frame.setVisible(false);
        frame.dispose();
    }

    /**
     * Sets the action to be performed when the submit button is clicked.
     *
     * @param callback A BiConsumer that accepts the selected option (String)
     *                 and the selected directory (String).
     */
    public void onSubmit(BiConsumer<String, String> callback) {
        // Store the provided callback function
        this.submitCallback = callback;

        // DO NOT create a new button or add components here.
        // This method's purpose is only to define *what* happens when the *existing*
        // submit button is clicked.
    }

    // Example Usage (you might put this in a separate Main class)
    public static void main(String[] args) {
        // Swing applications must be created and run on the Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StartWindow startWindow = new StartWindow();

                // Register the callback using the onSubmit method
                startWindow.onSubmit((option, directory) -> {
                    System.out.println("Callback triggered!");
                    System.out.println("Option: " + option);
                    System.out.println("Directory: " + directory);

                    // Perform your application logic here based on the option and directory
                    if ("Install Client".equals(option)) {
                        // Logic for installing client
                        JOptionPane.showMessageDialog(null, "Simulating Client Installation to: " + directory);
                        // startWindow.closeWindow(); // Example: Close window after action
                    } else if ("Create FAT Installer".equals(option)) {
                        // Logic for creating FAT installer
                         JOptionPane.showMessageDialog(null, "Simulating FAT Installer Creation in: " + directory);
                        // startWindow.closeWindow(); // Example: Close window after action
                    }
                    // You could also show another window or update the current one
                });

                startWindow.showWindow();
            }
        });
    }
}