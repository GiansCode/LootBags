package io.alerium.lootbags.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import io.alerium.lootbags.LootBagsManager;
import io.alerium.lootbags.data.LootBag;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class CraftListener implements Listener {

    @EventHandler
    public void onPreCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();

        for (LootBag bag : LootBagsManager.getInstance().getBags()) {
            if (event.getRecipe() == bag.getRecipe()) {
                for (ItemStack itemStack : inventory.getMatrix()) {
                    if (itemStack != bag.getItem()) {
                        inventory.setResult(null);
                        break;
                    }
                }
            }
        }
    }

}
/*
678
345
012
 */