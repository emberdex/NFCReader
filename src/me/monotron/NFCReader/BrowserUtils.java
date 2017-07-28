package me.monotron.NFCReader;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by Toby on 24/07/2017.
 */
public class BrowserUtils {

    private static Process p;

    /**
     * Method to launch an ID in the browser.
     * @param id The ID of the roundme instance to launch.
     * @param kiosk Whether to launch in kiosk mode or not.
     */
    public static void launchBrowser(int id, boolean kiosk) {
        try {
            GraceUtils.graceChrome();
            // Launch Chrome from the program files
            p = Runtime.getRuntime().exec(String.format("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe %s %s",
                    (kiosk) ? "--kiosk" : "", String.format(CardUtils.ROUNDME_URL, id)));
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(GUIUtils.window, "Failed to launch the browser." +
                    "See the console for additional information.", "Error", JOptionPane.ERROR_MESSAGE);
            ioe.printStackTrace();
        }
    }

    /**
     * Method to kill the browser. Called when the card is removed.
     */
    public static void killBrowser() {
        p.destroy();
        GraceUtils.graceChrome();
    }
}
