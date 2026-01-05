package me.imduxg.mythicitemsync.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class SyncLogger {

    private static File logFile;
    private static boolean enabled = true;

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private SyncLogger() {}

    public static void init(File dataFolder, boolean enabledFlag) {
        enabled = enabledFlag;
        if (!enabled) return;

        try {
            File dir = new File(dataFolder, "logs");
            if (!dir.exists()) dir.mkdirs();

            logFile = new File(dir, "sync.log");
            if (!logFile.exists()) logFile.createNewFile();
        } catch (IOException ignored) {
            // no console log
        }
    }

    public static synchronized void log(String type, String message) {
        if (!enabled || logFile == null) return;

        String time = FORMAT.format(new Date());
        String line = "[" + time + "] [" + type + "] " + message;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException ignored) {
            // no console log
        }
    }
}
