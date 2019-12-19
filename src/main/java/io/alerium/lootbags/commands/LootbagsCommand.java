package io.alerium.lootbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import io.alerium.lootbags.StringUtil;
import io.alerium.lootbags.LootBagsManager;
import io.alerium.lootbags.LootBagsPlugin;
import io.alerium.lootbags.data.LootBag;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class LootbagsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(StringUtil.format("&aRunning LootBags " + LootBagsPlugin.getInstance().getDescription().getVersion() + " by " + LootBagsPlugin.getInstance().getDescription().getAuthors().get(0)));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(StringUtil.format("&aThese are the current loot bags:"));
            for (LootBag bag : LootBagsManager.getInstance().getBags()) {
                sender.sendMessage(StringUtil.format("&a- " + bag.getName()));
            }
        } else if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("lootbags.give")) {
                sender.sendMessage(StringUtil.format(LootBagsPlugin.getInstance().getMessage("noPermission")));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(StringUtil.format("&c/lootbags give <name> <type> [amount]"));
                return true;
            }

            // lootbags give TheTrollz Common 1

            String name = args[2];
            String playerName = args[1];
            int amount = 1;

            Player player = Bukkit.getPlayer(playerName);

            if (player == null) {
                sender.sendMessage(StringUtil.format(LootBagsPlugin.getInstance().getMessage("playerOffline")));
                return true;
            }

            try {
                amount = Integer.parseInt(args[3]);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                // ignore
            }

            for (LootBag bag : LootBagsManager.getInstance().getBags()) {
                if (bag.getName().equalsIgnoreCase(name)) {

                        player.sendMessage(
                                StringUtil.format(LootBagsPlugin.getInstance().getMessage("give")
                                        .replace("%sender%", sender.getName())
                                        .replace("%type%", name)
                                        .replace("%amount%", amount + "")));
                        for (int x = 0; x < amount; x++) {
                            player.getInventory().addItem(bag.getItem());
                        }

                    return true;
                }
            }

            sender.sendMessage(StringUtil.format(LootBagsPlugin.getInstance().getMessage("bagNotExist")));
        } else if (args[0].equalsIgnoreCase("giveall")) {
            if (!sender.hasPermission("lootbags.give.all")) {
                sender.sendMessage(StringUtil.format(LootBagsPlugin.getInstance().getMessage("playerOffline")));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(StringUtil.format("&c/lootbags giveall <type> [amount]"));
                return true;
            }

            String name = args[1];
            int amount = 1;

            try {
                amount = Integer.parseInt(args[2]);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                // ignore
            }

            for (LootBag bag : LootBagsManager.getInstance().getBags()) {
                if (bag.getName().equalsIgnoreCase(name)) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(
                                StringUtil.format(LootBagsPlugin.getInstance().getMessage("giveAll")
                                        .replace("%sender%", sender.getName())
                                        .replace("%type%", name)
                                        .replace("%amount%", amount + "")));
                        for (int x = 0; x < amount; x++) {
                            player.getInventory().addItem(bag.getItem());
                        }
                    }
                    return true;
                }
            }

            sender.sendMessage(StringUtil.format(LootBagsPlugin.getInstance().getMessage("bagNotExist")));
        }

        return false;
    }
}
