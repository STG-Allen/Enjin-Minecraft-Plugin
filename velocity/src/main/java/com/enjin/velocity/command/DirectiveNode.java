package com.enjin.velocity.command;

import com.enjin.core.Enjin;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

import java.lang.reflect.Method;

public class DirectiveNode {

    @Getter
    private Directive data;

    @Getter
    private Permission permission;

    private Method method;

    public DirectiveNode(Directive data, Method method) {
        this.data = data;
        this.method = method;
    }

    public DirectiveNode(Directive data, Method method, Permission permission) {
        this(data, method);
        this.permission = permission;
    }

    public void invoke(CommandSource source, String[] args) {
        if (method == null) {
            return;
        }

        if (source instanceof Player
            && permission != null
            && !permission.value().equals("")
            && !source.hasPermission(permission.value())) {
            source.sendMessage(TextComponent.of("You need to have the \"").color(TextColor.RED)
                .append(TextComponent.of(permission.value()).color(TextColor.GOLD))
                .append(TextComponent.of("\" permission to run that directive.")
                    .color(TextColor.RED)));
            return;
        }

        try {
            if (method.getParameterTypes()[0] == Player.class && !(source instanceof Player)) {
                source.sendMessage(TextComponent.of("This directive can only be used in-game by " +
                    "a player.").color(TextColor.RED));
                return;
            }

            if (method.getParameterTypes()[0] == ConsoleCommandSource.class
                && !(source instanceof ConsoleCommandSource)) {
                source.sendMessage(TextComponent.of("This directive can only be used by the " +
                    "console.").color(TextColor.RED));
                return;
            }

            if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid() && data.requireValidKey()) {
                source.sendMessage(TextComponent.of("This directive requires the server to " +
                    "successfully be authenticated with Enjin."));
                return;
            }

            Enjin.getLogger().debug("Executing directive: " + data.parent() + "-" + data.value());
            method.invoke(null, source, args);
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
    }
}
