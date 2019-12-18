package io.alerium.lootbags;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import io.alerium.lootbags.commands.LootbagsCommand;
import io.alerium.lootbags.inventory.CraftListener;
import io.alerium.lootbags.inventory.EntityDeathListener;
import io.alerium.lootbags.inventory.InteractListener;

public final class LootBagsPlugin extends JavaPlugin implements Listener {

    private static LootBagsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyHeader(true);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

//        this.getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        this.getServer().getPluginManager().registerEvents(new InteractListener(), this);
        this.getServer().getPluginManager().registerEvents(new CraftListener(), this);

        getCommand("lootbags").setExecutor(new LootbagsCommand());

        LootBagsManager.getInstance().boot(this);
    }

    @Override
    public void onDisable() {
        if (LootBagsManager.getInstance().configWasMutated()) {
            saveConfig();
        }
    }

    public static LootBagsPlugin getInstance() {
        return instance;
    }

    public String getMessage(String node) {
        return getConfig().getString("messages." + node);
    }

}
