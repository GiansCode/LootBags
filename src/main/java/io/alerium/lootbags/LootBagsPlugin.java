package io.alerium.lootbags;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import io.alerium.lootbags.commands.LootbagCommand;
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

        getCommand("lootbags").setExecutor(new LootbagCommand());

        LootBagsManager.getInstance().boot(getConfig());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static LootBagsPlugin getInstance() {
        return instance;
    }

    public String getMessage(String node) {
        return getConfig().getString("messages." + node);
    }

}
