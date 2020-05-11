package com.enjin.velocity.command.commands;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.enjin.rpc.mappings.mappings.plugin.TagData;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.velocity.EnjinMinecraftPlugin;
import com.enjin.velocity.command.Command;
import com.enjin.velocity.command.Directive;
import com.enjin.velocity.command.Permission;
import com.enjin.velocity.tasks.ReportPublisher;
import com.enjin.velocity.utils.io.EnjinConsole;
import com.google.common.base.Optional;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CoreCommands {

    @Command(value = "venjin", aliases = "ve", requireValidKey = false)
    public static void enijn(CommandSource source, String[] args) {
        source.sendMessage(EnjinConsole.header());

        if (source.hasPermission("enjin.setkey")) {
            TextComponent key = TextComponent.builder()
                .append("/venjin key <KEY> : ").color(TextColor.GOLD)
                .append("Enter the secret key from your ").color(TextColor.GRAY)
                .append("Admin -> Games -> Minecraft -> Enjin plugin ")
                .append("page.").color(TextColor.WHITE)
                .build();
            source.sendMessage(key);
        }

        if (source.hasPermission("enjin.debug")) {
            TextComponent debug = TextComponent.builder()
                .append("/venjin debug: ").color(TextColor.GOLD)
                .append("Enable debug mode and display extra information in console.").color(TextColor.WHITE)
                .build();
            source.sendMessage(debug);
        }

        if (source.hasPermission("enjin.report")) {
            TextComponent report = TextComponent.builder()
                .append("/venjin report: ").color(TextColor.GOLD)
                .append("Generate a report file that you can  send to Enjin Support for " +
                    "troubleshooting.").color(TextColor.WHITE)
                .build();
            source.sendMessage(report);
        }

        if (source.hasPermission("enjin.tags.view")) {
            TextComponent tag = TextComponent.builder()
                .append("/venjin tags <player>: ").color(TextColor.GOLD)
                .append("Shows the tags on the website for the player").color(TextColor.WHITE)
                .build();
            source.sendMessage(tag);
        }

        if (source.hasPermission("enjin.points.getself")) {
            TextComponent sourcePoints = TextComponent.builder()
                .append("/venjin points: ").color(TextColor.GOLD)
                .append("Shows your current website points").color(TextColor.WHITE)
                .build();
            source.sendMessage(sourcePoints);
        }

        if (source.hasPermission("enjin.points.getothers")) {
            TextComponent othersPoints = TextComponent.builder()
                .append("/venjin points <NAME>: ").color(TextColor.GOLD)
                .append("Shows another players's current website points").color(TextColor.WHITE)
                .build();
            source.sendMessage(othersPoints);
        }

        if (source.hasPermission("enjin.points.add")) {
            TextComponent addPoints = TextComponent.builder()
                .append("/venjin addpoints <NAME> <AMOUNT>: ").color(TextColor.GOLD)
                .append("Adds points to a player.").color(TextColor.WHITE)
                .build();
            source.sendMessage(addPoints);
        }

        if (source.hasPermission("enjin.points.remove")) {
            TextComponent removePoints = TextComponent.builder()
                .append("/venjin removepoints <NAME> <AMOUNT>: ").color(TextColor.GOLD)
                .append("Remove points from a player.").color(TextColor.WHITE)
                .build();
            source.sendMessage(removePoints);
        }

        if (source.hasPermission("enjin.points.set")) {
            TextComponent setPoints = TextComponent.builder()
                .append("/venjin setpoints <NAME> <AMOUNT>: ").color(TextColor.GOLD)
                .append("Set a player's total points.").color(TextColor.WHITE)
                .build();
            source.sendMessage(setPoints);
        }
    }

    @Permission(value = "enjin.debug")
    @Directive(parent = "venjin", value = "debug", requireValidKey = false)
    public static void debug(CommandSource source, String[] args) {
        EnjinConfig config = Enjin.getConfiguration();
        config.setDebug(!config.isDebug());
        EnjinMinecraftPlugin.saveConfiguration();
        source.sendMessage(TextComponent.builder().append("Debugging has been set to " + config.isDebug()).color(TextColor.GREEN).build());
    }

    @Permission(value = "enjin.setkey")
    @Command(value = "venjinkey", aliases = "vek", requireValidKey = false)
    @Directive(parent = "venjin", value = "key", aliases = {"setkey", "sk", "enjinkey", "ek"},
        requireValidKey = false)
    public static void key(final CommandSource source, final String[] args) {
        if (args.length != 1) {
            source.sendMessage(TextComponent.of("USAGE: /enjin key <key>"));
            return;
        }

        Enjin.getLogger().info("Checking if key is valid");

        EnjinMinecraftPlugin.getInstance().getProxyServer().getScheduler().buildTask(EnjinMinecraftPlugin.getInstance(), () -> {
            if (Enjin.getConfiguration().getAuthKey().equals(args[0])) {
                source.sendMessage(TextComponent.of("That key has already been validated."));
                return;
            }

            Optional<Integer> port = EnjinMinecraftPlugin.getInstance().getPort();
            PluginService service = EnjinServices.getService(PluginService.class);
            RPCData<Auth> data = service.auth(Optional.of(args[0]),
                port.isPresent() ? port.get() : null,
                true,
                true);

            if (data == null) {
                source.sendMessage(TextComponent.of("A fatal error has occurred. Please try " +
                    "again later. If the problem persists please contact Enjin Support."));
                return;
            }

            if (data.getError() != null) {
                source.sendMessage(TextComponent.of(data.getError().getMessage()).color(TextColor.RED));
                return;
            }

            if (data.getResult() != null && data.getResult().isAuthed()) {
                source.sendMessage(TextComponent.of("The key has been successfully validated.").color(TextColor.GREEN));
                Enjin.getConfiguration().setApiUrl(args[0]);
                EnjinMinecraftPlugin.saveConfiguration();

                if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid()) {
                    EnjinMinecraftPlugin.getInstance().setAuthKeyInvalid(false);
                    EnjinMinecraftPlugin.getInstance().init();
                }
            } else {
                source.sendMessage(TextComponent.of("We were unable to validate the provided key" +
                    ".").color(TextColor.RED));
            }
        }).schedule();
    }

    @Permission(value = "enjin.report")
    @Directive(parent = "venjin", value = "report", requireValidKey = false)
    public static void report(CommandSource source, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss z");

        source.sendMessage(TextComponent.of("Please wait while we generate the report.").color(TextColor.GREEN));

        StringBuilder report = new StringBuilder();
        report.append("Enjin Debug Report generated on ").append(dateFormat.format(date)).append(
            "\n");
        report.append("Enjin plugin version: ").append("3.0.1").append("\n");
        report.append("Velocity version: ").append(plugin.getProxyServer().getVersion()).append(
            "\n");
        report.append("Java version: ")
            .append(System.getProperty("java.version"))
            .append(" ")
            .append(System.getProperty("java.vendor"))
            .append("\n");
        report.append("Operating system: ")
            .append(System.getProperty("os.name"))
            .append(" ")
            .append(System.getProperty("os.version"))
            .append(" ")
            .append(System.getProperty("os.arch"))
            .append("\n");

        if (plugin.isAuthKeyInvalid()) {
            report.append("ERROR: Authkey reported by plugin is invalid!\n");
        }

        if (plugin.isUnableToContactEnjin()) {
            report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 " +
                "minutes\n");
        }

        report.append("Enjin Server ID: ")
            .append(plugin.getServerId())
            .append("\n");

        report.append("\nPlugins: \n");
        for (PluginContainer p : plugin.getProxyServer().getPluginManager().getPlugins()) {
            report.append(p.getDescription().getName())
                .append(" version ")
                .append(p.getDescription().getVersion())
                .append("\n");
        }

        plugin.getProxyServer().getScheduler().buildTask(plugin, new ReportPublisher(plugin,
            report, source)).schedule();
    }


    @Permission(value = "enjin.tags")
    @Directive(parent = "venjin", value = "tags", requireValidKey = true)
    public static void tags(CommandSource source, String[] args) {
        if (args.length == 0) {
            source.sendMessage(TextComponent.of("USAGE: /venjin tags <player>"));
            return;
        }

        String name = args[0].substring(0, args[0].length() > 16 ? 16 : args[0].length());
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<List<TagData>> data = service.getTags(name);

        if (data == null) {
            source.sendMessage(TextComponent.of("A fatal error has occurred. Please try again " +
                "later. If the problem persists contact Enjin support."));
            return;
        }

        if (data.getError() != null) {
            source.sendMessage(TextComponent.of(data.getError().getMessage()));
            return;
        }

        List<TagData> tags = data.getResult();
        TextComponent tagList = TextComponent.of("");
        if (tags != null) {
            Iterator<TagData> iterator = tags.iterator();
            while (iterator.hasNext()) {
                if (!tagList.isEmpty()) {
                    tagList.append(TextComponent.of(", ").color(TextColor.GOLD));
                }

                TagData tag = iterator.next();
                tagList.append(TextComponent.of(tag.getName()).color(TextColor.GREEN));
            }
        }
        source.sendMessage(TextComponent.of(name).color(TextColor.GOLD).append(TextComponent.of(
            "'s Tags: ")).append(tagList));
    }
}
