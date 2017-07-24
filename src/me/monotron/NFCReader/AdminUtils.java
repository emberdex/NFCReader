package me.monotron.NFCReader;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by Toby on 11/07/2017.
 */
public class AdminUtils {

    public JFrame window;
    private JButton createAdminCardButton;
    private JButton invalidateAdminCardButton;
    private JButton writeIDToTagButton;
    private JButton invalidateTagButton;
    private JButton returnToKioskModeButton;
    private JButton exitToDesktopButton;
    private JPanel adminFrame;
    private JLabel statusLabel;

    /**
     * Code to initialise the GUI for the admin side.
     */
    public void initialise() {
        window = new JFrame("Administrator Access");

        window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        window.setContentPane(adminFrame);

        createAdminCardButton.addActionListener(e -> {
            statusLabel.setText("Waiting for card.");
            window.repaint();

            Card c;

            try {
                Main.terminal.waitForCardPresent(0);
            } catch (CardException ce) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            c = MifareUtils.getCardOn(Main.terminal);
            if(c == null) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            if(MifareUtils.isNFCTag(c)) {
                statusLabel.setText("Cannot use tag as admin card.");
                window.repaint();
                return;
            }

            boolean succ = MifareUtils.authenticate(c);
            if(!succ) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            boolean success = CardUtils.createAdminCard(c);
            if(!success) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            } else {
                statusLabel.setText("Operation succeeded.");
                window.repaint();
                return;
            }

        });

        invalidateAdminCardButton.addActionListener(e -> {
            statusLabel.setText("Waiting for card.");
            window.repaint();

            Card c;

            try {
                Main.terminal.waitForCardPresent(0);
            } catch (CardException ce) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            c = MifareUtils.getCardOn(Main.terminal);
            if(c == null) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            if(MifareUtils.isNFCTag(c)) {
                statusLabel.setText("Cannot use tag as admin card.");
                window.repaint();
                return;
            }

            boolean succ = MifareUtils.authenticate(c);
            if(!succ) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            boolean success = CardUtils.killAdminCard(c);
            if(!success) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            } else {
                statusLabel.setText("Operation succeeded.");
                window.repaint();
                return;
            }

        });

        writeIDToTagButton.addActionListener(e -> {
            statusLabel.setText("Waiting for card.");
            window.repaint();

            Card c;

            try {
                Main.terminal.waitForCardPresent(0);
            } catch (CardException ce) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            c = MifareUtils.getCardOn(Main.terminal);
            if (c == null) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            if (!MifareUtils.isNFCTag(c)) {
                statusLabel.setText("Cannot write to cards.");
                window.repaint();
                return;
            }

            int id;
            try {
                id = (int) JOptionPane.showInputDialog(adminFrame, "Enter an ID:", "Enter ID",
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
            } catch (ClassCastException cce) {
                statusLabel.setText("Please enter an integer.");
                window.repaint();
                return;
            }

            boolean succ = CardUtils.setID(c, id);
            if(!succ ) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            } else {
                statusLabel.setText("Operation succeeded.");
                window.repaint();
                return;
            }
        });

        invalidateTagButton.addActionListener(e -> {
            statusLabel.setText("Waiting for card.");
            window.repaint();

            Card c;

            try {
                Main.terminal.waitForCardPresent(0);
            } catch (CardException ce) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            c = MifareUtils.getCardOn(Main.terminal);
            if (c == null) {
                statusLabel.setText("Operation failed.");
                window.repaint();
                return;
            }

            if (!MifareUtils.isNFCTag(c)) {
                statusLabel.setText("Cannot write to cards.");
                window.repaint();
                return;
            }

            CardUtils.setID(c, 0);

        });

        exitToDesktopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        returnToKioskModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.setVisible(false);
            }
        });

        window.setSize(new Dimension(200, 300));
    }

    public void setVisibility(boolean visibility) {
        window.setVisible(visibility);
    }
}
