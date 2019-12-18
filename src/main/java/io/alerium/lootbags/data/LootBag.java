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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import io.alerium.lootbags.ChatUtil;
import io.alerium.lootbags.LootBagsPlugin;
import io.alerium.lootbags.RequirementsPredicate;
import pw.valaria.requirementsprocessor.RequirementsUtil;

public class LootBag {

    private String name;
    private ItemStack item;
    private RequirementsPredicate requirements;
    private Function<LootBag, Inventory> inventoryCreator;
    private List<String> dropsString;
    private List<Loot> loots;
    private Recipe recipe;
//        private Map<Player, Inventory> inventoryMap = new HashMap<>();

    public LootBag(String name, ItemStack item, RequirementsPredicate requirements, List<String> dropsString, List<Loot> loots, Function<LootBag, Inventory> inventoryCreator) {
        this.name = name;
        this.item = item;
        this.requirements = requirements;
        this.loots = loots;
        this.inventoryCreator = inventoryCreator;
        this.dropsString = dropsString;
    }

    public void processKill(Entity entity, Player player) {
        for (String drops : dropsString) {
            if (!entity.getType().name().equalsIgnoreCase(drops.split(";")[0])) {
                continue;
            }
            int percentage = Integer.parseInt(drops.split(";")[1]);
            int selected = randInt(0, 100);

            if (selected <= percentage) {
                entity.getLocation().getWorld().dropItem(entity.getLocation(), item);
                player.sendMessage(ChatUtil.format("&aYou got a " + name + " loot bag!"));
            }
        }
    }

    public void process(Player player) {
        if (requirements != null) {
            if (!requirements.test(player)) {
                player.sendMessage(ChatUtil.format("&cYou don't have permission to open this loot bag."));
                return;
            }
        }

//            if (inventoryMap.get(player) == null) {
        Inventory inventory = inventoryCreator.apply(this);

        Loot randomLoot = loots.get(ThreadLocalRandom.current().nextInt(loots.size()));
        inventory.addItem(randomLoot.getItemStack());

        for (Loot loot : loots) {
            int selected = randInt(0, 100);
            if (selected <=loot.getPercentage()) {
                ItemStack lootItem = loot.getItemStack().clone();
                lootItem.setAmount(loot.getAmount());
                inventory.addItem(lootItem);
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

    private int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
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
