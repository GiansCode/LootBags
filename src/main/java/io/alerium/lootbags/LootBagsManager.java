package io.alerium.lootbags;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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

    public void boot(FileConfiguration fileConfiguration) {

        try {
            String inventoryType = fileConfiguration.getString("default-inventory-type", "HOPPER");
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

        for (String bagName : fileConfiguration.getConfigurationSection("bags").getKeys(false)) {

            ConfigurationSection bagSection = fileConfiguration.getConfigurationSection("bags." + bagName);

            InventoryType inventoryType = defaultInventoryType;

            try {
                String inventoryTypeString = bagSection.getString("settings.inventory-type");
                if (inventoryTypeString != null) {
                    inventoryType = InventoryType.valueOf(inventoryTypeString);
                }
            } catch (IllegalArgumentException ex) {
                LootBagsPlugin.getInstance().getLogger().warning("Unknown inventory type, defaulting to hopper!");
            }

            LootBag lootBag = new LootBag(
                    bagSection.getString("settings.name"),
                    parseItem(bagSection.getConfigurationSection("item")),
                    bagSection.getConfigurationSection("requirements"),
                    bagSection.getStringList("drops"),
                    bagSection.getStringList("loot"),
                    inventoryType);
            bags.put(lootBag.name, lootBag);
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

    public static class LootBag {

        private String name;
        private ItemStack item;
        private InventoryType inventoryType;
        private List<String> dropsString;
        private List<String> lootString;
        private Recipe recipe;
        private ConfigurationSection requirements;
//        private Map<Player, Inventory> inventoryMap = new HashMap<>();

        public LootBag(String name, ItemStack item, ConfigurationSection requirements, List<String> dropsString, List<String> lootString, InventoryType inventoryType) {
            this.name = name;
            this.item = item;
            this.inventoryType = inventoryType;
            // The design choice of the requirements util requires that the requirements are extracted from the item,
            // Our solution for this to ensure sane API support by re-embedding the passed requirements into a requirements section if needed
            if (requirements != null && requirements.get("requirements") == null) {
                this.requirements = new YamlConfiguration();
                this.requirements.set("requirements", requirements);
            } else {
                this.requirements = requirements;
            }
            this.dropsString = dropsString;
            this.lootString = lootString;
        }

        public void processKill(Entity entity, Player player) {
            for (String drops : dropsString) {
                if (!entity.getType().name().equalsIgnoreCase(drops.split(";")[0])) {
                    continue;
                }
                int percentage = Integer.parseInt(drops.split(";")[1]);
                int selected = randInt(0, 100, new Random());

                if (selected <= percentage) {
                    entity.getLocation().getWorld().dropItem(entity.getLocation(), item);
                    player.sendMessage(ChatUtil.format("&aYou got a " + name + " loot bag!"));
                }
            }
        }

        public void process(Player player) {
            if (requirements != null) {
                if (!RequirementsUtil.handle(player, requirements)) {
                    player.sendMessage(ChatUtil.format("&cYou don't have permission to open this loot bag."));
                    return;
                }
            }

//            if (inventoryMap.get(player) == null) {
                Inventory inventory = Bukkit.createInventory(null, inventoryType, name + " Loot Bag");

                Random random = new Random();
                String randomLoot = lootString.get(random.nextInt(lootString.size()));

                {
                    int amount = Integer.parseInt(randomLoot.split(";")[2]);
                    short data = (short) Integer.parseInt(randomLoot.split(";")[1]);
                    String type = randomLoot.split(";")[0].toUpperCase();
                    inventory.addItem(new ItemStack(Material.valueOf(type), amount, data));
                }

                for (String loot : lootString) {
                    int percentage = Integer.parseInt(loot.split(";")[3]);
                    int selected = randInt(0, 100, new Random());

                    if (selected <= percentage) {
                        int amount = Integer.parseInt(loot.split(";")[2]);
                        short data = (short) Integer.parseInt(loot.split(";")[1]);
                        String type = loot.split(";")[0].toUpperCase();
                        inventory.addItem(new ItemStack(Material.valueOf(type), amount, data));
                    }
                }

                player.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("useBag").replace("%type%", getName())));

                player.openInventory(inventory);
                if (player.getItemInHand().getAmount() <= 1) {
                    player.setItemInHand(null);
                } else {
                    player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                }
//                inventoryMap.put(player, inventory);

//                remove(player);
//                if (player.getItemInHand().getAmount() <= 1) {
//                    player.setItemInHand(null);
//                } else {
//                    player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
//                }

//            } else {
//                player.openInventory(inventoryMap.get(player));
//            }
        }

        public boolean doesInventoryBelongToBag(Inventory inventory) {
            return inventory.getName().equalsIgnoreCase(name + " loot bag");
        }

        private int randInt(int min, int max, Random random) {
            return random.nextInt((max - min) + 1) + min;
        }

        public ItemStack getItem() {
            return item;
        }

        public Recipe getRecipe() {
            return recipe;
        }

        public void setRecipe(Recipe recipe) {
            this.recipe = recipe;
        }

        public String getName() {
            return name;
        }

        public InventoryType getInventoryType() {
            return inventoryType;
        }

//        public Inventory getInventory(Player player) {
//            return inventoryMap.get(player);
//        }
//
//        public void remove(Player player) {
//            inventoryMap.remove(player);
//        }
    }


}
