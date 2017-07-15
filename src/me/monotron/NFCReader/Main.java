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

    public static void main(String[] args) throws java.io.IOException {

        GUIUtils.initialise();

        System.out.println("Detecting card reader...");

        CardTerminal terminal = MifareUtils.detectAcrReader();
        if(terminal == null) {
            JOptionPane.showMessageDialog(GUIUtils.window, "Failed to detect a valid card reader.\n" +
                            "Please connect an ACS ACR122U reader to this computer, and relaunch the application.",
                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
            exit(1);
        }

        System.out.println("Detected card reader: " + terminal.getName());

        Thread t = new Thread(new LockdownThread(new ArrayList<String>(Arrays.asList(
                "explorer.exe", "Taskmgr.exe", "mmc.exe", "iexplore.exe", "notepad.exe"
        ))));

        //t.start();

        while(true) {
            System.out.println("Waiting for a valid card on the reader.");

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
                System.out.println("Failed to read the information on the card.");
                continue;
            }

            System.out.println("Getting ATR...");
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
                    GUIUtils.updateText("Admin card detected. Closing.");
                    try {
                        Thread.sleep(3000);
                        Runtime.getRuntime().exec("explorer.exe");
                    } catch (InterruptedException | IOException ie) {}
                    finally {
                        System.exit(0xDEADCAFE);
                    }
                } else {
                    GUIUtils.updateText("Invalid card.");
                }
            }

            GUIUtils.updateText("Remove the globe from the reader.");

            try {
                terminal.waitForCardAbsent(0);
            } catch (CardException ce) {
                System.out.println(String.format("CardException: %s", ce.getMessage()));

                if(ce.getCause().getMessage().equals("SCARD_E_NO_READERS_AVAILABLE")) {
                    JOptionPane.showMessageDialog(GUIUtils.window, "Communication with the card reader was lost while waiting for a card.",
                            "Fatal Error", JOptionPane.ERROR_MESSAGE);
                }

                ce.printStackTrace();
            }
        }
    }
}
