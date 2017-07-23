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

    public static void log(String msg) {
        System.out.println(String.format("[%s] %s", new SimpleDateFormat("d/M/y HH:mm:ss").format(new Date()), msg));
    }

    public static void log(String msg, LogLevels logLevel) {
        System.out.println(String.format("[%s] <%s> %s", new SimpleDateFormat("d/M/y HH:mm:ss").format(new Date()),
                getLogLevel(logLevel), msg));
    }

}
