package me.monotron.NFCReader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Toby on 24/07/2017.
 */

enum LogLevels {
    INFO, WARNING, ERROR, FATAL
}

public class LogUtils {

    /**
     * Method to get the log level as a string.
     * @param logLevel The log level to stringify.
     * @return The stringified log level.
     */
    private static String getLogLevel(LogLevels logLevel) {
        switch (logLevel) {
            case INFO:
                return "INFO";
            case WARNING:
                return "WARNING";
            case ERROR:
                return "ERROR";
            case FATAL:
                return "FATAL";
            default:
                return "";
        }
    }

    /**
     * Log a message to the console.
     * @param msg The message to log.
     */
    static void log(String msg) {
        System.out.println(String.format("[%s] %s", new SimpleDateFormat("d/M/y HH:mm:ss").format(new Date()), msg));
    }

    /**
     * Log a message to the console.
     * @param msg The message to log.
     * @param logLevel The log level.
     */
    static void log(String msg, LogLevels logLevel) {
        System.out.println(String.format("[%s] <%s> %s", new SimpleDateFormat("d/M/y HH:mm:ss").format(new Date()),
                getLogLevel(logLevel), msg));
    }

}
