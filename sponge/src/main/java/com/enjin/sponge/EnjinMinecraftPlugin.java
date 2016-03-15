package com.enjin.sponge;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.command.CommandBank;
import com.enjin.sponge.command.commands.BuyCommand;
import com.enjin.sponge.command.commands.CoreCommands;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.shop.ShopListener;
import com.enjin.sponge.sync.RPCPacketManager;
import com.enjin.sponge.tasks.TPSMonitor;
import com.enjin.sponge.utils.Log;
import com.enjin.sponge.utils.commands.CommandWrapper;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(id = "enjinminecraftplugin", name = "Enjin Minecraft Plugin", description = "Enjin Minecraft Plugin for Sponge", version = "2.8.3-sponge")
public class EnjinMinecraftPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private static List<CommandSpec> commands = Lists.newArrayList();
    @Getter
    private static List<CommandWrapper> processedCommands = Lists.newArrayList();

    @Inject
    @Getter
    private PluginContainer container;
    @Inject
    @Getter
    private Logger logger;
    @Inject
    @Getter
    private java.util.logging.Logger javaLogger;
    @Inject
    @ConfigDir(sharedRoot = false)
    @Getter
    private File configDir;
    @Inject
    @Getter
    private Game game;

    @Getter
    private Task syncTask;

    @Getter
    private boolean firstRun = true;
    @Getter @Setter
    private boolean authKeyInvalid = false;

    public EnjinMinecraftPlugin() {
        instance = this;
        Enjin.setPlugin(this);
    }

    @Listener
    public void initialization(GameInitializationEvent event) {
        init();
    }

    public void init() {
        if (authKeyInvalid) {
            return;
        }

        if (firstRun) {
            firstRun = false;
            initConfig();

            Enjin.setLogger(new Log(configDir));
            debug("Init config done.");

            initCommands();
            debug("Init commands done.");

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(Optional.absent(), getPort(), true);
                if (data == null) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. Data could not be retrieved.");
                    return;
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. " + data.getError().getMessage());
                    return;
                } else if (!data.getResult()) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. Failed to authenticate.");
                    return;
                }
            } else {
                authKeyInvalid = true;
                debug("Auth key is invalid. Must be 50 characters in length.");
                return;
            }
        }

        //menuAPI = new MenuAPI(this);
        //debug("Init gui api done.");
        //initManagers();
        //debug("Init managers done.");
        //initPlugins();
        //debug("Init plugins done.");
        //initPermissions();
        //debug("Init permissions done.");
        initListeners();
        debug("Init listeners done.");
        initTasks();
        debug("Init tasks done.");
    }

    private void initConfig() {
        logger.info("Initializing EMP Config");
        EMPConfig config = JsonConfig.load(new File(configDir, "config.json"), EMPConfig.class);
        Enjin.setConfiguration(config);
    }

    private void initCommands() {
        logger.info("Initializing EMP Commands");
        CommandBank.setup(this);

        CommandBank.register(BuyCommand.class, CoreCommands.class);
    }

    private void initListeners() {
        game.getEventManager().registerListeners(this, new ShopListener());
    }

    public void initTasks() {
        if (syncTask != null) {
            stopTasks();
        }

        syncTask = game.getScheduler().createTaskBuilder()
                .execute(new RPCPacketManager(this))
                .async().interval(60, TimeUnit.SECONDS)
                .submit(this);

		game.getScheduler().createTaskBuilder()
				.execute(new TPSMonitor())
				.async().interval(2, TimeUnit.SECONDS)
				.submit(this);
    }

    public void stopTasks() {
        syncTask.cancel();
        syncTask = null;
    }

    public Integer getPort() {
        return game.getServer().getBoundAddress().get().getPort();
    }

    @Override
    public InstructionHandler getInstructionHandler() {
        return null;
    }

    @Override
    public void debug(String s) {
        Enjin.getLogger().debug(s);
    }

    public static void saveConfiguration() {
        Enjin.getConfiguration().save(new File(instance.configDir, "config.json"));
    }
}
