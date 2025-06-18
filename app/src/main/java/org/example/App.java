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
                    Installing installingWindow = new Installing();
                    installingWindow.createWindow();
                    System.out.println("Selected option: " + selectedOption);
                    Console.createWindow(new String[][]{
                        {"powershell.exe", "npm", "i"},
                        {"powershell.exe", "npm", "run", "start"}
                    });
                    installingWindow.closeWindow();
                    break;
                default:
                    System.out.println("Unknown option selected: " + selectedOption);
            }
        });
        startWindow.showWindow();
    }
}
