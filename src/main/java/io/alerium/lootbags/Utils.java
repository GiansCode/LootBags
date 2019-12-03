package io.alerium.lootbags;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class Utils {

    public static Inventory createInventory(String name, String typeString, InventoryType defaultType) {

        if (isNumerial(typeString)) {
            int size = Integer.parseInt(typeString) * 9; // rows > count
            return Bukkit.createInventory(null, size, name);
        } else if (typeString != null) {

            try {
                InventoryType inventoryType = InventoryType.valueOf(typeString);
                return Bukkit.createInventory(null, inventoryType, name);
            } catch (IllegalArgumentException ex) {
                LootBagsPlugin.getInstance().getLogger().warning(String.format("Unknown inventory type %s, defaulting to %s!", typeString, defaultType.name()));
            }
        }

        return Bukkit.createInventory(null, defaultType, name);


    }


    // This is horrible, but, isAlpha is failing me for some reason
    public static boolean isNumerial(String str) {
        if (str == null) {
            return false;
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.getInteger(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
