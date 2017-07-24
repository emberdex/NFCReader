package me.monotron.NFCReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Toby on 24/07/2017.
 */
public class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel(String path) throws IOException {
        image = ImageIO.read(new File(path));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }
}
