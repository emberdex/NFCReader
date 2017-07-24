package me.monotron.NFCReader;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.smartcardio.*;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.exit;
import static me.monotron.NFCReader.MifareUtils.isSuccess;

/**
 * Created by Toby on 05/07/2017.
 */
public class Main {

    static Card attachedCard;
    public static CardTerminal terminal;
    private static AdminUtils ut;

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

        ut = new AdminUtils();
        ut.initialise();

        LogUtils.log("Detected card reader: " + terminal.getName(), LogLevels.INFO);

        Thread t = new Thread(new LockdownThread(new ArrayList<String>(Arrays.asList(
                "explorer.exe", "Taskmgr.exe", "mmc.exe", "iexplore.exe", "notepad.exe"
        ))));

        //t.start();

        while(true) {
            LogUtils.log("Waiting for a globe.", LogLevels.INFO);

            new AdminUtils().initialise();

            GUIUtils.updateText("Place a globe on the reader.");
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
                GUIUtils.updateText("Invalid card. Please remove to continue.");
                MifareUtils.waitForCardRemovalOn(terminal);
                continue;
            }

            // Authentamacate
            if(!MifareUtils.isNFCTag(attachedCard)) {
                boolean success = MifareUtils.authenticate(attachedCard);
                if(!success) {
                    GUIUtils.updateText("Couldn't communicate with the card.");
                }
            }

            // Open browser window
            if(MifareUtils.isNFCTag(attachedCard)) {
                CardUtils.openBrowserForId(CardUtils.getId(attachedCard));
            } else {
                // do stuff for admin card
                if(CardUtils.isAdminCard(attachedCard)) {
                    GUIUtils.updateText("ADMIN MODE");
                    ut.setVisibility(true);
                    try {
                        terminal.waitForCardAbsent(0);
                    } catch (CardException ce) {}
                    finally {
                        ut.setVisibility(false);
                    }
                } else {
                    GUIUtils.updateText("Invalid card.");
                }
            }

            GUIUtils.updateText("Remove the globe from the reader.");

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
