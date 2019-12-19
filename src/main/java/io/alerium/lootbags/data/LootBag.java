package io.alerium.lootbags.data;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import io.alerium.lootbags.StringUtil;
import io.alerium.lootbags.LootBagsManager;
import io.alerium.lootbags.LootBagsPlugin;
import io.alerium.lootbags.RequirementsPredicate;
import io.alerium.lootbags.event.LootOpenEvent;
import io.samdev.actionutil.ActionUtil;

public class LootBag {

    private final String name;
    private final ItemStack item;
    private final RequirementsPredicate requirements;
    private final Function<LootBag, Inventory> inventoryCreator;
    private final List<Loot> loots;
    private final List<Reward> rewards;
    private Recipe recipe;

    private final EnumMap<EntityType, Drop> dropsEnumMap = new EnumMap<>(EntityType.class);
//        private Map<Player, Inventory> inventoryMap = new HashMap<>();

    public LootBag(String name, ItemStack item, RequirementsPredicate requirements, List<Drop> drops, List<Loot> loots, List<Reward> rewards, Function<LootBag, Inventory> inventoryCreator) {
        this.name = name;
        this.item = item;
        this.requirements = requirements;
        this.loots = loots;
        this.rewards = rewards;
        this.inventoryCreator = inventoryCreator;

        for (Drop drop : drops) {
            dropsEnumMap.put(drop.getEntityType(), drop);
        }
    }

    public void processKill(Entity entity, Player player) {
        Drop drop = dropsEnumMap.get(entity.getType());
        if (drop != null) {
            int selected = randInt(0, 100);

            if (selected <= drop.getChance()) {
                entity.getLocation().getWorld().dropItem(entity.getLocation(), item);
                player.sendMessage(StringUtil.format("&aYou got a " + name + " loot bag!"));
            }
        }
    }

    public void process(Player player) {
        if (requirements != null) {
            if (!requirements.test(player)) {
                player.sendMessage(StringUtil.format("&cYou don't have permission to open this loot bag."));
                return;
            }
        }

//            if (inventoryMap.get(player) == null) {
        Inventory inventory = inventoryCreator.apply(this);

        if (!loots.isEmpty()) {
            Loot randomLoot = loots.get(ThreadLocalRandom.current().nextInt(loots.size()));
            inventory.addItem(randomLoot.getItemStack());

            for (Loot loot : loots) {
                int selected = randInt(0, 100);
                if (selected <= loot.getPercentage()) {
                    ItemStack lootItem = loot.getItemStack().clone();
                    lootItem.setAmount(loot.getAmount());
                    inventory.addItem(lootItem);
                }
            }
        }


        List<Reward> rewards = new ArrayList<>();
        if (LootBagsManager.getInstance().hasActionUtil()) {
            for (Reward reward : this.rewards) {
                int selected = randInt(0, 100);
                if (selected <= reward.getChance()) {
                    rewards.add(reward);
                }
            }
        }

        LootOpenEvent event = new LootOpenEvent(player, this, inventory, rewards);
        if (event.isCancelled()) {
            return;
        }

        player.sendMessage(StringUtil.format(LootBagsPlugin.getInstance().getMessage("useBag").replace("%type%", getName())));

        for (Reward reward : event.getRewards()) {
            List<String> rewardActions = LootBagsManager.getInstance().getReward(reward.getReward());
            if (rewardActions == null) {
                LootBagsPlugin.getInstance().getLogger().warning("Missing reward (" + reward.getReward() + ") from loot (" + getName() + ")");
                continue;
            }
            ActionUtil.executeActions(player, rewardActions);
        }

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
