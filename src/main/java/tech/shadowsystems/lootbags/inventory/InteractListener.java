package tech.shadowsystems.lootbags.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import tech.shadowsystems.lootbags.LootBagsManager;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class InteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            for (LootBagsManager.LootBag bag : LootBagsManager.getInstance().getBags()) {
                if (bag.getItem().isSimilar(event.getItem())) {
                    event.setCancelled(true);
                    bag.process(player);
                    break;
                }
            }
        }
    }

}
