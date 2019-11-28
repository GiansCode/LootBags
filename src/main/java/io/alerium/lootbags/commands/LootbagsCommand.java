package io.alerium.lootbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.alerium.lootbags.ChatUtil;
import io.alerium.lootbags.LootBagsManager;
import io.alerium.lootbags.LootBagsPlugin;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class LootbagsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatUtil.format("&aRunning LootBags " + LootBagsPlugin.getInstance().getDescription().getVersion() + " by " + LootBagsPlugin.getInstance().getDescription().getAuthors().get(0)));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatUtil.format("&aThese are the current loot bags:"));
            for (LootBagsManager.LootBag bag : LootBagsManager.getInstance().getBags()) {
                sender.sendMessage(ChatUtil.format("&a- " + bag.getName()));
            }
        } else if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("lootbags.give")) {
                sender.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("noPermission")));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(ChatUtil.format("&c/lootbags give <name> <type> [amount]"));
                return true;
            }

            // lootbags give TheTrollz Common 1

            String name = args[2];
            String playerName = args[1];
            int amount = 1;

            if (Bukkit.getPlayer(playerName) == null) {
                sender.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("playerOffline")));
                return true;
            }

            Player player = Bukkit.getPlayer(playerName);

            try {
                amount = Integer.parseInt(args[3]);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                // ignore
            }

            for (LootBagsManager.LootBag bag : LootBagsManager.getInstance().getBags()) {
                if (bag.getName().equalsIgnoreCase(name)) {

                        player.sendMessage(
                                ChatUtil.format(LootBagsPlugin.getInstance().getMessage("give")
                                        .replaceAll("%sender%", sender.getName())
                                        .replaceAll("%type%", name)
                                        .replaceAll("%amount%", amount + "")));
                        for (int x = 0; x < amount; x++) {
                            player.getInventory().addItem(bag.getItem());
                        }

                    return true;
                }
            }

            sender.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("bagNotExist")));
        } else if (args[0].equalsIgnoreCase("giveall")) {
            if (!sender.hasPermission("lootbags.give.all")) {
                sender.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("playerOffline")));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatUtil.format("&c/lootbags giveall <type> [amount]"));
                return true;
            }

            String name = args[1];
            int amount = 1;

            try {
                amount = Integer.parseInt(args[2]);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                // ignore
            }

            for (LootBagsManager.LootBag bag : LootBagsManager.getInstance().getBags()) {
                if (bag.getName().equalsIgnoreCase(name)) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(
                                ChatUtil.format(LootBagsPlugin.getInstance().getMessage("giveAll")
                                        .replaceAll("%sender%", sender.getName())
                                        .replaceAll("%type%", name)
                                        .replaceAll("%amount%", amount + "")));
                        for (int x = 0; x < amount; x++) {
                            player.getInventory().addItem(bag.getItem());
                        }
                    }
                    return true;
                }
            }

            sender.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("bagNotExist")));
        }

        return false;
    }
}
