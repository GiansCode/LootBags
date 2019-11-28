package io.alerium.lootbags.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import io.alerium.lootbags.LootBagsManager;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class EntityDeathListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            for (LootBagsManager.LootBag bag : LootBagsManager.getInstance().getBags()) {
                bag.processKill(event.getEntity(), event.getEntity().getKiller());
            }
        }
    }

}
