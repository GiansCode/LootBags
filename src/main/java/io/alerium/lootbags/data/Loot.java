package io.alerium.lootbags.data;

import org.bukkit.inventory.ItemStack;

public class Loot {


    private final ItemStack itemStack;
    private Integer amount;
    private final Integer percentage;
    public Loot(ItemStack itemStack, Integer amount, Integer percentage) {
        this.itemStack = itemStack;
        this.amount = amount;
        this.percentage = percentage;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Integer getAmount() {
        return amount;
    }

    public Integer getPercentage() {
        return percentage;
    }
}
