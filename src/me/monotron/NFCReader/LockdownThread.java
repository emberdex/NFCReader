package me.monotron.NFCReader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Toby on 11/07/2017.
 */
public class LockdownThread implements Runnable {
    public ArrayList<String> list;

    LockdownThread(ArrayList<String> processes) {
        this.list = processes;
    }

    /**
     * A thread to continually kill a specified list of processes.
     */
    public void run() {
        while(true) {
            for (String s : list) {
                try {
                    Runtime.getRuntime().exec(String.format("taskkill /f /im %s", s));
                } catch (IOException ioe) {
                    // whatever
                }
            }
            // SO MY SHIT DOESN'T CRASH THE ENTIRE SYSTEM
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                continue;
            }
        }
    }
}
