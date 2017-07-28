package me.monotron.NFCReader;
import java.io.*;
/**
 * Created by Tavis on 28/07/2017.
 * I made a thing!
 */

public class GraceUtils {

    static void graceChrome() {
        try {
            String homeDir = System.getProperty("user.home");
            File file = new File(homeDir + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Preferences");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", oldtext = "";
            while ((line = reader.readLine()) != null) {
                oldtext += line + "\r\n";
            }
            reader.close();
            String graceCleanup = oldtext.replaceAll("\"exit_type\":\"Crashed\",\"exited_cleanly\":true", "\"exit_type\":\"None\",\"exited_cleanly\":true");
            FileWriter writer = new FileWriter(homeDir + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Preferences");
            writer.write(graceCleanup);
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
