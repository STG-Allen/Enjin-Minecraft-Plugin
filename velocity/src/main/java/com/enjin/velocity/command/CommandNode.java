package com.enjin.velocity.command;

import com.enjin.core.Enjin;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandNode implements Command {

    @Getter
    private com.enjin.velocity.command.Command data;

    @Getter
    private Permission permission;
    private Method method;

    @Getter
    private Map<String, DirectiveNode> directives = new HashMap<>();

    public CommandNode(com.enjin.velocity.command.Command data, Method method) {
        this.data = data;
        this.method = method;
    }

    public CommandNode(com.enjin.velocity.command.Command data, Method method,
                       Permission permission) {
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

        if (args.length > 0) {
            DirectiveNode directiveNode = directives.get(args[0]);
            if (directiveNode != null) {
                directiveNode.invoke(source, args.length > 1 ? Arrays.copyOfRange(args, 1,
                    args.length) : new String[]{});
                return;
            }
        }

        try {
            if (method.getParameterTypes()[0] == Player.class && !(source instanceof Player)) {
                source.sendMessage(TextComponent.of("This command may only be used in-game by a " +
                    "player.").color(TextColor.RED));
                return;
            }

            if (method.getParameterTypes()[0] == ConsoleCommandSource.class
                && !(source instanceof ConsoleCommandSource)) {
                source.sendMessage(TextComponent.of("This command may only be used by console.")
                    .color(TextColor.RED));
                return;
            }

            if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid() && data.requireValidKey()) {
                source.sendMessage(TextComponent.of("This commands requires the server to " +
                    "successfully be authenticated with Enjin.").color(TextColor.RED));
                return;
            }

            Enjin.getLogger().debug("Executing command: " + data.value());
            method.invoke(null, source, args);
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
    }

    @Override
    public void execute(CommandSource source, @NonNull String[] args) {
        invoke(source, args);
    }
}
