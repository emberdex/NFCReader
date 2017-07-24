package me.monotron.NFCReader;

import javax.smartcardio.*;

/**
 * Created by Toby on 06/07/2017.
 */
public class MifareUtils {
    // APDUs for various operations on the card, as well as the CC.
    // apduGetKey is hardcoded to suit my particular reader, but CommandCode can be changed.
    private static final byte commandCode = (byte) 0xFF;
    // APDU command for getting keys off the card. We then use this to authenticate. (all hardcoded, change at will)
    private static final byte[] apduGetKey = { commandCode, (byte) 0x82, (byte) 0x00, (byte) 0x00, (byte) 0x06,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
    // APDU command for authenticating so we can read/write the card.
    private static final byte[] apduAuthenticate = { commandCode, (byte) 0x86, (byte) 0x00, (byte) 0x00,
            (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x00 };
    // APDU command for reading a sector off a Mifare1k. The response should be no more than 16 bytes for a Mifare1K card.
    private static final byte[] apduReadSector = { commandCode, (byte) 0xB0, (byte) 0x00, (byte) 0x01, (byte) 0x10 };
    // APDU command for writing a sector to a Mifare1k. Append the data to the end.
    private static final byte[] apduWriteSectorPartial = { commandCode, (byte) 0xD6, (byte) 0x00, (byte) 0x01, (byte) 0x10 };
    // APDU command for disabling the beep
    private static final byte[] apduDisableBeep = { commandCode, (byte) 0x00, (byte) 0x52, (byte) 0x00, (byte) 0x00 };
    // APDU command for enabling the beep
    private static final byte[] apduEnableBeep = { commandCode, (byte) 0x00, (byte) 0x52, (byte) 0x00, (byte) 0x00 };

    /**
     * Method to read a sector from a smart card.
     * Works for Mifare1k, Mifare4k (untested!)
     * Card must already be authenticated with for this method to succeed.
     * @param card The card to read from.
     * @param sector The sector number to read.
     * @return A byte array containing the response, or an empty byte array if the read operation failed.
     */
     static byte[] readSector(Card card, byte sector) {
        byte[] apduNew = apduReadSector;
        apduNew[3] = sector;
        ResponseAPDU response = null;
        try {
            response = card.getBasicChannel().transmit(new CommandAPDU(apduNew));
        } catch (CardException ce) {
            System.out.println(String.format("Failed to read sector %d from the card.", sector));
            ce.printStackTrace();
        }

        if(response == null) { return new byte[0]; }
        else {
            return response.getBytes();
        }
    }

    /**
     * Method to read pages from an NFC tag.
     * Works with MifareUltralight.
     * @param card The tag to read.
     * @param startPage The page to read from.
     * @param n The number of pages to read.
     * @return A byte array containing the response, or an empty byte array if the operation failed.
     */
     static byte[] readPages(Card card, byte startPage, byte n) {
        byte[] apduNew = apduReadSector;

        // 0x03 in APDU is the start address, 0x04 is the number of bytes to read
        apduNew[3] = startPage;
        apduNew[4] = n;

        ResponseAPDU response = null;

        try {
            response = card.getBasicChannel().transmit(new CommandAPDU(apduNew));
        } catch (CardException ce) {
            System.out.println(String.format("Failed to read %d bytes from %d from the card.", n, startPage));
            ce.printStackTrace();
        }

        if(response == null) return new byte[]{};
        else return response.getBytes();
    }

    /**
     * Method to write data to a block on a smart card.
     * Works with Mifare1k and Mifare4k (untested!)
     * Card must already be authenticated with for this method to succeed.
     * The length of the data must be 16 bytes or less, and non-zero, for this method to succeed.
     * @param card The card to write to.
     * @param sector The sector to write to.
     * @param data The data to write.
     * @return A boolean corresponding to the status of the operation (succeeded/failed).
     */
     static boolean writeSector(Card card, byte sector, byte[] data) {
        if(data.length > 16 || data.length == 0) return false;

        byte[] apduNew = new byte[apduWriteSectorPartial.length + 16];
        int i = 0;
        for (; i < apduWriteSectorPartial.length; i++) {
            apduNew[i] = apduWriteSectorPartial[i];
        }

        for(; i < data.length + apduWriteSectorPartial.length; i++) {
            apduNew[i] = data[i - (apduWriteSectorPartial.length)];
        }

        for(; i < apduNew.length; i++) {
            apduNew[i] = (byte) 0x00;
        }

        apduNew[3] = sector;
        apduNew[4] = 0x10;

        ResponseAPDU response = null;

        try {
            response = card.getBasicChannel().transmit(new CommandAPDU(apduNew));
        } catch (CardException ce) {
            System.out.println("Failed to write to sector %d of the card.");
            ce.printStackTrace();
        }

        return response != null && isSuccess(response.getBytes());
    }

    /**
     * Method to write pages of data to a NFC tag.
     * Works on MifareUltralight.
     * DO NOT CALL THIS METHOD ON A SMART CARD. THERE BE DRAGONS AHEAD.
     * Data is automatically padded out and split into nPages pages.
     * @param card The tag to write data to.
     * @param startPage The starting page.
     * @param nPages The number of pages to write.
     * @param data The data to write.
     * @return A boolean corresponding to the status of the operation (succeeded/failed).
     */
    static boolean writePages(Card card, byte startPage, byte nPages, byte[] data) {
        // some basic sanity checking
        if(data.length / 4 > nPages) {
            System.err.println("Data for writePages() call must be able to fit within (nPages * 4) bytes.");
            return false;
        }

        // pad the array to 4-byte pages
        byte[] padded_data = new byte[data.length + (data.length % 4)];

        // add all data to the new array
        int i = 0;
        for (; i < data.length; i++) {
            padded_data[i] = data[i];
        }

        // pad with zeros
        for(; i < padded_data.length; i++) {
            padded_data[i] = (byte) 0x00;
        }

        ResponseAPDU response = null;

        byte currentPage = startPage;
        i = 0;
        while (currentPage < startPage + nPages) {
            byte[] apduNew = new byte[apduWriteSectorPartial.length + 5];

            for(int k = 0; k < apduWriteSectorPartial.length; k++) {
                apduNew[k] = apduWriteSectorPartial[k];
            }

            apduNew[3] = currentPage;
            apduNew[4] = (byte) 0x04;

            for(int j = 5; j < 9; j++) {
                apduNew[j] = padded_data[i];
                i++;
            }

            try {
                response = card.getBasicChannel().transmit(new CommandAPDU(apduNew));
            } catch (CardException e) {
                System.out.println(String.format("Failed to write page %d to the card.", currentPage));
                e.printStackTrace();
            }

            if(!isSuccess(response.getBytes())) {
                System.out.println(String.format("Failed to write page %d to the card.", currentPage));
                return false;
            }

            currentPage++;
        }
        return true;
    }

    /**
     * Method to toggle the buzzer on the ACS ACR122U reader.
     * Works with any card on the reader.
     * @param card The card on the reader which should have its buzzer disabled.
     * @param state The state to which the buzzer should be set (true = on, false = off).
     * @return A boolean corresponding to the status of the operation (succeeded/failed).
     */
    static boolean toggleBuzzer(Card card, boolean state) {
        ResponseAPDU response = null;

        try {
            response = card.getBasicChannel().transmit(new CommandAPDU((state) ? apduEnableBeep : apduDisableBeep));
        } catch (CardException ce) {
            System.out.println("Failed to toggle the beep.");
            ce.printStackTrace();
        }

        return response != null && isSuccess(response.getBytes());
    }

    /**
     * Method to detect a valid ACS ACR122 card reader.
     * @return The first ACR122 detected on the system, or null if none is detected.
     */
    static CardTerminal detectAcrReader() {
        CardTerminal correctTerminal = null;

        // Get a list of terminals.
        TerminalFactory factory = TerminalFactory.getDefault();
        try {
            for (CardTerminal term : factory.terminals().list()) {
                if(term.getName().contains("ACS ACR122")) {
                    correctTerminal = term;
                    break;
                }
            }
        } catch (CardException ce) {
            System.out.println("Failed to interface with the card terminal.");
            ce.printStackTrace();
        }

        return correctTerminal;
    }

    /**
     * Method to wait for a card on a given terminal.
     * @param terminal The terminal on which to wait.
     */
    static void waitForCardOn(CardTerminal terminal) {
        try {
            terminal.waitForCardPresent(0);
        } catch (CardException ce) {
            System.out.println("Failed to communicate with the smartcard reader.");
            ce.printStackTrace();
        }
    }

    /**
     * Method to get the card on a terminal.
     * @param terminal The terminal to read the card from.
     * @return The card on the terminal, or null if none is detected.
     */
    static Card getCardOn(CardTerminal terminal) {
        Card temp = null;
        try {
            temp = terminal.connect("*");
        } catch (CardException ce) {
            System.out.println("Failed to communicate with the smartcard reader.");
            ce.printStackTrace();
        }
        return temp;
    }

    /**
     * Method to wait for a card on a given terminal.
     * @param terminal The terminal on which to wait.
     * @param timeout The time, in milliseconds, to wait until giving up.
     */
    static void waitForCardOn(CardTerminal terminal, long timeout) {
        try {
            terminal.waitForCardPresent(timeout);
        } catch (CardException ce) {
            System.out.println("Failed to communicate with the smartcard reader.");
            ce.printStackTrace();
        }
    }

    /**
     * Method to wait for a card to be removed from a given terminal.
     * @param terminal The card to remove.
     */
    static void waitForCardRemovalOn(CardTerminal terminal) {
        try {
            terminal.waitForCardAbsent(0);
        } catch (CardException ce) {
            System.out.println("Failed to communicate with the smartcard reader.");
            ce.printStackTrace();
        }
    }

    /**
     * Method to check if a card is actually a Mifare card.
     * @param atr The ATR data of the card to test.
     * @return A boolean corresponding to whether the card is a Mifare card or not.
     */
    static boolean isValidMifareCard(byte[] atr) {
        boolean isMifareCard = true;

        try {
            if (atr.length < 16) {
                isMifareCard = false;
            }

            if(atr[13] != 0x0) {
                isMifareCard = false;
            }

            if(!MifareTypes.types.contains(atr[14])) {
                isMifareCard = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }

        return isMifareCard;
    }

    /**
     * Method to get the type of Mifare card for a given ATR.
     * @param atr The ATR data of the card to test.
     * @return The ATR card type byte, or 0x00 if the card is not a valid Mifare card.
     */
    static byte getMifareType(byte[] atr) {
        if(isValidMifareCard(atr)) return atr[14];
        else return 0x00;
    }

    /**
     * Method to authenticate with a smart card.
     * Works on Mifare1k, Mifare4k (untested)
     * Will fail on NFC tags, as authentication is not required for tags.
     * This method must be called before attempting to read/write data on the card.
     * @param card The card to authenticate with.
     * @return A boolean corresponding to the status of the operation (succeeded/failed).
     */
    static boolean authenticate(Card card) {
        ResponseAPDU response = null;
        try {
            response = card.getBasicChannel().transmit(new CommandAPDU(apduGetKey));
            if (!isSuccess(response.getBytes())) return false;
            response = card.getBasicChannel().transmit(new CommandAPDU(apduAuthenticate));
        } catch (CardException ce) {
            System.out.println("Failed to authenticate with the card.");
            ce.printStackTrace();
        }
        return response != null && isSuccess(response.getBytes());
    }

    /**
     * Method to test the last 2 bytes of a response to check if it succeeded.
     * @param response The response to test.
     * @return A boolean corresponding to whether the response indicates a successful operation.
     */
    static boolean isSuccess(byte[] response) {
        return (response[response.length - 1] == 0x00 && response[response.length - 2] == (byte) 0x90);
    }

    /**
     * Method to test if a card is actually a NFC tag.
     * @param card The card to test.
     * @return A boolean corresponding to whether a card is a tag or not.
     */
    static boolean isNFCTag(Card card) { return (MifareUtils.getMifareType(card.getATR().getBytes()) == (byte) 0x03); }

    static byte[] chopStatusBytes(byte[] response) {
        byte[] retval = new byte[response.length - 2];

        for (int i = 0; i < retval.length; i++) {
            retval[i] = response[i];
        }

        return retval;
    }
}
