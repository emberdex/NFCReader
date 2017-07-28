package me.monotron.NFCReader;

import javax.smartcardio.*;
import javax.swing.*;
import java.io.IOException;

import static java.lang.System.exit;

/**
 * Created by Toby on 05/07/2017.
 */
public class Main {

    static CardTerminal terminal;
    private static AdminUtils ut = new AdminUtils();

    public static void main(String[] args) throws java.io.IOException {

        // Initialise the main GUI.
        GUIUtils.initialise();

        LogUtils.log("Detecting card reader...", LogLevels.INFO);

        // Detect the correct card reader.
        // We want specifically an ACS ACR122U reader.
        terminal = MifareUtils.detectAcrReader();
        if(terminal == null) {
            JOptionPane.showMessageDialog(GUIUtils.window, "Failed to detect a valid card reader.\n" +
                            "Please connect an ACS ACR122U reader to this computer, and relaunch the application.",
                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
            exit(1);
        }

        // Initialise the admin tools window.
        ut.initialise();

        LogUtils.log("Detected card reader: " + terminal.getName(), LogLevels.INFO);

        while(true) {
            LogUtils.log("Waiting for a globe.", LogLevels.INFO);

            // Update the GUI images.
            GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\place.png");

            // Wait until the card is present.
            try {
                terminal.waitForCardPresent(0);
            } catch (CardException ce) {
                System.out.println(String.format("CardException: %s", ce.getMessage()));

                // If the reader becomes unavailable, display a message box.
                if(ce.getCause().getMessage().equals("SCARD_E_NO_READERS_AVAILABLE")) {
                    JOptionPane.showMessageDialog(GUIUtils.window, "Communication with the card reader was lost while waiting for a card.",
                            "Fatal Error", JOptionPane.ERROR_MESSAGE);
                }

                exit(1);
            }

            // Get the card on the terminal.
            Card attachedCard = MifareUtils.getCardOn(terminal);
            if(attachedCard == null) {
                LogUtils.log("Failed to read the data on the card.", LogLevels.ERROR);
                continue;
            }

            // Get the ATR from the card.
            LogUtils.log("Reading ATR...", LogLevels.INFO);
            ATR atr = attachedCard.getATR();

            boolean isMifareCard = MifareUtils.isValidMifareCard(atr.getBytes());

            // Check whether the card is actually a Mifare card.
            if(!isMifareCard) {
                GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\error.png");
                MifareUtils.waitForCardRemovalOn(terminal);
                continue;
            }

            // Authentamacate
            if(!MifareUtils.isNFCTag(attachedCard)) {
                boolean success = MifareUtils.authenticate(attachedCard);
                if(!success) {
                    GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\error.png");
                }
            }

            // Open browser window for attached card.
            if(!CardUtils.isAdminCard(attachedCard) && MifareUtils.isNFCTag(attachedCard)) {
                int returnedID = CardUtils.getId(attachedCard);
                if(returnedID != 0) {
                    BrowserUtils.launchBrowser(returnedID, true);

                    try {
                        terminal.waitForCardAbsent(0);
                    } catch (CardException ce) {}

                    BrowserUtils.killBrowser();
                }
            } else {
                // do stuff for admin card
                if(CardUtils.isAdminCard(attachedCard)) {
                    GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\sandvich.png");
                    ut.window.setVisible(true);

                } else {
                    // otherwise, assume something went wrong, display error
                    GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\error.png");
                }
            }

            // Display image to tell the user to remove the globe
            GUIUtils.updateImage(System.getProperty("user.dir") + "\\images\\remove.png");

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
