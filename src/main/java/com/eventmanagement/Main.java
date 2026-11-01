package com.eventmanagement;

import com.eventmanagement.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 8);

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}