package com.enjin.velocity;

import com.enjin.common.config.GenericEnjinConfig;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.velocity.command.CommandBank;
import com.enjin.velocity.command.commands.CoreCommands;
import com.enjin.velocity.command.commands.PointCommands;
import com.enjin.velocity.sync.RPCPacketManager;
import com.enjin.velocity.sync.VelocityInstructionHandler;
import com.enjin.velocity.utils.Log;
import com.enjin.velocity.utils.io.EnjinErrorReport;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Plugin(
    id = "enjin-minecraft-plugin",
    authors = {"STG_Allen"},
    version = "3.0.1",
    description = "Enjin Minecraft Plugin for Velocity",
    name = "Enjin Minecraft Plugin"
)
public class EnjinMinecraftPlugin implements EnjinPlugin {

    @Getter
    private static EnjinMinecraftPlugin instance;

    @Getter
    private InstructionHandler instructionHandler = new VelocityInstructionHandler();
    @Getter
    private boolean firstRun = true;

    @Getter
    private File configDir = Paths.get("plugins/enjin-velocity").toFile();

    @Getter
    @Setter
    private boolean unableToContactEnjin = false;
    @Getter
    @Setter
    private boolean authKeyInvalid = false;
    @Getter
    @Setter
    private EnjinErrorReport lastError = null;
    @Getter
    @Setter
    private String newVersion = "";
    @Getter
    @Setter
    private boolean hasUpdate = false;
    @Getter
    @Setter
    private boolean updateFailed = false;

    @Getter
    private long serverId = -1;

    @Inject
    private ProxyServer proxyServer;

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent e) {
        instance = this;
        Enjin.setPlugin(instance);
        init();
    }

    public void init() {
        if (authKeyInvalid) {
            return;
        }

        if (firstRun) {
            Log log = new Log(configDir);
            Enjin.setLogger(log);

            firstRun = false;
            initConfig();
            log.configure();
            Enjin.getLogger().debug("Init config done.");
            //initCommands
            initCommands();
            Enjin.getLogger().debug("Init commands done.");

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                Optional<Integer> port = getPort();
                RPCData<Auth> data = EnjinServices.getService(PluginService.class)
                    .auth(com.google.common.base.Optional.<String>absent(),
                        port.isPresent() ? port.get() : null,
                        true,
                        true);
                if (data == null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Data could not be retrieved.");
                    return;
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. " + data.getError().getMessage());
                    return;
                } else if (data.getRequest() == null || !data.getResult().isAuthed()) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Failed to authenticate.");
                    return;
                }

                Auth auth = data.getResult();
                if (auth.getServerId() > 0) {
                    serverId = auth.getServerId();
                }
            } else {
                authKeyInvalid = true;
                Enjin.getLogger().debug("Auth key is invalid. Must be 50 characters in length.");
                return;
            }
        }
        initTasks();
        Enjin.getLogger().debug("Init tasks done.");
    }

    public void initConfig() {
        try {
            File configFile = new File(configDir, "config.json");
            GenericEnjinConfig genericConfig = JsonConfig.load(configFile, GenericEnjinConfig.class);
            Enjin.setConfiguration(genericConfig);

            if (genericConfig.getSyncDelay() < 0) {
                genericConfig.setSyncDelay(0);
            } else if (genericConfig.getSyncDelay() > 10) {
                genericConfig.setSyncDelay(10);
            }

            genericConfig.save(configFile);
        } catch (Exception e) {
            Enjin.getLogger().warning("Error occured while initializing Enjin configuration");
            Enjin.getLogger().log(e);
        }
    }

    public static void saveConfiguration() {
        Enjin.getConfiguration().save(new File(instance.getConfigDir(), "config.json"));
    }

    public void initCommands() {
        //Don't need to register it as a listener as velocity doesn't require a listener for
        // commands.
        CommandBank.register(CoreCommands.class, PointCommands.class);
    }

    public void initTasks() {
        Enjin.getLogger().debug("Starting tasks.");
        proxyServer.getScheduler().buildTask(this, new RPCPacketManager(this)).repeat(60L, TimeUnit.SECONDS).schedule();
    }

    public void disableTasks() {
        //There is no "disable tasks" in velocity
    }

    public Optional<Integer> getPort() {
        return Optional.of(proxyServer.getBoundAddress().getPort());
    }

}
