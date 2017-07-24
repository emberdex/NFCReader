package me.monotron.NFCReader;

import javax.smartcardio.*;
import javax.swing.*;
import java.io.IOException;

import static java.lang.System.exit;

/**
 * Created by Toby on 05/07/2017.
 */
public class Main {

    static Card attachedCard;
    public static CardTerminal terminal;
    static AdminUtils ut = new AdminUtils();

    public static void main(String[] args) throws java.io.IOException {

        GUIUtils.initialise();

        LogUtils.log("Detecting card reader...", LogLevels.INFO);

        terminal = MifareUtils.detectAcrReader();
        if(terminal == null) {
            JOptionPane.showMessageDialog(GUIUtils.window, "Failed to detect a valid card reader.\n" +
                            "Please connect an ACS ACR122U reader to this computer, and relaunch the application.",
                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
            exit(1);
        }

        ut.initialise();

        LogUtils.log("Detected card reader: " + terminal.getName(), LogLevels.INFO);

        while(true) {
            LogUtils.log("Waiting for a globe.", LogLevels.INFO);

            new AdminUtils().initialise();

            System.out.println(System.getProperty("user.dir"));

            try {
                GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\place.png");
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(GUIUtils.window, "Failed to update the GUI image.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                terminal.waitForCardPresent(0);
            } catch (CardException ce) {
                System.out.println(String.format("CardException: %s", ce.getMessage()));

                if(ce.getCause().getMessage().equals("SCARD_E_NO_READERS_AVAILABLE")) {
                    JOptionPane.showMessageDialog(GUIUtils.window, "Communication with the card reader was lost while waiting for a card.",
                            "Fatal Error", JOptionPane.ERROR_MESSAGE);
                }

                exit(1);
            }

            attachedCard = MifareUtils.getCardOn(terminal);
            if(attachedCard == null) {
                LogUtils.log("Failed to read the data on the card.", LogLevels.ERROR);
                continue;
            }

            LogUtils.log("Reading ATR...", LogLevels.INFO);
            ATR atr = attachedCard.getATR();

            boolean isMifareCard = MifareUtils.isValidMifareCard(atr.getBytes());

            if(!isMifareCard) {
                try {
                    GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\error.png");
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(GUIUtils.window, "Failed to update the GUI image.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                MifareUtils.waitForCardRemovalOn(terminal);
                continue;
            }

            // Authentamacate
            if(!MifareUtils.isNFCTag(attachedCard)) {
                boolean success = MifareUtils.authenticate(attachedCard);
                if(!success) {
                    try {
                        GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\error.png");
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(GUIUtils.window, "Failed to update the GUI image.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            // Open browser window
            if(MifareUtils.isNFCTag(attachedCard)) {
                CardUtils.openBrowserForId(CardUtils.getId(attachedCard));
            } else {
                // do stuff for admin card
                if(CardUtils.isAdminCard(attachedCard)) {
                    try {
                        GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\sandvich.png");
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(GUIUtils.window, "Failed to update the GUI image.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    ut.setVisibility(true);
                    try {
                        terminal.waitForCardAbsent(0);
                    } catch (CardException ce) {}
                    finally {
                        ut.setVisibility(false);
                    }
                } else {
                    try {
                        GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\error.png");
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(GUIUtils.window, "Failed to update the GUI image.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            try {
                GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\remove.png");
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(GUIUtils.window, "Failed to update the GUI image.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                terminal.waitForCardAbsent(0);
            } catch (CardException ce) {
                LogUtils.log("Failed to communicate with the card reader.", LogLevels.ERROR);

                if(ce.getCause().getMessage().equals("SCARD_E_NO_READERS_AVAILABLE")) {
                    JOptionPane.showMessageDialog(GUIUtils.window, "Communication with the card reader was lost while waiting for a card.",
                            "Fatal Error", JOptionPane.ERROR_MESSAGE);
                }

                ce.printStackTrace();
            }
        }
    }
}
