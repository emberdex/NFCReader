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
        window = new JFrame("Tavis Booth MA - NFC Reader");
        content = new JPanel();

        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        window.setSize(screenSize);
        window.setUndecorated(true);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setContentPane(content);

        content.setLayout(new BorderLayout());
        content.setBackground(Color.DARK_GRAY);

        text = new JLabel("Initialising...", SwingConstants.CENTER);
        text.setBackground(Color.DARK_GRAY);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 72));

        content.add(text, BorderLayout.CENTER);

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

    static void updateImage(String path) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        JLabel pic = new JLabel(new ImageIcon(img));
        pic.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        window.getContentPane().removeAll();
        window.add(pic);
        window.repaint();
        updateText("");
    }
}
