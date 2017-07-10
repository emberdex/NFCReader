package me.monotron.NFCReader;

import javax.smartcardio.*;
import java.util.Arrays;

import static java.lang.System.exit;
import static me.monotron.NFCReader.MifareUtils.isSuccess;

/**
 * Created by Toby on 05/07/2017.
 */
public class Main {

    static Card attachedCard;

    public static void main(String[] args) {
        System.out.println("Detecting card reader...");

        CardTerminal terminal = MifareUtils.detectAtrReader();
        if(terminal == null) {
            System.out.println("Failed to detect a proper ATR122 reader.");
            exit(1);
        }

        System.out.println("Detected card reader: " + terminal.getName());

        while(true) {
            // check if admin card

            try {
                terminal.waitForCardPresent(0);
            } catch (CardException ce) {
                System.out.println(String.format("CardException: %s", ce.getMessage()));
                ce.printStackTrace();
                break;
            }

            attachedCard = MifareUtils.getCardOn(terminal);
            if(attachedCard == null) {
                System.out.println("Failed to read the information on the card.");
                continue;
            }

            System.out.println("Reading initial card information.");

            System.out.println("Getting ATR...");
            ATR atr = attachedCard.getATR();

            System.out.print("Are we a valid MiFare smart card... ");
            boolean isMifareCard = MifareUtils.isValidMifareCard(atr.getBytes());

            System.out.println((isMifareCard) ? "yes!" : "no!");

            if(!isMifareCard) continue;

            // Authentamacate
            if(MifareUtils.isNFCTag(attachedCard)) {
                boolean success = MifareUtils.authenticate(attachedCard);
                if(!success) {
                    System.out.println("Failed to authenticate with the card.");
                    continue;
                }
            }

            // Open browser window
            if(MifareUtils.isNFCTag(attachedCard)) {
                CardUtils.openBrowserForId(CardUtils.getId(attachedCard));
            } else {
                // do stuff for admin card
            }

            try {
                terminal.waitForCardAbsent(0);
            } catch (CardException ce) {
                System.out.println(String.format("CardException: %s", ce.getMessage()));
                ce.printStackTrace();
            }
        }
    }
}
