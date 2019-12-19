package io.alerium.lootbags.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.alerium.lootbags.data.LootBag;
import io.alerium.lootbags.data.Reward;

public class LootOpenEvent extends Event implements Cancellable {
    private final Player player;
    private final LootBag lootBag;
    private final Inventory inventory;
    private final List<Reward> rewards;

    private boolean isCancelled = false;

    public LootOpenEvent(@NotNull Player player, @NotNull LootBag lootBag, Inventory inventory, @NotNull List<Reward> rewards) {
        this.player = player;
        this.lootBag = lootBag;
        this.inventory = inventory;
        this.rewards = rewards;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public LootBag getLootBag() {
        return this.lootBag;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @NotNull
    public List<Reward> getRewards() {
        return this.rewards;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }


    private static final HandlerList handlers = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
