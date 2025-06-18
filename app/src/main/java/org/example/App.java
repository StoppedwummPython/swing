/*
 * Example swing application.
 */
package org.example;

import org.example.windows.*;

public class App {
    public static void main(String[] args) {
        StartWindow startWindow = new StartWindow();
        startWindow.onSubmit((selectedOption, directory) -> {
            switch (selectedOption) {
                case "Install Client":
                    startWindow.closeWindow();
                    Console.createWindow(new String[][]{
                        {"powershell.exe", "echo", "'Installing Client...'"}
                    });
                    break;
                default:
                    System.out.println("Unknown option selected: " + selectedOption);
            }
        });
        startWindow.showWindow();
    }
}
