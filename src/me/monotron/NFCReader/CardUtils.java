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
class CardUtils {

    // The offset on the tag at which to store the ID.
    private static final byte   ID_OFFSET       = (byte) 0x08;
    // The size of the ID, in 4-byte pages.
    private static final byte   ID_PAGES        = (byte) 0x02;
    // The length of the ID, rounded to the nearest page.
    private static final byte   ID_LENGTH       = (byte) 0x08;
    // The roundme URL.
    public static final String ROUNDME_URL     = "https://roundme.com/tour/%d";
    // The admin magic page data.
    private static final byte[] ADMIN_DATA      = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }; // cafe babe
    // just a blank page
    private static final byte[] KILL_ADMIN_DATA = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }; // not cafe babe
    // The offset at which to store admin data.
    private static final byte   ADMIN_OFFSET    = (byte) 0x02;

    /**
     * Method to get the ID of a Roundme image from a NFC tag.
     * @param card The card from which the ID will be read.
     * @return The ID of the roundme image stored on the card.
     */
    static int getId(Card card) {

        // If the card is not a tag
        if(!MifareUtils.isNFCTag(card)) {
            System.err.println("This is not a globe. Not going to read the ID.");
            return 0;
        }

        // Read the response from the card.
        byte[] response = MifareUtils.readPages(card, ID_OFFSET, ID_LENGTH);
        if(!MifareUtils.isSuccess(response)) {
            System.err.println("Failed to read the ID from the tag.");
            return 0;
        }

        // Remove the status bytes from the card.
        response = MifareUtils.chopStatusBytes(response);

        // Convert the response to a string, chop the last 2 extraneous bytes.
        String id = new String(MifareUtils.chopStatusBytes(response));

        int retVal = 0;

        // Parse the read ID to an integer.
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
    static boolean setID(Card card, int id) {

        if(!MifareUtils.isNFCTag(card)) {
            System.err.println("This is not a globe. Not going to write the ID.");
            return false;
        }

        // Write the integer to the card.
        return MifareUtils.writePages(card, ID_OFFSET, ID_PAGES, Integer.toString(id).getBytes());
    }

    /**
     * Method to check if a card is an admin card, for use in breaking out of the sandbox.
     * @param card The card to check.
     * @return A boolean corresponding to whether the card is an admin card.
     */
    static boolean isAdminCard(Card card) {
        if(MifareUtils.isNFCTag(card)) return false;

        // Read the data from the card and return the status.
        byte[] response = MifareUtils.readSector(card, ADMIN_OFFSET);
        return (Arrays.equals(MifareUtils.chopStatusBytes(response), ADMIN_DATA));
    }

    /**
     * Method to create an admin card, for use in breaking out of the sandbox.
     * @param card The card to promote to an admin.
     * @return A boolean corresponding to whether the operation succeeded or not.
     */
    static boolean createAdminCard(Card card) {
        return !MifareUtils.isNFCTag(card) && MifareUtils.writeSector(card, ADMIN_OFFSET, ADMIN_DATA);
    }

    /**
     * Method to invalidate an existing admin card.
     * @param card The card to invalidate.
     * @return A boolean corresponding to whether the operation succeeded or not.
     */
    static boolean killAdminCard(Card card) {
        return !MifareUtils.isNFCTag(card) && MifareUtils.writeSector(card, ADMIN_OFFSET, KILL_ADMIN_DATA);
    }

    /**
     * Method to open the browser window for an ID.
     * @param id The ID of the roundme image to display.
     */
    static void openBrowserForId(int id) {
        BrowserUtils.launchBrowser(id, true);
    }
}
