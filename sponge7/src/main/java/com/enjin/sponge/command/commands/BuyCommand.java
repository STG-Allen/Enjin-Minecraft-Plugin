package com.enjin.sponge.command.commands;

import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.command.Command;
import com.enjin.sponge.command.Directive;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.managers.PurchaseManager;
import com.enjin.sponge.shop.RPCShopFetcher;
import com.enjin.sponge.shop.TextShopUtil;
import com.enjin.sponge.shop.gui.ShopSelector;
import com.google.common.base.Optional;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BuyCommand {
    @Command(value = "buy")
    public static void buy(Player player, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        Optional<Integer> selection;
        try {
            selection = args.length == 0 ? Optional.<Integer>absent() : Optional.fromNullable(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            player.sendMessage(Text.of(TextColors.RED,
                                       "USAGE: /",
                                       Enjin.getConfiguration(EMPConfig.class).getBuyCommand(),
                                       " #"));
            return;
        }

        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        if (!instances.containsKey(player.getUniqueId()) || shouldUpdate(instances.get(player.getUniqueId()))) {
            fetchShop(player);
        } else {
            if (Enjin.getConfiguration(EMPConfig.class).isUseBuyGUI()) {
                new ShopSelector(instances.get(player.getUniqueId())).open(player);
                return;
            }

            PlayerShopInstance instance = instances.get(player.getUniqueId());
            if (selection.isPresent()) {
                int number = selection.get() < 1 ? 1 : selection.get();
                if (instance.getActiveShop() != null) {
                    if (instance.getActiveCategory() != null) {
                        Category category = instance.getActiveCategory();
                        if (category.getCategories() != null && !category.getCategories().isEmpty()) {
                            if (instance.getActiveCategory().getCategories().size() < number) {
                                instance.updateCategory(instance.getActiveCategory().getCategories().size() - 1);
                            } else {
                                instance.updateCategory(number - 1);
                            }
                        } else {
                            if (instance.getActiveCategory().getItems().size() == 0) {
                                Enjin.getLogger().debug("No items found in category: " + category.getName());
                                player.sendMessage(Text.of(TextColors.RED, "There are no items in this category."));
                            } else if (number == 0) {
                                Enjin.getLogger().debug("Sending first item to " + player.getName());
                                TextShopUtil.sendItemInfo(player, instance, 0);
                            } else if (instance.getActiveCategory().getItems().size() < number) {
                                Enjin.getLogger().debug("Sending last item to " + player.getName());
                                TextShopUtil.sendItemInfo(player, instance, category.getItems().size() - 1);
                            } else {
                                Enjin.getLogger().debug("Sending item to " + player.getName());
                                TextShopUtil.sendItemInfo(player, instance, number - 1);
                            }

                            return;
                        }
                    } else {
                        Enjin.getLogger().debug("No active category has been set. Selecting category from shop.");
                        List<Category> categories = instance.getActiveShop().getCategories();
                        if (categories.size() < number) {
                            instance.updateCategory(categories.size() - 1);
                        } else {
                            instance.updateCategory(number - 1);
                        }
                    }
                } else {
                    if (instance.getShops().size() < number) {
                        instance.updateShop(instance.getShops().size() - 1);
                    } else {
                        instance.updateShop(number - 1);
                    }
                }
            } else {
                if (instance.getActiveCategory() != null) {
                    instance.updateCategory(-1);
                }
            }

            TextShopUtil.sendTextShop(player, instances.get(player.getUniqueId()), -1);
        }
    }

    @Directive(parent = "buy", value = "page")
    public static void page(Player player, String[] args) {
        Optional<Integer> selection;
        try {
            selection = args.length == 0 ? Optional.<Integer>absent() : Optional.fromNullable(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            player.sendMessage(Text.of(TextColors.RED,
                                       "USAGE: /",
                                       Enjin.getConfiguration(EMPConfig.class).getBuyCommand(),
                                       " #"));
            return;
        }

        PlayerShopInstance instance = PlayerShopInstance.getInstances().get(player.getUniqueId());
        if (instance == null || instance.getActiveShop() == null) {
            player.sendMessage(Text.of(TextColors.RED, "You must select a shop before attempting to paginate."));
        } else {
            TextShopUtil.sendTextShop(player, instance, selection.isPresent() ? selection.get() : -1);
        }
    }

    @Directive(parent = "buy", value = "item")
    public static void item(Player player, String[] args) {
        if (Enjin.getConfiguration(EMPConfig.class).isUseBuyGUI()) {
            //            player.sendMessage(Text.of(TextColors.RED, "The text shop has been disabled. Please use the gui to make point purchases."));
            //            return;
        }

        PlayerShopInstance instance = PlayerShopInstance.getInstances().get(player.getUniqueId());
        if (instance == null) {
            player.sendMessage(Text.of(TextColors.RED, "You have no shop selected."));
            return;
        }

        if (args.length == 0) {
            Item item = instance.getActiveItem();
            PurchaseManager.processItemPurchase(player, instance.getActiveShop(), item);
        } else {
            Integer index;

            try {
                index = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(Text.of(TextColors.RED,
                                           "USAGE: /",
                                           Enjin.getConfiguration(EMPConfig.class).getBuyCommand(),
                                           " item #"));
                return;
            }

            if (index < 1) {
                index = 1;
            }

            Category category = instance.getActiveCategory();

            if (category == null) {
                player.sendMessage(Text.of("You must select a category first."));
            } else if (category.getItems() == null || category.getItems().isEmpty()) {
                player.sendMessage(Text.of("There are no items in this category."));
            } else {
                if (index > category.getItems().size()) {
                    index = category.getItems().size();
                }

                Item item = category.getItems().get(index - 1);
                PurchaseManager.processItemPurchase(player, instance.getActiveShop(), item);
            }
        }
    }

    @Directive(parent = "buy", value = "confirm")
    public static void confirm(Player player, String[] args) {
        PurchaseManager.confirmPurchase(player);
    }

    @Directive(parent = "buy", value = "shop")
    public static void shop(Player player, String[] args) {
        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        if (!instances.containsKey(player.getUniqueId())) {
            fetchShop(player);
            return;
        }

        Optional<Integer> selection;
        try {
            selection = args.length == 0 ? Optional.<Integer>absent() : Optional.fromNullable(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            player.sendMessage(Text.of(TextColors.RED,
                                       "USAGE: /",
                                       Enjin.getConfiguration(EMPConfig.class).getBuyCommand(),
                                       " shop #"));
            return;
        }

        if (Enjin.getConfiguration(EMPConfig.class).isUseBuyGUI()) {
            //            Menu menu = new ShopList(player);
            //            menu.openMenu(player);
            //
            //            return;
        }

        PlayerShopInstance instance = instances.get(player.getUniqueId());
        if (!selection.isPresent()) {
            instance.updateShop(-1);
            TextShopUtil.sendTextShop(player, instance, -1);
        } else {
            int value = selection.get() < 1 ? -1 : selection.get() - 1;
            instance.updateShop(value);
            TextShopUtil.sendTextShop(player, instance, -1);
        }

    }

    private static boolean shouldUpdate(PlayerShopInstance instance) {
        return (System.currentTimeMillis() - instance.getLastUpdated()) > TimeUnit.MINUTES.toMillis(10);
    }

    private static void fetchShop(Player player) {
        EnjinMinecraftPlugin.getInstance().getGame().getScheduler().createTaskBuilder()
                            .execute(new RPCShopFetcher(player))
                            .submit(Enjin.getPlugin());
    }
}
