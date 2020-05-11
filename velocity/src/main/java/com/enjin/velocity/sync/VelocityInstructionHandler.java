package com.enjin.velocity.sync;

import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.EnjinConfig;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.text.TextComponent;

import java.io.File;
import java.util.List;

public class VelocityInstructionHandler implements InstructionHandler {

    @Inject
    private ProxyServer proxyServer;

    @Override
    public void addToWhitelist(String player) {

    }

    @Override
    public void removeFromWhitelist(String player) {

    }

    @Override
    public void ban(String player) {

    }

    @Override
    public void pardon(String player) {

    }

    @Override
    public void addToGroup(String player, String group, String world) {

    }

    @Override
    public void removeFromGroup(String player, String group, String world) {

    }

    @Override
    public void execute(Long id, String command, Optional<Long> delay, Optional<Boolean> requireOnline, Optional<String> name, Optional<String> uuid) {

    }

    @Override
    public void commandConfirmed(List<Long> executed) {

    }

    @Override
    public void configUpdated(Object update) {
        EnjinConfig config = Enjin.getConfiguration();
        if(config != null) {
            config.update(new File(EnjinMinecraftPlugin.getInstance().getConfigDir(), "config.json"), update);
        }
    }

    @Override
    public void statusReceived(String status) {
        Enjin.getLogger().debug("Enjin Status: " + status);
    }

    @Override
    public void clearInGameCache(String player, int id, String price) {

    }

    @Override
    public void notify(List<String> players, String message, long time) {
        for (String player : players) {
            Player p = proxyServer.getPlayer(player).get();
            if(p != null) {
                p.sendMessage(TextComponent.of(message));
            }
        }
    }

    @Override
    public void version(String version) {

    }
}
