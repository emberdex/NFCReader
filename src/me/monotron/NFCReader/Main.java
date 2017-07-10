package me.monotron.NFCReader;

import javax.smartcardio.*;
import java.util.Arrays;

import static java.lang.System.exit;
import static me.monotron.NFCReader.MifareUtils.isSuccess;

/**
 * Created by Toby on 05/07/2017.
 */
public class Main {
    public static void main(String[] args) {

        System.out.println("Detecting card reader...");

        CardTerminal terminal = MifareUtils.detectAtrReader();
        if(terminal == null) {
            System.out.println("Failed to detect a proper ATR122 reader.");
            exit(1);
        }

        System.out.println("Detected card reader: " + terminal.getName());
        System.out.println("Place a valid smartcard on the reader.");

        MifareUtils.waitForCardOn(terminal);

        System.out.println("Reading initial card information.");
        Card attachedCard = MifareUtils.getCardOn(terminal);
        if(attachedCard == null) {
            System.out.println("Failed to read the information on the card.");
            exit(1);
        }

        System.out.println("Disabling beep.");
        MifareUtils.toggleBuzzer(attachedCard, false);

        System.out.println("Getting ATR...");
        ATR atr = attachedCard.getATR();

        System.out.print("Are we a valid MiFare smart card... ");
        boolean isMifareCard = MifareUtils.isValidMifareCard(atr.getBytes());

        System.out.println((isMifareCard) ? "yes!" : "no!");

        if(!isMifareCard) exit(1);

        // Authentamacate
        if(MifareUtils.getMifareType(atr.getBytes()) != 0x03) {
            boolean success = MifareUtils.authenticate(attachedCard);
            if(!success) {
                System.out.println("Failed to authenticate with the card.");
                exit(1);
            }
        }

        // Print data on sector 01
        if(MifareUtils.getMifareType(atr.getBytes()) != 0x03) {
            System.out.println("Printing data on sector 0x01.");
            byte[] response = MifareUtils.readSector(attachedCard, (byte) 0x01);
            if(!isSuccess(response)) {
                System.out.println("Failed to read the data. Response:");
                System.out.println(Arrays.toString(response));
                exit(1);
            }
            System.out.println(Arrays.toString(response));
        } else {
            System.out.println("Printing ID.");
            byte[] response = MifareUtils.readPages(attachedCard, (byte) 0x04, (byte) 0x08);
            if(!isSuccess(response)) {
                System.out.println("Failed to read the data. Response:");
                System.out.println(Arrays.toString(response));
                exit(1);
            }
            System.out.println(new String(response));
        }

        // Maybe write some data now?
        if(MifareUtils.getMifareType(atr.getBytes()) != 0x03) {
            boolean succ = MifareUtils.writeSector(attachedCard, (byte) 0x02, new byte[] { 0x01, 0x02, 0x03 });
            if(!succ) {
                System.out.println("Failed to write data to sector.");
                exit(1);
            }
        } else {
            boolean succ = MifareUtils.writePages(attachedCard, (byte) 0x04, (byte) 0x02, new byte[]{ 0x05, 0x02, 0x05, 0x02, 0x05, 0x02 });
            if(!succ) {
                System.out.println("Failed to write data to card.");
                exit(1);
            }
        }


        System.out.println("Enabling beep");
        MifareUtils.toggleBuzzer(attachedCard, true);

        System.out.println("Wrote some data too!");
    }
}
