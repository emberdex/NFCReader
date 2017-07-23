package me.monotron.NFCReader;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Toby on 11/07/2017.
 */
public class AdminUtils {

    static JFrame window;
    static JPanel content;

    /**
     * Code to initialise the GUI for the admin side.
     */
    public static void initialise() {
        window = new JFrame("Administrator Access");
        content = new JPanel();

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setContentPane(content);

        content.setLayout(new BorderLayout());
        content.setBackground(Color.DARK_GRAY);
    }
}
