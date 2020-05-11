package com.enjin.velocity.tasks;

import com.enjin.core.Enjin;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.util.ConnectionUtil;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.enjin.velocity.utils.io.ReverseFileReader;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class ReportPublisher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private StringBuilder stringBuilder;
    private CommandSource commandSource;

    public ReportPublisher(EnjinMinecraftPlugin plugin, StringBuilder stringBuilder, CommandSource commandSource) {
        this.plugin = plugin;
        this.stringBuilder = stringBuilder;
        this.commandSource = commandSource;
    }

    @Override
    public void run() {
        EnjinConfig config = Enjin.getConfiguration();
        stringBuilder.append("\nLast Severe error message: \n");
        File serverLogLocation = plugin.getConfigDir().getAbsoluteFile().getParentFile().getParentFile();
        try {
            File logFile = new File(serverLogLocation.getAbsoluteFile() + File.separator + "proxy.log.0");
            ReverseFileReader reverseFileReader = new ReverseFileReader(logFile.getAbsolutePath());
            LinkedList<String> errorMessages = new LinkedList<>();
            String line = "";
            boolean errorFound = false;

            while ((line = reverseFileReader.readLine()) != null && !errorFound) {
                if (errorMessages.size() >= 40) {
                    errorMessages.removeFirst();
                }
                errorMessages.add(line);

                if (line.contains("[SEVERE]") || line.contains("[ERROR]")) {
                    boolean severeEnded = false;
                    while ((line = reverseFileReader.readLine()) != null && !severeEnded) {
                        if (line.contains("[SEVERE]") || line.contains("[ERROR]")) {
                            if (errorMessages.size() >= 40) {
                                errorMessages.removeFirst();
                            }
                            errorMessages.add(line);
                        } else {
                            severeEnded = true;
                        }
                    }
                    for (int i = errorMessages.size(); i > 0; i--) {
                        stringBuilder.append(errorMessages.get(i - 1)).append("\n");
                    }
                    errorFound = true;
                }
            }
            reverseFileReader.close();
        } catch (Exception e) {
            if (config.isDebug()) {
                Enjin.getLogger().log(e);
            }
        }
        try {
            ReverseFileReader reverseFileReader = new ReverseFileReader(
                plugin.getConfigDir().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log");
            stringBuilder.append("\nLast 100 lines of enjin.log: \n");
            LinkedList<String> enjinLog = new LinkedList<>();
            String line = "";
            for (int i = 0; i < 100 && (line = reverseFileReader.readLine()) != null; i++) {
                enjinLog.add(line);
            }
            for (int i = enjinLog.size(); i > 0; i--) {
                stringBuilder.append(enjinLog.get(i - 1)).append("\n");
            }
            reverseFileReader.close();
        } catch (Exception ignored) {
        }
        if (plugin.getLastError() != null) {
            stringBuilder.append("\nLast Enjin Plugin Severe error message: \n");
            stringBuilder.append(plugin.getLastError().toString());
        }
        stringBuilder.append("\n=========================================\nEnjin HTTPS test: ")
            .append(ConnectionUtil.testHTTPSconnection() ? "passed" : "FAILED!")
            .append("\n");
        stringBuilder.append("Enjin HTTP test: ")
            .append(ConnectionUtil.testHTTPconnection() ? "passed" : "FAILED!")
            .append("\n");
        stringBuilder.append("Enjin web connectivity test: ")
            .append(ConnectionUtil.testWebConnection() ? "passed" : "FAILED!")
            .append("\n");
        stringBuilder.append("Is mineshafter present: ")
            .append(ConnectionUtil.isMineshafterPresent() ? "yes" : "no")
            .append("\n=========================================\n");

        String report = stringBuilder.toString().replaceAll(config.getApiUrl(),
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        Date date = new Date();
        InputStream inputStream = null;

        try {
            inputStream = new ByteArrayInputStream(report.getBytes());
            ZipFile zipFile = new ZipFile(new File("enjinreport_" + dateFormat.format(date) + ".zip"));
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            zipFile.addFile(Enjin.getLogger().getLogFile(), zipParameters);
            zipParameters.setFileNameInZip("enjinreport_" + dateFormat.format(date) + ".txt");
            zipParameters.setSourceExternalStream(true);
            zipFile.addStream(inputStream, zipParameters);
            commandSource.sendMessage(TextComponent.of("Enjin report created in " + zipFile.getFile().getPath() + " successfully!").color(TextColor.GOLD));
        } catch (ZipException e) {
            commandSource.sendMessage(TextComponent.of("Unable to write enjin report!").color(TextColor.DARK_RED));
            Enjin.getLogger().log(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Enjin.getLogger().log(e);
            }
        }
    }
}
