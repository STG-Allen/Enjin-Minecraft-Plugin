package com.enjin.velocity.sync;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Instruction;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;
import com.enjin.rpc.mappings.mappings.proxy.NodeState;
import com.enjin.rpc.mappings.services.VelocityService;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.enjin.velocity.sync.data.NewerVersionInstruction;
import com.enjin.velocity.sync.data.NotificationsInstruction;
import com.enjin.velocity.sync.data.RemoteConfigUpdateInstruction;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RPCPacketManager implements Runnable {


    private EnjinMinecraftPlugin plugin;

    public RPCPacketManager(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        //TODO setPluginVersion to the correct version
        final Status status = new Status(
            System.getProperty("java.version"),
            null,
            getPlugins(),
            null,
            "@version@-bungee",
            null,
            null,
            getMaxPlayers(),
            getOnlineCount(),
            getOnlinePlayers(),
            null,
            null,
            null,
            null,
            null);
        EnjinMinecraftPlugin.getInstance().getProxyServer().getScheduler().buildTask(plugin, new Runnable() {

            @Override
            public void run() {
                VelocityService service = EnjinServices.getService(VelocityService.class);
                RPCData<SyncResponse> data = service.get(status, getServers());

                if (data == null) {
                    Enjin.getLogger().debug("Data is null while requesting sync update from Velocity.get.");
                    return;
                }

                if (data.getError() != null) {
                    Enjin.getLogger().warning(data.getError().getMessage());
                } else {
                    SyncResponse response = data.getResult();
                    if (response != null && response.getStatus().equalsIgnoreCase("ok")) {
                        for (Instruction instruction : response.getInstructions()) {
                            switch (instruction.getCode()) {
                                case CONFIG:
                                    RemoteConfigUpdateInstruction.handle((Map<String, Object>) instruction.getData());
                                    break;
                                case RESPONSE_STATUS:
                                    Enjin.getPlugin()
                                        .getInstructionHandler()
                                        .statusReceived((String) instruction.getData());
                                    break;
                                case NOTIFICATIONS:
                                    NotificationsInstruction.handle((NotificationData) instruction.getData());
                                    break;
                                case PLUGIN_VERSION:
                                    NewerVersionInstruction.handle((String) instruction.getData());
                                    break;
                                default:
                            }
                        }
                    }
                }
            }
        }).repeat(5, TimeUnit.SECONDS).schedule();
    }

    private List<String> getPlugins() {
        List<String> plugins = new ArrayList<>();
        if (plugin.getProxyServer().getPluginManager().getPlugins().isEmpty()) {
            plugins.add("null");
            return plugins;
        }
        plugin.getProxyServer().getPluginManager().getPlugins().forEach(p -> {
            plugins.add(p.getDescription().getName().orElse("invalid-plugin"));
        });
        return plugins;
    }

    private Integer getMaxPlayers() {
        return EnjinMinecraftPlugin.getInstance().getProxyServer().getConfiguration().getShowMaxPlayers();
    }

    private Integer getOnlineCount() {
        return EnjinMinecraftPlugin.getInstance().getProxyServer().getPlayerCount();
    }

    private List<PlayerInfo> getOnlinePlayers() {
        List<PlayerInfo> players = new ArrayList<>();
        plugin.getProxyServer().getAllPlayers().forEach(p ->
            players.add(new PlayerInfo(p.getUsername(), p.getUniqueId()))
        );
        return players;
    }

    private Map<String, NodeState> getServers() {
        final Map<String, NodeState> servers = new ConcurrentHashMap<>();
        final boolean[] alreadyAnnouncedPlayers = {false};

        plugin.getProxyServer().getAllServers().forEach(registeredServer -> {
            final ServerInfo info = registeredServer.getServerInfo();
            if (info == null) {
                return;
            }//Little hack to get the max player count to display properly for the time being
            if (!alreadyAnnouncedPlayers[0]) {
                servers.put(info.getName(),
                    new NodeState(getPlayersFromSever(registeredServer),
                        plugin.getProxyServer().getConfiguration().getShowMaxPlayers()));
                alreadyAnnouncedPlayers[0] = true;
            } else {
                servers.put(info.getName(),
                    new NodeState(getPlayersFromSever(registeredServer), 0));
            }
        });
        return servers;
    }

    private List<String> getPlayersFromSever(RegisteredServer registeredServer) {
        List<String> players = new ArrayList<>();
        registeredServer.getPlayersConnected().forEach(p -> players.add(p.getUsername()));
        return players;
    }

}
