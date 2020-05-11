package com.enjin.velocity.utils;

import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.enjin.velocity.utils.io.EnjinLogFormatter;
import lombok.Getter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log implements EnjinLogger {

    private static final SimpleDateFormat LOG_ZIP_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Getter
    private final static Logger logger = Logger.getLogger(EnjinMinecraftPlugin.class.getSimpleName());
    private File logs = null;
    private File log = null;

    public Log(File configDir) {
        logs = new File(configDir, "logs");
        log = new File(logs, "enjin.log");

        try {
            if (log.exists()) {
                zipAndReplaceExistingLog();
            } else {
                logs.mkdirs();
                log.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zipAndReplaceExistingLog() {
        FileInputStream fileInputStream = null;

        try {
            String date = LOG_ZIP_NAME_FORMAT.format(Calendar.getInstance().getTime());
            int i = 0;
            File file = null;
            while (file == null || file.exists()) {
                file = new File(logs, date + "-" + ++i + ".log.zip");
            }

            net.lingala.zip4j.core.ZipFile zipFile = new ZipFile(file);
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setFileNameInZip(date + "-" + i + ".log");
            zipParameters.setSourceExternalStream(true);
            zipFile.addStream((fileInputStream = new FileInputStream(log)), zipParameters);
        } catch (IOException | ZipException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            log.delete();
            log.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void info(String msg) {
        logger.info(hideSensitiveText(msg));
    }

    @Override
    public void warning(String msg) {
        logger.warning(hideSensitiveText(msg));
    }

    @Override
    public void debug(String msg) {
        if (Enjin.getConfiguration() != null && Enjin.getConfiguration().isDebug()) {
            logger.info("[DEBUG] " + hideSensitiveText(msg));
        }
    }

    @Override
    public void log(String msg, Throwable t) {
        logger.log(Level.SEVERE, hideSensitiveText(msg), t);
    }

    @Override
    public void log(Throwable t) {
        log("Exception Caught: ", t);
    }

    @Override
    public String getLastLine() {
        return "";
    }

    @Override
    public void setDebug(boolean debug) {
        //Assuming this is correct although it is not defined in the other "Log" classes.
        Enjin.getConfiguration().setDebug(debug);
    }

    @Override
    public File getLogDirectory() {
        return logs;
    }

    @Override
    public File getLogFile() {
        return log;
    }

    private String hideSensitiveText(String msg) {
        if(Enjin.getConfiguration().getAuthKey().equals("")) {
            return msg;
        }
        return msg.replaceAll(Enjin.getConfiguration().getAuthKey(),
            "**************************************************");
    }

    public void configure() {
        if (Enjin.getConfiguration().isLoggingEnabled()) {
            EnjinLogFormatter formatter = new EnjinLogFormatter();
            FileHandler handler = null;

            try {
                handler = new FileHandler(
                    EnjinMinecraftPlugin.getInstance().getConfigDir()
                        .getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log",
                    true);

            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.setFormatter(formatter);
            logger.addHandler(handler);
        }
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINEST);
    }
}
