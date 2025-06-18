package org.example.components;

import javax.swing.*;

public class RadioSel {
    private JPanel panel;
    private JRadioButton[] radioButtons;
    private ButtonGroup buttonGroup;

    public RadioSel(String[] options) {
        panel = new JPanel();
        buttonGroup = new ButtonGroup();
        radioButtons = new JRadioButton[options.length];
        for (int i = 0; i < options.length; i++) {
            radioButtons[i] = new JRadioButton(options[i]);
            buttonGroup.add(radioButtons[i]);
            panel.add(radioButtons[i]);
        }
        // Set the first radio button as selected by default
        if (radioButtons.length > 0) {
            radioButtons[0].setSelected(true);
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getSelectedOption() {
        for (JRadioButton radioButton : radioButtons) {
            if (radioButton.isSelected()) {
                return radioButton.getText();
            }
        }
        return null; // No option selected
    }
}
