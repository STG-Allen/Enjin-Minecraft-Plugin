package com.enjin.velocity.command.commands;

import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PointService;
import com.enjin.velocity.command.Directive;
import com.enjin.velocity.command.Permission;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

public class PointCommands {
    private static final String ADD_POINTS_USAGE = "USAGE: /venjin addpoints <points> or /enjin " +
        "addpoints <player> <points>";
    private static final String REMOVE_POINTS_USAGE = "USAGE: /venjin removepoints <points> or " +
        "/enjin removepoints <player> <points>";
    private static final String SET_POINTS_USAGE = "USAGE: /venjin setpoints <points> or /enjin " +
        "setpoints <player> <points>";

    @Permission(value = "enjin.points.add")
    @Directive(parent = "venjin", value = "addpoints")
    public static void add(CommandSource source, String[] args) {
        String name = "";
        Integer points;
        if (source instanceof Player) {
            name = ((Player) source).getUsername();
        }

        if (args.length == 1) {
            if (!(source instanceof Player)) {
                source.sendMessage(TextComponent.of("Only a player can give themselves points."));
                return;
            }

            try {
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                source.sendMessage(TextComponent.of(ADD_POINTS_USAGE));
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                source.sendMessage(TextComponent.of(ADD_POINTS_USAGE));
                return;
            }
        } else {
            source.sendMessage(TextComponent.of(ADD_POINTS_USAGE));
            return;
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.add(name, points);

        if (data == null) {
            source.sendMessage(TextComponent.of("A fatal error has occurred. Please try again " +
                "later. If the problem persists please contact Enjin support."));
            return;
        }

        if (data.getError() != null) {
            source.sendMessage(TextComponent.of(data.getError().getMessage()));
            return;
        }

        source.sendMessage(TextComponent.of((args.length == 1 ? "Your" : (name + "'s")) + " new " +
            "point balance is ").color(TextColor.GREEN).append(
                TextComponent.of(data.getResult()).color(TextColor.GOLD)));
    }

    @Permission(value = "enjin.points.remove")
    @Directive(parent = "venjin", value = "removepoints")
    public static void remove(CommandSource source, String[] args) {
        String name = "";
        if (source instanceof Player) {
            name = ((Player) source).getUsername();
        }
        Integer points;
        if (args.length == 1) {
            if (!(source instanceof Player)) {
                source.sendMessage(TextComponent.of("Only a player can remove points from " +
                    "themselves."));
            }
            try {
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                source.sendMessage(TextComponent.of(REMOVE_POINTS_USAGE));
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                source.sendMessage(TextComponent.of(REMOVE_POINTS_USAGE));
                return;
            }
        } else {
            source.sendMessage(TextComponent.of(REMOVE_POINTS_USAGE));
            return;
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.remove(name, points);

        if (data == null) {
            source.sendMessage(TextComponent.of("A fatal error has occurred. Please try again " +
                "later. If the problem persists please contact Enjin support;"));
            return;
        }

        if (data.getError() != null) {
            source.sendMessage(TextComponent.of(data.getError().getMessage()));
            return;
        }

        source.sendMessage(TextComponent.of((args.length == 1 ? "Your" : (name + "'s")) + " new " +
            "point balance is ").color(TextColor.GREEN).append(
                TextComponent.of(data.getResult()).color(TextColor.GOLD)));
    }

    @Permission(value = "enjin.points.getself")
    @Directive(parent = "venjin", value = "getpoints", aliases = {"points"})
    public static void get(CommandSource source, String[] args) {
        String name = "";
        if (source instanceof Player) {
            name = ((Player) source).getUsername();
        }

        if (args.length == 0) {
            if (!(source instanceof Player)) {
                source.sendMessage(TextComponent.of("Only a player can get their own points."));
                return;
            }
        } else {
            if (source instanceof Player && !source.hasPermission("enjin.points.getothers")) {
                source.sendMessage(TextComponent.of("You need to have the \"").color(TextColor.RED)
                    .append(TextComponent.of("enjin.points.getothers").color(TextColor.GOLD))
                    .append(TextComponent.of("\" permission to run that directive.")
                        .color(TextColor.RED)));
                return;
            }
            name = args[0];
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.get(name);

        if (data == null) {
            source.sendMessage(TextComponent.of("A fatal error has occurred. Please try again " +
                "later. If the problem persists please contact Enjin support."));
            return;
        }

        if (data.getError() != null) {
            source.sendMessage(TextComponent.of(data.getError().getMessage()));
            return;
        }

        source.sendMessage(TextComponent.of((args.length == 0 ? "Your" : (name + "'s")) + " point" +
            " balance is ").color(TextColor.GREEN).append(
                TextComponent.of(data.getResult()).color(TextColor.GOLD)));
    }

    public static void set(CommandSource source, String[] args) {
        String name = "";
        Integer points;
        if (args.length == 1) {
            if (!(source instanceof Player)) {
                source.sendMessage(TextComponent.of("Only a player can set their own points."));
                return;
            }

            try {
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                source.sendMessage(TextComponent.of(SET_POINTS_USAGE));
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                source.sendMessage(TextComponent.of(SET_POINTS_USAGE));
                return;
            }
        } else {
            source.sendMessage(TextComponent.of(SET_POINTS_USAGE));
            return;
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.set(name, points);

        if (data == null) {
            source.sendMessage(TextComponent.of("A fatal error has occurred. Please try again " +
                "later. If the problem persists please contact Enjin support."));
            return;
        }

        if (data.getError() != null) {
            source.sendMessage(TextComponent.of(data.getError().getMessage()));
            return;
        }

        source.sendMessage(TextComponent.of((args.length == 0 ? "Your" : (name + "'s")) + " new " +
            "point balance is ").color(TextColor.GREEN).append(
                TextComponent.of(data.getResult()).color(TextColor.GOLD)));
    }
}
