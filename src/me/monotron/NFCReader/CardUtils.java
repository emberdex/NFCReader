package me.monotron.NFCReader;

import javax.smartcardio.Card;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by Toby on 10/07/2017.
 */
public class CardUtils {

    private static final byte   ID_OFFSET       = (byte) 0x08;
    private static final byte   ID_PAGES        = (byte) 0x02;
    private static final byte   ID_LENGTH       = (byte) 0x08;
    private static final String ROUNDME_URL     = "https://roundme.com/tour/%d";
    private static final byte[] ADMIN_DATA      = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }; // cafe babe
    private static final byte   ADMIN_OFFSET    = (byte) 0x02;
    private static final byte   ADMIN_LENGTH    = (byte) 0x04;

    /**
     * Method to get the ID of a Roundme image from a NFC tag.
     * @param card The card from which the ID will be read.
     * @return The ID of the roundme image stored on the card.
     */
    public static int getId(Card card) {

        if(!MifareUtils.isNFCTag(card)) {
            System.err.println("This is not a globe. Not going to read the ID.");
            return 0;
        }

        byte[] response = MifareUtils.readPages(card, ID_OFFSET, ID_LENGTH);
        if(!MifareUtils.isSuccess(response)) {
            System.err.println("Failed to read the ID from the tag.");
            return 0;
        }

        response = MifareUtils.chopStatusBytes(response);

        String id = new String(MifareUtils.chopStatusBytes(response));

        int retVal = 0;

        try {
            retVal = Integer.parseInt(id);
        } catch (NumberFormatException nfe) {
            System.out.println(String.format("NumberFormatException - invalid data on the card: %s",
                    nfe.getMessage()));
            nfe.printStackTrace();
        }

        return retVal;
    }

    /**
     * Method to write a Roundme ID to a NFC tag.
     * @param card The card to which the ID should be written.
     * @param id The ID to write to the card.
     * @return true if the write succeeded, otherwise false
     */
    public static boolean setID(Card card, int id) {

        if(!MifareUtils.isNFCTag(card)) {
            System.err.println("This is not a globe. Not going to read the ID.");
            return false;
        }

        return MifareUtils.writePages(card, ID_OFFSET, ID_PAGES, Integer.toString(id).getBytes());
    }

    /**
     * Method to check if a card is an admin card, for use in breaking out of the sandbox.
     * @param card The card to check.
     * @return A boolean corresponding to whether the card is an admin card.
     */
    public static boolean isAdminCard(Card card) {
        if(MifareUtils.isNFCTag(card)) return false;

        byte[] response = MifareUtils.readSector(card, ADMIN_OFFSET);
        return (Arrays.equals(MifareUtils.chopStatusBytes(response), ADMIN_DATA));
    }

    /**
     * Method to create an admin card, for use in breaking out of the sandbox.
     * @param card The card to promote to an admin.
     * @return A boolean corresponding to whether the operation succeeded or not.
     */
    public static boolean createAdminCard(Card card) {
        if(MifareUtils.isNFCTag(card)) return false;

        return MifareUtils.writeSector(card, ADMIN_OFFSET, ADMIN_DATA);
    }

    /**
     * Method to open the browser window for an ID.
     * @param id The ID of the roundme image to display.
     */
    public static void openBrowserForId(int id) {
        if(Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(String.format(ROUNDME_URL, id)));
            } catch (URISyntaxException | IOException e) {
                System.out.println(String.format("%s: %s", e.getClass().toString(), e.getMessage()));
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to open the browser window.");
        }
    }
}
