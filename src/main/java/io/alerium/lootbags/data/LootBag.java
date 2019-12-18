package io.alerium.lootbags.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import io.alerium.lootbags.ChatUtil;
import io.alerium.lootbags.LootBagsPlugin;
import pw.valaria.requirementsprocessor.RequirementsUtil;

public class LootBag {

    private String name;
    private ItemStack item;
    private Function<LootBag, Inventory> inventoryCreator;
    private List<String> dropsString;
    private List<String> lootString;
    private Recipe recipe;
    private ConfigurationSection requirements;
//        private Map<Player, Inventory> inventoryMap = new HashMap<>();

    public LootBag(String name, ItemStack item, ConfigurationSection requirements, List<String> dropsString, List<String> lootString, Function<LootBag, Inventory> inventoryCreator) {
        this.name = name;
        this.item = item;
        this.inventoryCreator = inventoryCreator;
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
        Inventory inventory = inventoryCreator.apply(this);

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

//        public Inventory getInventory(Player player) {
//            return inventoryMap.get(player);
//        }
//
//        public void remove(Player player) {
//            inventoryMap.remove(player);
//        }
}
