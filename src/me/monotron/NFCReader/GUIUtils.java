package me.monotron.NFCReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Toby on 11/07/2017.
 */
class GUIUtils {

    static JFrame window;
    private static JPanel content;
    private static JLabel text;

    /**
     * Method to initialise the GUI for the reader application.
     */
    static void initialise() {
        // Create the window.
        window = new JFrame("Tavis Booth MA - NFC Reader");
        content = new JPanel();

        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Set some properties about the window (i.e. no borders, uncloseable
        window.setSize(screenSize);
        window.setUndecorated(true);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setContentPane(content);

        // Set the initial layout.
        content.setLayout(new BorderLayout());
        content.setBackground(Color.DARK_GRAY);

        // Create the text label.
        text = new JLabel("Initialising...", SwingConstants.CENTER);
        text.setBackground(Color.DARK_GRAY);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 72));

        // Add the text to the window.
        content.add(text, BorderLayout.CENTER);

        // Show the window.
        window.setVisible(true);
    }

    /**
     * Method to update the text on the screen and repaint the window.
     * @param str The text to display in the window.
     */
    static void updateText(String str) {
        text.setText(str);
        window.repaint();
    }

    static void updateImage(String path) {
        BufferedImage img = null;

        // Load the image from file.
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(GUIUtils.window, "Failed to update the GUI image.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a new JLabel with the image inside.
        JLabel pic = new JLabel(new ImageIcon(img));
        pic.setSize(Toolkit.getDefaultToolkit().getScreenSize());

        // Remove everything from the content pane and add the image.
        window.getContentPane().removeAll();
        window.add(pic);
        window.repaint();
    }
}