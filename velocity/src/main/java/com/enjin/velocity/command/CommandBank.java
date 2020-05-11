package com.enjin.velocity.command;

import com.enjin.core.Enjin;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.google.common.base.Optional;
import com.velocitypowered.api.command.CommandSource;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class CommandBank {
    @Getter
    private static Map<String, CommandNode> nodes = new HashMap<>();

    public static void register(Class<?>... handles) {
        for (Class<?> clazz : handles) {
            Enjin.getLogger().debug("Registering commands and directives for " + clazz.getSimpleName());
            List<Method> methods = new ArrayList<>();

            for (Method method : clazz.getMethods()) {
                if (!(method.isAnnotationPresent(Command.class) || method.isAnnotationPresent(Directive.class))) {
                    continue;
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    Enjin.getLogger().debug(method.getName() + " is not static.");
                    continue;
                }

                if (method.getParameterTypes().length != 2) {
                    Enjin.getLogger().debug(method.getName() + " does not have 2 parameters.");
                    continue;
                }

                Class<?>[] types = method.getParameterTypes();
                if (!CommandSource.class.isAssignableFrom(types[0])) {
                    Enjin.getLogger().debug(method.getName() + "'s first argument is not " +
                        "assignable from CommandSource.");
                    continue;
                }

                if (!String[].class.isAssignableFrom(types[1])) {
                    Enjin.getLogger().debug(method.getName() + "'s second argument is not " +
                        "assignable from String[].");
                    continue;
                }

                methods.add(method);
            }

            List<CommandNode> root = new ArrayList<>();
            List<DirectiveNode> sub = new ArrayList<>();

            for (Method method : methods) {
                if (method.isAnnotationPresent(Command.class)) {
                    root.add(method.isAnnotationPresent(Permission.class)
                        ? new CommandNode(method.getAnnotation(Command.class),
                        method,
                        method.getAnnotation(Permission.class))
                        : new CommandNode(method.getAnnotation(Command.class), method));
                }

                if (method.isAnnotationPresent(Directive.class)) {
                    sub.add(method.isAnnotationPresent(Permission.class)
                        ? new DirectiveNode(method.getAnnotation(Directive.class),
                        method,
                        method.getAnnotation(Permission.class))
                        : new DirectiveNode(method.getAnnotation(Directive.class), method));
                }
            }

            registerCommandNodes(root.toArray(new CommandNode[0]));
            registerDirectiveNodes(sub.toArray(new DirectiveNode[0]));
        }
    }

    private static void registerCommandNodes(CommandNode... nodes) {
        for (CommandNode node : nodes) {
            if (CommandBank.nodes.containsKey(node.getData().value())) {
                continue;
            }

            Enjin.getLogger().debug("Registering command: " + node.getData().value());
            CommandBank.nodes.put(node.getData().value(), node);
            registerCommandAlias(node.getData().value(), node.getData().aliases());
            EnjinMinecraftPlugin.getInstance().getProxyServer()
                .getCommandManager().register(node.getData().value(), node);
        }
    }

    private static void registerDirectiveNodes(DirectiveNode... nodes) {
        for (DirectiveNode node : nodes) {
            CommandNode command = CommandBank.getNodes().get(node.getData().parent());

            if (command != null) {
                if (command.getDirectives().containsKey(node.getData().value())) {
                    continue;
                }

                Enjin.getLogger().debug("Registering directive: " + node.getData().value() + " " +
                    "for command: " + node.getData().parent());
                command.getDirectives().put(node.getData().value(), node);
                registerDirectiveAlias(node.getData().parent(), node.getData().value(),
                    node.getData().aliases());
            }
        }
    }

    public static void registerCommandAlias(String command, String... alias) {
        if (nodes.containsKey(alias)) {
            Enjin.getLogger().debug("That alias has already been registered by another command.");
            return;
        }

        CommandNode node = nodes.get(command);
        if (node != null) {
            for (String a : alias) {
                nodes.put(a, node);
            }
        }
    }

    public static void registerDirectiveAlias(String command, String directive, String... alias) {
        CommandNode node = nodes.get(command);
        if (node != null) {
            if (node.getDirectives().containsKey(alias)) {
                Enjin.getLogger().debug("That alias has already been registered by another " +
                    "directive.");
                return;
            }

            DirectiveNode directiveNode = node.getDirectives().get(directive);
            if (directiveNode != null) {
                for (String a : alias) {
                    node.getDirectives().put(a, directiveNode);
                }
            }
        }
    }

    private boolean handle(CommandSource source, String c) {
        if (nodes.size() == 0) {
            return false;
        }

        String[] parts = c.startsWith("/") ? c.replaceFirst("/", "").split(" ") : c.split(" ");
        String command = parts[0];

        Optional<CommandNode> w = Optional.fromNullable(nodes.get(command));

        if (w.isPresent()) {
            CommandNode wrapper = w.get();
            String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};
            wrapper.invoke(source, args);
            return true;
        }
        return false;
    }
}
