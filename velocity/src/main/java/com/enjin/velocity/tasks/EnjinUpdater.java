package com.enjin.velocity.tasks;

import com.enjin.core.Enjin;
import com.enjin.velocity.EnjinMinecraftPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class EnjinUpdater implements Runnable {
    private String downloadLocation = "";
    private File destination;
    private EnjinMinecraftPlugin plugin;
    private String version;
    private String updateJar = "http://resources.guild-hosting.net/1/downloads/emp/";

    public EnjinUpdater(String downloadLocation, String version, File destination, EnjinMinecraftPlugin plugin) {
        this.downloadLocation = downloadLocation;
        this.version = version;
        this.destination = destination;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        File tempFile = new File(downloadLocation + File.separator + "EnjinMinecraftPlugin.jar.part");
        try {
            Enjin.getLogger().debug("Connecting to url " + updateJar + version + "/EnjinMinecraftPlugin.jar");
            URL website = new URL(updateJar + version + "/EnjinMinecraftPlugin.jar");

            ReadableByteChannel readableByteChannel = Channels.newChannel(website.openStream());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.getChannel().transferFrom(readableByteChannel, 0, 1 << 24);
            outputStream.close();

            if (destination.delete() && tempFile.renameTo(destination)) {
                plugin.setHasUpdate(true);
                plugin.setNewVersion(version);
                plugin.setUpdateFailed(false);
                Enjin.getLogger().info("Enjin Minecraft Plugin was updated to version " + version + ". Please restart your server.");
                return;
            } else {
                plugin.setUpdateFailed(true);
                Enjin.getLogger().warning("Unable to update to new version. Please update manually!");
            }
        } catch (IOException e) {
            Enjin.getLogger().log(e);
        }
        plugin.setHasUpdate(false);
    }
}
