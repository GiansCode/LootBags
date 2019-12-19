package io.alerium.lootbags;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import io.alerium.lootbags.data.Drop;
import io.alerium.lootbags.data.Loot;
import io.alerium.lootbags.data.Reward;

public class LootUtils {

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

    public static ItemStack parseItem(ConfigurationSection itemSection) {
        Material material = Material.valueOf(itemSection.getString("type").toUpperCase());
        int data = itemSection.getInt("data");

        ItemStack itemStack = new ItemStack(material, 1, (short) data);
        ItemMeta meta = itemStack.getItemMeta();

        assert meta != null; // All non-air itemstacks have meta!

        meta.setDisplayName(StringUtil.format(itemSection.getString("name")));
        meta.setLore(StringUtil.color(itemSection.getStringList("lore")));
        itemStack.setItemMeta(meta);
        return itemStack;

    }

    public static List<Reward> parseRewards(Map<String, List<String>> rewardsMap, ConfigurationSection bag) {
        List<Reward> rewards = new ArrayList<>();

        List<Map<?, ?>> rewardSection = bag.getMapList("rewards");

        for (Map<?, ?> map : rewardSection) {

            String reward = (String) Objects.requireNonNull(map.get("reward"), "reward type");
            Integer percentage = Integer.parseInt(Objects.requireNonNull(map.get("percentage"), "percentage").toString());

            if (rewardsMap.get(reward) == null) {
                LootBagsPlugin.getInstance().getLogger().log(Level.WARNING, "missing reward type " + reward);
                continue;
            }

            rewards.add(new Reward(reward, percentage));
        }

        return rewards;
    }

    public static List<Drop> parseDrops(ConfigurationSection bag) {
        List<Drop> drops = new ArrayList<>();

        List<Map<?, ?>> dropSection = bag.getMapList("drops");

        for (Map<?, ?> map : dropSection) {
            String type = (String) Objects.requireNonNull(map.get("entity-type"), "entity type");
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(type);
            } catch (IllegalArgumentException ex) {
                LootBagsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid entity type: " + type, ex);
                continue;
            }

            Integer percentage = Integer.parseInt(Objects.requireNonNull(map.get("percentage"), "percentage").toString());


            drops.add(new Drop(entityType, percentage));
        }

        return drops;
    }

    public static List<Loot> parseLoots(ConfigurationSection bag) {
        List<Loot> loots = new ArrayList<>();

        List<Map<?, ?>> lootSection = bag.getMapList("loot");

        for (Map<?, ?> map : lootSection) {
            String type = (String) Objects.requireNonNull(map.get("type"), "loot type");

            short data;

            Object dataRaw = map.get("data");
            if (dataRaw == null) {
                data = 0;
            } else {
                data = Short.parseShort(dataRaw.toString());
            }

            Integer percentage = Integer.parseInt(Objects.requireNonNull(map.get("percentage"), "percentage").toString());

            int amount;
            Object amountRaw = map.get("amount");
            if (amountRaw == null) {
                amount = 0;
            } else {
                amount = Integer.parseInt(amountRaw.toString());
            }

            loots.add(new Loot(new ItemStack(Material.valueOf(type), 1, data), amount, percentage));
        }

        return loots;
    }
}
