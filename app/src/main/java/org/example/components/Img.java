package org.example.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Img component for displaying images in a Swing application.
 * Get image as ressource.
 */
public class Img {
    public static JLabel createImageLabel(String imagePath, int width, int height) {
        ImageIcon imageIcon = new ImageIcon(Img.class.getResource(imagePath), "Image");
        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_DEFAULT); // scale it the smooth way
        imageIcon = new ImageIcon(newimg); // transform it back
        JLabel label = new JLabel(imageIcon, JLabel.CENTER);
        label.setPreferredSize(new java.awt.Dimension(width, height));
        label.setMinimumSize(new java.awt.Dimension(width, height));
        label.setMaximumSize(new java.awt.Dimension(width, height));
        // label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
}
