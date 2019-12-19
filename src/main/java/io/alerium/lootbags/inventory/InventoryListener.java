package io.alerium.lootbags.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import io.alerium.lootbags.StringUtil;
import io.alerium.lootbags.LootBagsManager;
import io.alerium.lootbags.LootBagsPlugin;
import io.alerium.lootbags.data.LootBag;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            for (LootBag bag : LootBagsManager.getInstance().getBags()) {
                if (bag.doesInventoryBelongToBag(event.getInventory())) {
                    ItemStack itemStack = event.getCurrentItem();
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (event.getInventory() instanceof PlayerInventory || event.getRawSlot() >= event.getInventory().getSize()) {
                            event.setCancelled(true);
                            return;
                        }

                        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                            event.setCancelled(true);
                            player.sendMessage(StringUtil.format(LootBagsPlugin.getInstance().getMessage("noShift")));
                            return;
                        }

//                        player.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("useBag").replace("%type%", bag.getName()).replace("%item%", capitalizeString(itemStack.getType().name().replace("_", " ")))));

//                        if (bag.getInventory(player) != null) {
//                            bag.remove(player);
//                            if (player.getItemInHand().getAmount() <= 1) {
//                                player.setItemInHand(null);
//                            } else {
//                                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
//                            }
////                            player.updateInventory(); // Including this line will cause bugs. Not includng it causes a single, not so annoying display bug. Display bug it is.
//                        }

                    }
                }
            }
        }
    }

}

