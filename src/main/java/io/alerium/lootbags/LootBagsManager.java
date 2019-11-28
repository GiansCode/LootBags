package io.alerium.lootbags;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class LootBagsManager {

    private static LootBagsManager ourInstance = new LootBagsManager();
    public static LootBagsManager getInstance() {
        return ourInstance;
    }
    private LootBagsManager() {
    }

    private List<LootBag> bags = new ArrayList<>();

    public void boot(FileConfiguration fileConfiguration) {
        for (String bag : fileConfiguration.getConfigurationSection("bags").getKeys(false)) {
            LootBag lootBag = new LootBag(fileConfiguration.getString("bags." + bag + ".settings.name"), parseItem("bags." + bag + ".item", fileConfiguration), fileConfiguration.getBoolean("bags." + bag + ".settings.usePermission"), fileConfiguration.getStringList("bags." + bag + ".drops"), fileConfiguration.getStringList("bags." + bag + ".loot"));
            bags.add(lootBag);
        }

        for (LootBag bag : getBags()) {
            try {
                ShapelessRecipe recipe = new ShapelessRecipe(getBags().get(getBags().indexOf(bag) + 1).getItem());

                recipe.addIngredient(4, bag.getItem().getData());
                bag.setRecipe(recipe);
                LootBagsPlugin.getInstance().getServer().addRecipe(recipe);
            } catch (IndexOutOfBoundsException ex) {
                // nothing
            }
        }

    }

    public ItemStack parseItem(String configPath, FileConfiguration fileConfiguration) {
        Material material = Material.valueOf(fileConfiguration.getString(configPath + ".type").toUpperCase());
        int data = fileConfiguration.getInt(configPath + ".data");
        ItemStack itemStack = new ItemStack(material, 1, (short) data);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatUtil.format(fileConfiguration.getString(configPath + ".name")));
        itemMeta.setLore(color(fileConfiguration.getStringList(configPath + ".lore")));
        itemStack.setItemMeta(itemMeta);

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
        return bags;
    }

    public class LootBag {

        private String name;
        private ItemStack item;
        private boolean usePermission;
        private List<String> dropsString;
        private List<String> lootString;
        private Recipe recipe;
//        private Map<Player, Inventory> inventoryMap = new HashMap<>();

        public LootBag(String name, ItemStack item, boolean usePermission, List<String> dropsString, List<String> lootString) {
            this.name = name;
            this.item = item;
            this.usePermission = usePermission;
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
            if (usePermission) {
                if (!player.hasPermission("lootbags." + name)) {
                    player.sendMessage(ChatUtil.format("&cYou don't have permission to open this loot bag."));
                    return;
                }
            }

//            if (inventoryMap.get(player) == null) {
                Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, name + " Loot Bag");

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

                player.sendMessage(ChatUtil.format(LootBagsPlugin.getInstance().getMessage("useBag").replaceAll("%type%", getName())));

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

//        public Inventory getInventory(Player player) {
//            return inventoryMap.get(player);
//        }
//
//        public void remove(Player player) {
//            inventoryMap.remove(player);
//        }
    }


}
