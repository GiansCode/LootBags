package io.alerium.lootbags;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;

import io.alerium.lootbags.data.Loot;
import io.alerium.lootbags.data.LootBag;
import pw.valaria.requirementsprocessor.RequirementsUtil;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class LootBagsManager {

    private static LootBagsManager ourInstance = new LootBagsManager();
    private final boolean isLegacy;
    private boolean configWasMutated = false;
    private InventoryType defaultInventoryType;
    public static LootBagsManager getInstance() {
        return ourInstance;
    }

    private LootBagsManager() {
        boolean isLegacy1;
        try {
            Class.forName("org.bukkit.NamespacedKey");
            isLegacy1 = false;
        } catch (ClassNotFoundException e) {
            isLegacy1 = true;
        }
        isLegacy = isLegacy1;

        RequirementsUtil.isDebug();
    }

    private Map<String, LootBag> bags = new HashMap<>();

    void boot(FileConfiguration fileConfiguration) {

        try {
            String inventoryType = fileConfiguration.getString("settings.default-inventory-type", "HOPPER");
            defaultInventoryType = InventoryType.valueOf(inventoryType);
        } catch (IllegalArgumentException ex) {
            LootBagsPlugin.getInstance().getLogger().warning("Unknown inventory type, defaulting to hopper!");
            defaultInventoryType = InventoryType.HOPPER;
        }

        // Migrations
        ConfigurationSection bagSections = fileConfiguration.getConfigurationSection("bags");
        if (bagSections == null) {
            LootBagsPlugin.getInstance().getLogger().warning("Configuration does not have any bags defined!");
            return;
        }

        for (String bagName : bagSections.getKeys(false)) {
            ConfigurationSection bag = bagSections.getConfigurationSection(bagName);
            if (bag.isSet("settings.usePermission")) {
                if (bag.getBoolean("settings.usePermission")) {
                    final ConfigurationSection bagPermRequirement = bag.createSection("requirements")
                                                                        .createSection("permission");

                    bagPermRequirement.set("requirement-type", "HAS_PERMISSION");
                    bagPermRequirement.set("input", "lootbags." + bagName);
                }
                bag.set("settings.usePermission", null);
                configWasMutated = true;
            }
        }

        for (String bagName : bagSections.getKeys(false)) {
            ConfigurationSection bag = bagSections.getConfigurationSection(bagName);
            List<?> loot = bag.getList("loot");
            List<Map<String, Object>> newLoot = new ArrayList<>();
            boolean wasMutated = false;

            for (Object lootEntry : loot) {
                if (lootEntry instanceof Map) {
                    newLoot.add((Map<String, Object>) lootEntry);
                } else if (lootEntry instanceof String) {
                    String lootEntryString = (String) lootEntry;
                    int percentage = Integer.parseInt(lootEntryString.split(";")[3]);
                    int amount = Integer.parseInt(lootEntryString.split(";")[2]);
                    short data = (short) Integer.parseInt(lootEntryString.split(";")[1]);
                    String type = lootEntryString.split(";")[0].toUpperCase();

                    Map<String, Object> newLootEntry = new HashMap<String, Object>();
                    newLootEntry.put("type", type);
                    newLootEntry.put("data", data);
                    newLootEntry.put("amount", amount);
                    newLootEntry.put("percentage", percentage);

                    newLoot.add(newLootEntry);
                    wasMutated = true;
                } else {
                    LootBagsPlugin.getInstance().getLogger().warning("Unable to handle loots for lootbag" + bagName);
                }

            }

            if (wasMutated) {
                bag.set("loot", newLoot);
                configWasMutated = true;
            }
        }


        for (String bagName : fileConfiguration.getConfigurationSection("bags").getKeys(false)) {

            ConfigurationSection bagSection = fileConfiguration.getConfigurationSection("bags." + bagName);

            try {
                LootBag lootBag = new LootBag(
                        bagSection.getString("settings.name"),
                        parseItem(bagSection.getConfigurationSection("item")),
                        new RequirementsPredicate(bagSection.getConfigurationSection("requirements")),
                        bagSection.getStringList("drops"),
                        parseLoots(bagSection),
                        (lootBag1) -> {
                            return Utils.createInventory(lootBag1.getName() + " Loot Bag", bagSection.getString("settings.inventory-type-or-size"), defaultInventoryType);
                        });
                bags.put(lootBag.getName(), lootBag);
            } catch (Throwable ex) {
                LootBagsPlugin.getInstance().getLogger().log(Level.WARNING, "An error occured while processing loot bag " + bagName, ex);
            }

        }

        // using an array deque, we'll peek into the next slot instead of relying upon an exception
        // to populate recipes
        final ArrayDeque<LootBag> processingBags = new ArrayDeque<>(getBags());
        while (!processingBags.isEmpty()) {
            final LootBag currentBag = processingBags.pop();
            // Declare var, mainly for making the IDE quiet
            final LootBag nextBag = processingBags.peek();
            if (nextBag != null) {
                ShapelessRecipe recipe;

                if (isLegacy) {
                    recipe = new ShapelessRecipe(processingBags.peek().getItem());
                } else {
                    recipe = new ShapelessRecipe(new NamespacedKey(LootBagsPlugin.getInstance(), currentBag.getName()),nextBag.getItem());
                }

                recipe.addIngredient(4, Objects.requireNonNull(currentBag.getItem().getData()));
                currentBag.setRecipe(recipe);
                LootBagsPlugin.getInstance().getServer().addRecipe(recipe);
            }
        }

    }

    public ItemStack parseItem(ConfigurationSection itemSection) {
        Material material = Material.valueOf(itemSection.getString("type").toUpperCase());
        int data = itemSection.getInt("data");

        ItemStack itemStack = new ItemStack(material, 1, (short) data);
        ItemMeta meta = itemStack.getItemMeta();

        assert meta != null; // All non-air itemstacks have meta!

        meta.setDisplayName(ChatUtil.format(itemSection.getString("name")));
        meta.setLore(color(itemSection.getStringList("lore")));
        itemStack.setItemMeta(meta);
        return itemStack;

    }

    public List<String> color(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String str : list) {
            newList.add(ChatUtil.format(str));
        }
        return newList;
    }

    public List<LootBag> getBags() {
        return new ArrayList<>(bags.values());
    }

    public boolean configWasMutated() {
        return configWasMutated;
    }


    private List<Loot> parseLoots(ConfigurationSection bag) {
        List<Loot> loots = new ArrayList<>();

        List<Map<?, ?>> lootSection = bag.getMapList("loot");

        if (lootSection.size() == 0) {
            throw new IllegalArgumentException("Bag does not have loots!");
        }

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
