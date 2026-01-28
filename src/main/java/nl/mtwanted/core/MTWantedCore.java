package nl.mtwanted.core;

import nl.mtwanted.core.items.CustomItemRegistry;
import nl.mtwanted.core.pack.ResourcePackService;
import nl.mtwanted.core.furniture.FurnitureListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MTWantedCore extends JavaPlugin {

    private CustomItemRegistry itemRegistry;
    private ResourcePackService resourcePackService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("items.yml", false);

        this.itemRegistry = new CustomItemRegistry(this);
        this.itemRegistry.reload();

        this.resourcePackService = new ResourcePackService(this);

        getCommand("mtw").setExecutor(new MtwCommand(this, itemRegistry, resourcePackService));
        getCommand("mtw").setTabCompleter(new MtwTabCompleter(itemRegistry));

        // Furniture (ItemDisplay placement)
        getServer().getPluginManager().registerEvents(new FurnitureListener(this, itemRegistry), this);

        getLogger().info("MTWantedCore enabled. Loaded " + itemRegistry.size() + " custom items.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MTWantedCore disabled.");
    }
}